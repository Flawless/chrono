(ns chrono.mask-test
  (:require [chrono.mask :as sut]
            [clojure.test :as t]
            [chrono.io :as io]))

(t/deftest mask-test
  (let [cases {[:hour \: :min \: :sec]
               {"2300"     "23:00:"
                "23:00"    "23:00:"
                "230000"   "23:00:00"
                "23:00:00" "23:00:00"
                ""         ""
                "2"        "2"
                "02:2"     "02:2"
                "02:2:"    "02:02:"
                "02:20"    "02:20:"
                "02:02:2"  "02:02:2"
                nil        ""
                "123456"   "12:34:56"
                "2:2"      "02:2"
                "2:2:"     "02:02:"
                "2:2:2"    "02:02:2"
                "399"      "03:09:09"
                "999999"   "09:09:09"}

               [:month \- :day]
               {"01-01" "01-01"
                "0101"  "01-01"
                "67"    "06-07"
                "!!!"   ""
                "0!"    "0"
                "!0"    "0"
                "11-!"  "11-"
                "11-!0" "11-0"
                "11-0!" "11-0"
                "1"     "1"
                "1!"    "1"
                "11"    "11-"
                "331"   "03-31"
                "12"    "12-"
                "0"     "0"
                "9"     "09-"
                ""      ""}}]
    (doseq [[fmt facts] cases
            [inp res]   facts]
      (t/testing (str "resolve: " fmt " " inp)
          (t/is (= res (sut/resolve inp fmt)))))))

(t/deftest mask-parse-test
  (t/testing "test for mask-parse match function"
    (t/is (= (sut/mask-parse-matcher :hour "230000") ["230000" "23" "0000"]))
    (t/is (= (sut/mask-parse-matcher \:    "0000"  ) ["0000"   ""   "0000"]))
    (t/is (= (sut/mask-parse-matcher :min  "0000"  ) ["0000"   "00" "00"  ]))
    (t/is (= (sut/mask-parse-matcher ":"   "00"    ) ["00"     ""   "00"  ]))
    (t/is (= (sut/mask-parse-matcher :sec  "00"    ) ["00"     "00" nil   ]))

    (t/is (= ["!0" ""   "!0"] (sut/mask-parse-matcher :month "!0")))
    (t/is (= ["1"  ""   "1" ] (sut/mask-parse-matcher :month "1" )))
    (t/is (= ["!0" ""   "!0"] (sut/mask-parse-matcher \-     "!0"))))
  (t/testing "testing parse"
    (t/is (= (sut/parse "230000" [:hour \: :min \: :sec])
             {:hour 23 :min 0 :sec 0}))
    (t/is (= (sut/parse "!0" [:month \: :day])
             {:not-parsed "0"}))
    (t/is (= (sut/parse "11-" [:month \: :day])
             {:month 11}))
    (t/is (= (sut/parse "1" [:month \- :day])
             {:not-parsed "1"}))))
