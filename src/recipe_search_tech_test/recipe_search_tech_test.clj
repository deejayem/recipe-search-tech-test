(ns recipe-search-tech-test.recipe-search-tech-test
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [recipe-search-tech-test.stop-words :as sw]))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))

;; TODO define scores
;; TODO define sections?
;; TODO plurals, synonyms
;; TODO exclude short words? (Or score them lower?)

;; TODO what values should these have?
(def ^:private section-weightings
  {:title 100
   :introduction 2
   :ingredients 2
   :method 2})

;; e.g. {"cheese" [{:id "foo.txt" :title 1 :ingredients 1 :intro 1 :method 3}]}
;; or {"cheese" {"foo.txt" {:title 1 :ingredients 1 :intro 1 :method 3}}}

(defn- strip-ears
  "TODO"
  [word]
  (str/replace word #"^'|'$" ""))

;; TODO this is a bad name (add-number-complement add-plural-complement ?)
(defn- add-plurals
  "TODO"
  [words]
  (reduce (fn [acc word]
            (if (str/ends-with? word "s")
              (conj acc (str/replace word #"s$" ""))
              (conj acc (str word "s"))))
          words
          words))

(defn- index-line
  "TODO"
  [index file section line]
  (reduce (fn [idx word]
            (update-in idx [(str/lower-case word) (.getName file) section] (fnil inc 0)))
          index
          ;; TODO can this be improved? (What other characters should we include?)
          ;; TODO remove 's instead? (or remove "s" after splitting)
          ;; TODO Is this the correct way to handle hyphenated words?
          ;; TODO what about numbers? (remove, e.g. 200g)
          (->> (re-seq #"[\w-']+" line)
               (map strip-ears)
               (remove #(< (count %) 2))
               (remove sw/stop-words)
               add-plurals)))

(defn- index-file
  "TODO"
  ;; TODO ^File ?
  [index file]
  ;; TODO do we want to use .listFiles in build-index, then we need to get the name, or .list?
  ;; (Does using a File instead of the filename help with testing?)
  (with-open [rdr (io/reader file)]
    (first (reduce (fn [[idx section] line]
                     ;; TODO put these in a map? (and do Introduction: -> :introduction ?)
                     (condp = line
                       "Introduction:" [idx :introduction]
                       "Ingredients:" [idx :ingredients]
                       "Method:" [idx :method]
                       [(index-line idx file section line) section]))
                   [index :title]
                   (line-seq rdr)))))

;; TODO move to separate ns? (E.g. index?)
(defn build-index
  "TODO"
  []
  (let [recipe-files (.listFiles (io/file "resources/recipes"))]
    ;; TODO store this in an atom? (Or can we do it without one?
    ;; (i.e. pass to a read-input function, but that might not work if we wanted to make this multi-threaded))
    (reduce (fn [idx file]
              (index-file idx file))
            {}
            recipe-files)))

(defn- sorted-map-by-value
  "TODO"
  [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defn- calculate-score
  "TODO"
  [counts]
  (reduce-kv (fn [sum section count]
               (+ sum (* count (section-weightings section))))
             0
             counts))

;; TODO how much slower is it to use a sorted map here?
(defn- score-term
  "TODO"
  [index term]
  (into {} (map (fn [[id counts]]
                  [id (calculate-score counts)])
                (index term))))

;; TODO move to separate ns? (E.g. search?)
(defn search
  "TODO"
  [index query]
  ;; TODO what's the best way to split words? (What if we just use (str/split query #"\s+") ?)
  (let [terms (re-seq #"[\w-']+" query)
        candidates (->> terms
                        (map (partial score-term index))
                        (apply merge-with +))]
    (->> candidates
         sorted-map-by-value
         keys
         (take 10))))

(comment

  (def small-index {"cheese" {"foo.txt" {:title 1 :ingredients 1 :introduction 1 :method 3}
                              "bar.txt" {:title 1 :ingredients 1 :introduction 1 :method 10}}
                    "pasta" {"foo.txt" {:title 1 :ingredients 1 :introduction 1 :method 5}
                             "bar.txt" {:title 1 :ingredients 1 :introduction 1 :method 2}}
                    "pesto" {"baz.txt" {:title 0 :ingredients 1 :introduction 0 :method 1}}})

  (time (def big-index (build-index)))

  (score-term small-index "cheese")
  (score-term big-index "cheese")

  (search small-index "cheese pasta")
  (search big-index "cheese pasta")
  (time (search big-index "broccoli stilton soup"))

  )
