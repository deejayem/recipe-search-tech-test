(ns recipe-search-tech-test.recipe-search-tech-test
  (:gen-class)
  (:require [clojure.string :as str]
            [recipe-search-tech-test.index :as index]))

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

(defn- handle-search
  "Handler function for searches."
  [index query]
  (let [results (index/search index query)]
    (if (empty? results)
      (println "No results found")
      (do (println "Top" (count results) "results:")
          (doseq [result results]
            (println result))))
    (println))
  index)

(defn- handle-build-index
  "Handler function for building an index."
  []
  (print "Building index... ")
  (flush)
  (let [index (index/build-index)]
    (println " done.\n")
    index))

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
      ;; Rebuild the index from scratch on "reindex" as it's still fairly quick
      ;; and we don't want to have to worry about checking if files have been modified.
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

  ;; See timings belong, for ten runs of each function
  ;; (not as good as using a proper benchmarking tool, but sufficient to give some idea of performance)

  (time (def an-index (index/build-index)))
  ;; Some example times on an old, slow laptop:
  ;; "Elapsed time: 925.578265 msecs"
  ;; "Elapsed time: 916.26918 msecs"
  ;; "Elapsed time: 915.391461 msecs"
  ;; "Elapsed time: 924.001262 msecs"
  ;; "Elapsed time: 919.089405 msecs"
  ;; "Elapsed time: 906.967371 msecs"
  ;; "Elapsed time: 903.788762 msecs"
  ;; "Elapsed time: 920.738829 msecs"
  ;; "Elapsed time: 907.855198 msecs"
  ;; "Elapsed time: 922.922689 msecs"

  (time (index/search an-index "broccoli stilton soup"))
  ;; Some example times on an old, slow laptop:
  ;; "Elapsed time: 1.572801 msecs"
  ;; "Elapsed time: 0.616983 msecs"
  ;; "Elapsed time: 0.707118 msecs"
  ;; "Elapsed time: 0.508767 msecs"
  ;; "Elapsed time: 0.469181 msecs"
  ;; "Elapsed time: 0.475034 msecs"
  ;; "Elapsed time: 0.641663 msecs"
  ;; "Elapsed time: 0.595414 msecs"
  ;; "Elapsed time: 0.459915 msecs"
  ;; "Elapsed time: 0.439509 msecs"

  )
