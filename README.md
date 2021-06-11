# clj-puredata

clj-puredata is a [Clojure](https://clojure.org/) library for generating [PureData](https://puredata.info/) patches.
It features:

- live coding / reloading
- automatic patch layouts
- support for many puredata-nodes
- increased quality of life

## Installation

Add the following to your `:dependencies`:

```
[clj-puredata "0.3.2"]
```

## Documentation

- [Tutorial](doc/tutorial.md)
- [Full Documentation Index](doc/index.md)
- API Docs (ToDo)

## Quick Start

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

- [x] add support for radio ui nodes
  - [ ] find out what the constant "1" in the `radio-nodes` template is used for
  [ ] check changes in file format for version >48
    [ ] find out what trailing `, f N` means for nodes (appears to set width to N on some nodes)
- [ ] subpatches (needs multiple parsing contexts? e.g. map of context instead of single atom)
- [ ] layouter should sort node arguments by inlet number (instead of argument position)
- [ ] add support for `:left`, `:right`, `:first`, `:second`, etc. as arguments to `inlet` or `outlet`
  (q: how? some nodes have variable # of inlets/outlets)
- [ ] make layout engine ignore any nodes that have :x or :y set (also ignore them when setting column widths or offsets).
)
- [ ] fix bug in layouter where two nodes can occupy the same position
- [ ] fix behavior in layouter where any completely circular part of a patch is pushed into the top-left corner and not layouted correctly.
- [ ] add optional arguments `:inlet`, `:outlet` to `other` -- `(other 'ref :inlet 0 :outlet 1)`
- [ ] add helper `in-out-lets` that can supply both `:inlet` and `:outlet` in one call?

## Changelog

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
- [x] DEBUG: do `inlet` and `outlet` work on `other`???
- [x] `connect` function & supporting data structure for adding connections after node creation
- [x] make `write-patch-reload` return file string as well.
- [x] helpers for coloring ui-nodes (copy from cl-pd)
- [x] Redo Tutorial for Options, use UI node with Colors for demonstration
- [x] expand resources into separate directory (e.g. `helper-patches`) instead of `./` root.
- [x] put generated patches into a `patch` subdirectory instead of `./` root.
- [x] add image-to-patch functionality.
- [x] clarify gotchas like the use of dashes in signal-nodes like `[:+-]`
- [x] fix bug in layouter that calculates node size of messages wrong (workaround: limit at 25 characters node size

## License

Copyright © 2021 Philipp Dikmann

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
