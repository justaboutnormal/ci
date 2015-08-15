# ci

CI is a set of practice problems from 
[Programming Collective Intelligence: Building Smart Web 2.0 Applications](https://smile.amazon.com/gp/product/0596529325)\*. 
The original examples in the book were written in Python and I've attempted to convert them into idiomatic Clojure. My primary 
motive is to gain feedback regarding the quality of the Clojure code written. This is not an application, a library, 
nor full-featured.

Currently included problems:
  * [Product Recommendation Engine](src/ci/recommendations.clj) pages 29-41 - recommendations.clj
    * [tests](test/ci/recommendations_test.clj)

## Usage

Make sure Leiningen is installed.

From command prompt in the project directory.

    $ lein test

## Disclaimer

These are examples converted from Toby Segaran's book *Programming Collecive Intelligence*\*. I do not assert a 
Copyright for this reason; despite that the examples are not directly copied&mdash;they are converted.

\* *Programming Collecive Intelligence* by Toby Segaran. Copyright © 2007 Toby Segaran, 978-0-596-52932-1.