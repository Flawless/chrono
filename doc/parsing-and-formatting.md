parse and strict-parse functions allow to create datetime hashmap from it's string representations. Format represented as a vecor. If ommited ISO format is used.

```clj
(require '[chrono.core :as ch])

(ch/parse "2018-01-29T10:30:15.222")
;; => {:year 2018, :month 1, :day 29, :hour 10, :min 30, :sec 15, :ms 222}

;; if some fileds ommited from the end of string only existed will be parsed
(ch/parse "2018-01-29" [:year \- :month \- :day \T :hour \: :min \: :sec \. :ms])
;; => {:year 2018, :month 1, :day 29}

;; insted of `parse` `strict-parse` will return date only if string exactly match

(ch/strict-parse "2018-01-29" [:year \- :month \- :day])
;; => {:year 2018, :month 1, :day 29}

(ch/strict-parse "2018-01-29" [:year \- :month \- :day \T :hour \: :min \: :sec \. :ms])
;; => nil
```
Conversivly, format function may be used to create string from hashmap. Custom keys allowed in format

```clj
(require '[chrono.core :as ch])

(ch/format {:day 28 :month 1 :year 2018} [:day "/" :month "/" :year]) ;; => "29/01/2018"

;; custom width for special position may be provided by passing int parameter to format vector

(ch/format {:day 28 :month 1 :year 2018} [:day "/" :month "/" [:year 2]]) ;; => "29/01/18"

;; if width specified greater then number witdh additional zeros will be added
(ch/format {:hour 333 :min 10 :sec 34} [[:hour 4] :min  :sec :ms]) ;; => "03331034000"
```

Format specified by vector with keyword ans symbol sequence as they placed in string, also extended format may be used. In extended format some keywords may be replaced by vectors, inside of which first place must be occupied by current keyword and others may contains special values.

Language for months literal representations my by specified by metadata of format vector. Library provides two builtin languages for months: english, which is used by default and russian. Other languages may be added via adding methods for util/locale multimethods.

```clj
(require '[chrono.core :as ch])

(ch/parse "November 1992" ^:en[:month :year]) ;; => {:year 1992 :month 11}

;; short month representation obtained in case special keyword :short supplied
(ch/format {:month 11 :day 25} ^:en[[:month :short] \. :day]) ;; => "Nov. 25"


;; list of languages may be extended by specifying method locale

(require '[chrono.util :as util])

(defmethod util/locale :ru [_]
  {:month
   {1 {:name "Январь", :short "Янв", :regex "(?iu)янв(ар(ь|я))?"}
    2 {:name "Февраль", :short "Фев", :regex "(?iu)фев(рал(ь|я))?"}
    3 {:name "Март", :short "Мар", :regex "(?iu)мар(та?)?"}
    4 {:name "Апрель", :short "Апр", :regex "(?iu)апр(ел(ь|я)?)?"}
    5 {:name "Май", :short "Май", :regex "(?iu)ма(й|я)?"}
    6 {:name "Июнь", :short "Июн", :regex "(?iu)июн(ь|я)?"}
    7 {:name "Июль", :short "Июл", :regex "(?iu)июл(ь|я)?"}
    8 {:name "Авгусь", :short "Авг", :regex "(?iu)авг(уста?)?"}
    9 {:name "Сентябрь", :short "Сен", :regex "(?iu)сен(тябр(ь|я)?)?"}
    10 {:name "Октябрь", :short "Окт", :regex "(?iu)окт(ябр(ь|я)?)?"}
    11 {:name "Ноябрь", :short "Ноя", :regex "(?iu)ноя(бр(ь|я)?)?"}
    12 {:name "Декабрь", :short "Дек", :regex "(?iu)дек(бр(ь|я)?)?"}}})

(ch/parse "декабрь" [:month]) ;; {:month 12}
(ch/format {:month 03} [[:month :short]]) ;; "Мар"
```

Also extended format allow passing custom format function for field into format vector

```clj
(require '[chrono.core :as ch])

(ch/format {:some-field "abc"} [[:some-field (fn [v fmt-vector lang] (clojure.string/upper-case v))]])
;; => "ABC"
(ch/format {:some-field 5} [[:some-field 2 (fn [v [kw second-arg this-function] _] (+ v second-arg))]])
;; => "7"
```
