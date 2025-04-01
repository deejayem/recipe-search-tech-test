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
- How should short words be handled? (i.e. 1-2 characters) Can they just be ignored? (Check data)
  - grep -ohe '\b\w\w\b' * |tr '[A-Z]' '[a-z]'| sort -u
  - Words like "ox" appear in recipes, so we can't just exclude two letter words
  - Excluding one letter words should be okay
  - Create list of words to exclude? (Or does it not matter if these are in the index?)
- Be able to read new recipes?
- Interface (cli? repl? api?)
- Write README/documentation (delete most of what's below here?)

FIXME: my new application.

## Installation

Download from https://github.com/recipe-search-tech-test/recipe-search-tech-test

## Usage

The simplest way to run the application is to execute the following, in the project directory:

  $ clojure -X:run

Alternatively, you can build and run an uberjar:

  $ clojure -T:build ci
  $ java -jar target/net.clojars.recipe-search-tech-test/recipe-search-tech-test-0.1.0-SNAPSHOT.jar

Or run `(-main)` in the `recipe-search-tech-test` namespace.

                                     If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

