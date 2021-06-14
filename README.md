# clj-puredata

clj-puredata is a [Clojure](https://clojure.org/) library for generating [PureData](https://puredata.info/) patches.

Visit the [Homepage](https://clj-puredata.github.io/clj-puredata/) or the [Documentation](docs/index.md) for more info.

## Quick Start

This will create a `counter.pd` patch, use it in a `usage.pd` patch, and start PureData with that patch.
Whenever you change and re-evaluate the `write-patch-reload` form, the patch will be updated in PureData as well.

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
