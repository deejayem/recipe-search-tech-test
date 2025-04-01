(ns recipe-search-tech-test.recipe-search-tech-test
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [recipe-search-tech-test.stop-words :as sw]))

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

(defn- normalise
  "TODO"
  [word]
  (-> word
      str/lower-case
      ;; remove ' from the beginning or end of a word, and 's from the end
      ;; (' should be the only non-alphanumeric character left after splitting)
      (str/replace #"^'|'$|'s$" "")))

(defn- add-opposite-pluralities
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
  (let [filename (.getName file)]
    (reduce (fn [idx word]
              (update-in idx [(normalise word) filename section] (fnil inc 0)))
            index
            ;; TODO can this be improved? (What other characters should we include?)
            ;; TODO remove 's instead? (or remove "s" after splitting)
            ;; TODO Is this the correct way to handle hyphenated words?
            ;; TODO what about numbers? (remove, e.g. 200g)
            (->> (re-seq #"[\w-']+" line)
                 (remove #(< (count %) 2))
                 (remove sw/stop-words)
                 add-opposite-pluralities))))

(defn- index-file
  "TODO"
  [index file]
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
                  [id (calculate-score counts)]))
        (index term)))

;; TODO move to separate ns? (E.g. search?)
(defn search
  "TODO"
  [index query]
  ;; TODO what's the best way to split words? (What if we just use (str/split query #"\s+") ?)
  (let [terms (re-seq #"[\w-']+" query)
        candidates (->> terms
                        (map normalise)
                        (map (partial score-term index))
                        (apply merge-with +))]
    (->> candidates
         sorted-map-by-value
         keys
         (take 10))))

(defn- handle-search
  "TODO."
  [index query]
  ;; TODO print more nicely
  (println (search index query))
  index)

(defn- handle-build-index
  "TODO."
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
  []
  (println "Please enter your command at the prompt (>)")
  (println "Commands:")
  (println "  search <query> - search for query (e.g. search broccoli silton soup)")
  (println "  reindex - rebuilds the search index (in case new recipes have been added)")
  (println "  help - display this help message\n"))

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
      "help"  (print-help))))

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
