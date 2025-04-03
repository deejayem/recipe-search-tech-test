# recipe-search-tech-test

Solution to the Recipe Search test, by David Morgan

## Usage

The simplest way to run the application is to execute the following at the command line (in the repo's directory):

```
  $ clojure -X:run
```

Then you can search using `search <search query>` and exit using `exit`, as well as using the other commands
explained in the initial help message. See also the [Examples section](#Examples) below.

Alternatively, you can build and run an uberjar:

```
  $ clojure -T:build ci
  $ java -jar target/net.clojars.recipe-search-tech-test/recipe-search-tech-test-0.1.0-SNAPSHOT.jar
```

Or run `(-main)` in a REPL in the `recipe-search-tech-test` namespace, or call `index/build-index` and `index/search`
in there, as shown in the `comment` block.

## Examples

Here are some examples of searches being done in the application:

```
  > search broccoli stilton soup
  Top 10 results:
  broccoli-soup-with-stilton.txt
  broccoli-bulghur-stilton-grapes.txt
  cauliflower-stilton-soup.txt
  broccoli-soup-with-gorgonzola.txt
  purple-sprouting-broccoli-bean-and-pasta.txt
  broccoli-bean-pasta-soup.txt
  toasted-broccoli-with-sesame-garlic-soy.txt
  orange-baked-chicken-with-griddled-purpl.txt
  curried-broccoli-quinoa.txt
  chimichurri-steak-with-potatoes-psb.txt

  > search pithivier
  Top 2 results:
  squash-chard-stilton-pithivier.txt
  leek-and-smoked-cheese-pithivier.txt

  > search emacs
  No results found
```

## Performance

On my laptop (an i5 from 2020!), indexing takes just under one second, and searching for "broccoli stilton soup" usually
takes 0.5-1.5ms (but sometimes slightly longer). See the `comment` block in `recipe-search-tech-test` for examples.

## Frequencies

One way that the indexing could have been implemented is to use `frequencies` (from `clojure.core`), and then the
weightings could have been applied using `update-vals`. However, when I implemented it this way, it was slightly
slower than the current version.

## Stop Words

I think it's common for search engines to remove stop words before indexing (and from search queries), so I found a list
online and did the same.

## Plurals

A real search engine would handle this much better (using stemming or lemmatization), but the naive approach taken here
should still improve how relevant the results are. It works by guessing if words are singular or plural, based on the
presence or absence of an 's' at the end. Then singular words are made plural by adding an 's', and plural words are
made singular by removing the 's'. Both words are then indexed. This means that if I search for "carrots", then a
recipe with "Carrot Soup" in the title will score well. It does mean that nonexistent words will be added to the index,
but they shouldn't cause any problems.

## Weightings

I decided to give the highest score to words found in the title (as these ought to be the most important), and also to
score words found in the ingredients slightly higher than words found in the other parts of the body. I did this because
I thought there may be false positives elsewhere. E.g. I can imagine an introduction that says "Are you fed up of having
sandwiches for lunch? Then try this amazing soup!" or a method that says "Peel the carrots with a potato peeler." In
these two examples, sandwiches and potatoes are not relevant to the recipes.

## Updating the index
I decided that trying to update the index should just build it again from scratch. It's straightforward to pull the
filenames out of the existing index (`(->> an-index vals (mapcat keys) set)`), and then filter the list of files using
this set. That means that we could pass an existing index to the `build-index` function, and only index new files.
However, that wouldn't take into account that existing files may have been modified, so keeping things simple seemed
best.

