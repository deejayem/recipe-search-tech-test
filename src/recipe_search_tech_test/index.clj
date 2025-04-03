(ns recipe-search-tech-test.index
  (:require [clojure.string :as str]
            [recipe-search-tech-test.text :as text]
            [clojure.java.io :as io]))

(def ^:private section-weightings
  {:title 20
   :introduction 1
   ;; Score ingredients more highly, as there is less chance of false positives here
   :ingredients 2
   :method 1})

;;; Functions for building the index (index format is explained in the docstring of build-index)

(defn- index-line
  "Add the words in a line to the index, based on the current file and section (title, introduction, etc)."
  [index file section line]
  (let [filename (.getName file)
        update-score (partial + (section-weightings section))]
    (reduce (fn [idx word]
              (update-in idx [word filename] (fnil update-score 0)))
            index
            (-> line
                text/parse-text
                text/add-opposite-pluralities))))

;; The section headings are predictable, otherwise we could create a more complex map
;; with the weightings and headings for each section (or have two separate maps).
;; The map returned will contain :title, the heading for which does not appear in the
;; recipes (as it's just the first line), but having it in the map doesn't do any harm.
(defn- get-section-headings
  "Returns a map from the section heading text to the section keys.
  e.g. \"Introduction:\" -> :introduction"
  []
  (letfn [(section->heading [section]
            (-> (name section)
                str/capitalize
                (str ":")))]
    (into {} (map (juxt section->heading
                        identity))
          (keys section-weightings))))

(defn- index-file
  "Index a file."
  [index file]
  (with-open [rdr (io/reader file)]
    (let [section-headings (get-section-headings)]
      (first (reduce (fn [[idx section] line]
                       (if-let [new-section (section-headings line)]
                         ;; Don't index the headings, but use the new section for the next line
                         [idx new-section]
                         [(index-line idx file section line) section]))
                     [index :title] ; the first line is the title
                     (line-seq rdr))))))

(defn build-index
  "Build an index, from all of the files in the resources/recipes directory.

  The index is a nested map, with the outer keys being the words in the recipes, the keys of the inner
  map being the filenames (recipe ids), and the values being the score for that word in that file, e.g.
  {\"stilton\" {\"broccoli-soup-with-stilton.txt\" 24}}"
  []
  (let [recipe-files (.listFiles (io/file "resources/recipes"))]
    (reduce (fn [idx file]
              (index-file idx file))
            {}
            recipe-files)))

;;; Function for searching the index

(defn search
  "Performs a search for `query` in `index`"
  [index query]
  (let [terms (text/parse-text query)
        candidates (->> terms
                        (map index)
                        (apply merge-with +))]
    (->> candidates
         (sort-by second >)
         (map first)
         (take 10))))
