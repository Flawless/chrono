(ns chrono.mask
  (:refer-clojure :exclude [resolve])
  #?@
   (:clj
    [(:require
      [chrono.io :as io]
      [chrono.util :as util]
      [clojure.string :as str])]
    :cljs
    [(:require
      [chrono.io :as io]
      [chrono.util :as util]
      [clojure.string :as str]
      goog.string)]))

(defn- format-str [fmt & args]
  (apply
   #?(:clj  clojure.core/format
      :cljs goog.string/format)
   fmt
   args))

(defn mask-parse-matcher [f s]
  (let [key? (contains? util/format-patterns f)
        f-len (util/format-patterns f)
        p (util/patternize f)
        [match-s cur-s rest-s :as res] (some-> p
                                               (#(str "(" % ")" "(.+)?"))
                                               re-pattern
                                               (re-matches s))
        res (if (and key?
                     (not= f-len (count cur-s))
                     (nil? rest-s)
                     ((re-matches (re-pattern p) (str cur-s \0)))) [s "" s] res)]
    (if-not res [s "" s] res)))

(defn parse [s fmt]
  (let [drop-pat (-> (remove keyword? fmt)
                     str/join
                     (#(str \["^0-9" % \]))
                     re-pattern)
        s (some-> s (str/replace drop-pat ""))
        res (io/priv-parse mask-parse-matcher {:s s :f fmt})]
    (-> res :acc
        (->> (filter #(-> % val empty? not)))
        (#(zipmap (keys %) (map util/parse-int (vals %))))
        (cond-> (:s res) (assoc :not-parsed (:s res))))))


 ;          :acc
(defn build [t fmt]
  (reduce (fn [acc f]
            (let [kw (cond-> f (vector? f) first)
                  v  (get t kw)]
              (cond
                (not (contains? util/format-patterns kw))
                (str acc f)

                (number? v)
                (str acc (format-str (str "%0" (if (vector? f) (second f) (util/format-patterns f)) \d) v))

                (string? v)
                (str acc v)

                :else (reduced acc))))
          ""
          fmt))

(defn clean-build [t fmt]
  (let [clean-fmt
        (reduce
         (fn [acc f]
           (cond
             (not (keyword? f)) (update acc :buff conj f)
             (some? (get t f))  (-> acc
                                    (update :result concat (conj (:buff acc) f))
                                    (assoc :buff []))
             :else              (reduced acc)))
         {:result []
          :buff   []}
         fmt)]
    (build t (vec (:result clean-fmt)))))

(defn resolve [s fmt]
  (let [{:keys [not-parsed] :as p} (parse s fmt)]
    (str (build p fmt) not-parsed)))
