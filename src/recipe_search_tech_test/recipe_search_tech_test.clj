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
    (println ""))
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
      ;; rebuilding from scratch is quick, otherwise we could just index the new files
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
  ;; "Elapsed time: 2453.467321 msecs"
  ;; "Elapsed time: 2437.6425 msecs"
  ;; "Elapsed time: 2471.313783 msecs"
  ;; "Elapsed time: 2447.307504 msecs"
  ;; "Elapsed time: 2458.028332 msecs"
  ;; "Elapsed time: 2436.143466 msecs"
  ;; "Elapsed time: 2422.639322 msecs"
  ;; "Elapsed time: 2467.106305 msecs"
  ;; "Elapsed time: 2520.03456 msecs"
  ;; "Elapsed time: 2489.00808 msecs"

  (time (index/search an-index "broccoli stilton soup"))
  ;; Some example times on an old, slow laptop:
  ;; "Elapsed time: 2.981324 msecs"
  ;; "Elapsed time: 1.680834 msecs"
  ;; "Elapsed time: 2.13902 msecs"
  ;; "Elapsed time: 1.454338 msecs"
  ;; "Elapsed time: 1.504322 msecs"
  ;; "Elapsed time: 1.260686 msecs"
  ;; "Elapsed time: 1.181287 msecs"
  ;; "Elapsed time: 1.180477 msecs"
  ;; "Elapsed time: 1.156572 msecs"
  ;; "Elapsed time: 1.154327 msecs"

  )
