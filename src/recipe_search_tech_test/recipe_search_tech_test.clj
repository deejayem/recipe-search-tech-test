(ns recipe-search-tech-test.recipe-search-tech-test
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

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

;; e.g. {"cheese" [{:id "foo.txt" :title 1 :ingredients 1 :intro 1 :method 3}]}
;; or {"cheese" {"foo.txt" {:title 1 :ingredients 1 :intro 1 :method 3}}}

(defn- index-line
  "TODO"
  [index file section line]
  (reduce (fn [idx word]
            (update-in idx [(str/lower-case word) (.getName file) section] (fnil inc 0)))
          index
          ;; TODO can this be improved? (What other characters should we include?)
          ;; TODO remove 's instead? (or remove "s" after splitting)
          ;; TODO Is this the correct way to handle hyphenated words?
          (re-seq #"[\w-']+" line)))

(defn- index-file
  "TODO"
  ;; TODO ^File ?
  [index file]
  ;; TODO do we want to use .listFiles in build-index, then we need to get the name, or .list?
  ;; (Does using a File instead of the filename help with testing?)
  (with-open [rdr (io/reader file)]
    (reduce (fn [[idx section] line]
              ;; TODO put these in a map? (and do Introduction: -> :introduction ?)
              (condp = line
                "Introduction:" [idx :introduction]
                "Ingredients:" [idx :ingredients]
                "Method:" [idx :method]
                [index-line (idx file section line) section]))
            [index :title]
            (line-seq rdr))))

;; TODO move to separate ns? (E.g. index?)
(defn build-index
  "TODO"
  []
  (let [recipe-files (.listFiles (io/file "resources/recipes"))]))
