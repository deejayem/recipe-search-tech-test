(ns recipe-search-tech-test.recipe-search-tech-test
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [recipe-search-tech-test.text :as text]))

(def ^:private section-weightings
  {:title 20
   :introduction 1
   ;; Score ingredients more highly, as there is less chance of false positives here
   :ingredients 2
   :method 1})

;; The section headings are predictable, otherwise we could create a more complex map
;; with the weightings and headings for each section.
;; The map returnign will contain :title, the heading for which does not appear in the
;; recipes (as it's just the first line), but having it in the map doesn't do any harm.
(defn- get-section-headings
  "Returns a map from the section heading strings to the section keys."
  []
  (into {} (map (juxt #(-> %
                           name
                           str/capitalize
                           (str ":"))
                      identity))
        (keys section-weightings)))

(defn- index-line
  "Add the words in a line to the index, based on the current file and section (title, introduction, etc)."
  [index file section line]
  (let [filename (.getName file)]
    (reduce (fn [idx word]
              (update-in idx [(normalise word) filename] (fnil (partial + (section-weightings section)) 0)))
            index
            (-> line
                text/parse-text
                text/add-opposite-pluralities))))

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
                     [index :title]
                     (line-seq rdr))))))

;; TODO move to separate ns? (E.g. index?)
(defn build-index
  "Build an index, from all of the files in the resources/recipes directory.

  The index is a nested map, with the outer keys being in the recipes, the keys of the inner map being the filenames
  (recipe ids), and the values being the score for that word in that file, e.g.
  {\"stilton\" {\"broccoli-soup-with-stilton.txt\" 24}}"
  []
  (let [recipe-files (.listFiles (io/file "resources/recipes"))]
    (reduce (fn [idx file]
              (index-file idx file))
            {}
            recipe-files)))

(defn- sorted-map-by-value
  "Converts into a sorted map, sorted by value, and then key."
  [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

;; TODO move to separate ns? (E.g. search?)
(defn search
  "Performs a search for `query` in `index`"
  [index query]
  (let [terms (text/parse-text query)
        candidates (->> terms
                        (map index)
                        (apply merge-with +))]
    (->> candidates
         sorted-map-by-value
         keys
         (take 10))))

(defn- handle-search
  "Handler function for searches."
  [index query]
  (let [results (search index query)]
    (if (empty? results)
      (println "No results found")
      (do (println "Top" (count results) "results:")
          (doseq [result results]
            (println result))))
    (println ""))
  index)

(defn- handle-build-index
  "Handler function for building an index."
  []
  (print "Building index... ")
  (flush)
  (let [index (build-index)]
    (println " done.\n")
    index))

(defn- get-user-input
  "Reads user input from the terminal."
  []
  (str/trim (read-line)))

(defn- print-help
  "Prints the help/usage message."
  []
  (println "Please enter your command at the prompt (>)")
  (println "Commands:")
  (println "  search <query> - search for query (e.g. search broccoli stilton soup)")
  (println "  reindex - rebuilds the search index (in case new recipes have been added)")
  (println "  help - display this help message\n"))

(defn- handle-help
  "Handler function for printing the help message."
  [index]
  (print-help)
  index)

(defn- handle-invalid-input
  "Handler function for responding to invalid input."
  [index input]
  (println "Input" input "is invalid\n")
  (print-help)
  index)

(defn- prompt-user
  "Displays a prompt and returns the user input."
  ([]
   (prompt-user false))
  ([display-help?]
   (when display-help?
     (println "Welcome to Recipe Search")
     (print-help))
   (print "> ")
   (flush)
   (get-user-input)))

(defn- process-input
  "Processes the command entered by the user."
  [index input]
  (let [[_ command args] (re-matches #"(\w+)\s*(.*)" input)]
    (case command
      ;; rebuilding from scratch is quick, otherwise we could just add the new files
      "reindex" (handle-build-index)
      "search" (handle-search index args)
      "help"  (handle-help index)
      (handle-invalid-input index input))))

(defn -main
  "Application entrypoint that reads input and controls the program flow."
  [& _args]
  (loop [index (handle-build-index)
         input (prompt-user true)]
    (when-not (= "exit" input)
      (recur (process-input index input) (prompt-user)))))

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
