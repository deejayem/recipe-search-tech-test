# recipe-search-tech-test

## TODO
- Build index (need to know where in the recipe words appear: title, intro, ingredients, method)
- Search
  - How should we score it? How many points for titles vs in the body? (Does the section matter: intro vs ingredients vs methods?)
- Is stemming or lemmatisation needed?
  - Maybe only handle plurals?
  - Check words ending in 's' in the recipies to see if just removing 's' is sufficient
    - Maybe only for words that appear with and without s? (Extra step and end of indexing?)
- Should it handle synonyms? (E.g. one recipe says "cider or wine" then refers to "alcohol", others mention different types of cheese or pasta, and "meat" also appears)
  - Should there be a lower score for a synonym?
- Be able to read new recipes?
- Interface (cli? repl? api?)
- Write README/documentation (delete most of what's below here?)

FIXME: my new application.

## Installation

Download from https://github.com/recipe-search-tech-test/recipe-search-tech-test

## Usage

FIXME: explanation

Run the project directly, via `:exec-fn`:

    $ clojure -X:run-x
    Hello, Clojure!

Run the project, overriding the name to be greeted:

    $ clojure -X:run-x :name '"Someone"'
    Hello, Someone!

Run the project directly, via `:main-opts` (`-m recipe-search-tech-test.recipe-search-tech-test`):

    $ clojure -M:run-m
    Hello, World!

Run the project, overriding the name to be greeted:

    $ clojure -M:run-m Via-Main
    Hello, Via-Main!

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build an uberjar (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/net.clojars.recipe-search-tech-test/recipe-search-tech-test-0.1.0-SNAPSHOT.jar

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

