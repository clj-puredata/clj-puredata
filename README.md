# clj-puredata

clj-puredata generates [PureData](https://puredata.info/) patches using [Clojure](https://clojure.org/).
It supports live reloading, has a simple layouter that automatically arranges the generated patches

Check the [documentation](doc/index.md), especially the [Tutorial](doc/tutorial.md).

## Installation

Using Leiningen/Boot: `[clj-puredata "0.2.0-SNAPSHOT"]`

## Usage

This will create a `counter.pd` patch, use it in a `usage.pd` patch, and start PureData with that patch.
Whenever you change and re-evaluage the `write-patch-reload` form, the patch will be updated in PureData as well.

```clojure
(ns example.core
  (:require [clj-puredata.core :refer :all]))
    
(write-patch "counter.pd"
             [:outlet [:float {:name 'the-float}
                       0
                       [:b [:inlet]]
                       [:+ 1 (other 'the-float)]]])

(write-patch-reload "usage.pd"
                    [:print "count"
                     ["counter.pd"
                      [:msg "bang"]]])

(startup "usage.pd")
```

Check the [Documentation](doc/intro.md), especially the [Tutorial](doc/tutorial.md).


## Roadmap

- [x] live reloading of newly generated patches into PureData
- [x] parsing hiccup syntax.
- [x] writing of PureData patch format.
- [x] layout engine: uses ascii dims, maybe nicer with image dims?
- [x] layout engine: don't overwrite nodes that already have :x or :y set.
- [x] layout engine: push auto-layouted nodes down (so they're outside the graph-on-parent frame).
- [x] missing node types: float, symbol, sliders/buttons etc.
- [x] finish patch footer template for "graph on parent" options.
- [x] patches (e.g. ["patch.pd" ...] should be recognized).
- [x] remove the need for explicit PD anywhere - make it implicit in WITH-PATCH, INLET, OUTLET, OTHER.
- [ ] subpatches (needs multiple parsing contexts? e.g. map of context instead of single atom)
- [ ] helpers for coloring ui-nodes (copy from cl-pd)
- [ ] `connect` function & supporting data structure for adding connections after node creation
- [ ] layouter should sort node arguments by inlet number (instead of argument position)
- [ ] DEBUG: do `inlet` and `outlet` work on `other`???
- [ ] add support for `:left`, `:right`, `:first`, `:second`, etc. as arguments to `inlet` or `outlet`


## License

Copyright © 2021 Philipp Dikmann

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
