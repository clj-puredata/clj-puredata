# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

[Back to Index](index.md)

## Table of Contents

##### User-Facing Functions

| Function                                    | Input                                               | Output                 | Source                                             |
|---------------------------------------------|-----------------------------------------------------|------------------------|----------------------------------------------------|
| [`pd`](#PD)                                 | [hiccup](types.md#hiccup) or [node](types.md#node)  | [nodes](types.md#node) | [parse.clj#221](../src/clj_puredata/parse.clj#221) |
| [`outlet`](#outlet)                         | [hiccup](types.md#hiccup) or [node](types.md#node)  |                        |                                                    |
| [`inlet`](#inlet)                           | [hiccup](types.md#hiccup) or [node](types.md#node)  |                        |                                                    |
| [`other`](#other)                           | reference                                           |                        |                                                    |
| [`write-patch`](#write-patch)               | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |                                                    |
| [`write-patch-reload`](#write-patch-reload) | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |                                                    |
| [`startup`](#startup)                       | one or more filenames                               |                        |                                                    |
| [`load-patches`](#load-patches)             | one or more filenames                               |                        |                                                    |
| [`reload-all-patches`](#reload-all-patches) |                                                     |                        |                                                    |
| [`open-pd`](#open-pd)                       |                                                     |                        |                                                    |
| [`import-image`](#import-image)             |                                                     |                        |                                                    |

##### Internal Functions

| Function                                    | Input                                               | Output                  |
|---------------------------------------------|-----------------------------------------------------|-------------------------|
| [`lines`](#lines)                           | [nodes](types.md#node)                              | [lines](types.md#lines) |
| [`patch`](#patch)                           | [lines](types.md#lines)                             | [patch](types.md#patch) |
| [`write`](#write)                           | [patch](types.md#patch)                             | [file](types.md#file)   |

## PD

```
(pd hiccup-vector*)   -> hash-map
(pd [hiccup-vector*]) -> [hash-map*]
```

`pd` converts hiccup syntax into Nodes. If `pd` encounters a Node during parsing, it will be passed along unchanged.
`pd` is implicitly called by [`write-patch`](#write-patch) and [`write-patch-reload`](#write-patch-reload).
Nodes are assigned unique ids by `pd`, and `clj-puredata` takes care to avoid duplicate nodes.
Call it explicitly when generating nodes in functions, or to re-use nodes. (Another way to reuse nodes is [`other`](#other).)

```clojure
;; supports hiccup
(pd [:float]) => {:type :node, :op "float", :unique-id 121, :options {}, :args []}

;; passes along nodes unchanged
(pd (pd [:float]) => {:type :node, :op "float", :unique-id 122, :options {}, :args []}

;; supports nested structures
(pd [:+ [:-]]) 
=> {:type :node,
    :op "+",
    :unique-id 122,
    :options {},
    :args [{:type :node, :op "-", :unique-id 123, :options {}, :args []}]}

(defn count-number-of-nodes [x]
  (->> x lines (filter #(-> % :type (= :node))) count))

;; each node is only used once
(->> (let [a (pd [:msg 1])]
       (pd [:+ a a]))
     count-number-of-nodes)
=> 2

;; identical hiccup is still evaluated twice
(->> (let [b [:msg 1]]
       (pd [:+ b b]))
     count-number-of-nodes)
=> 3

```

## Outlet

## Inlet

## Other

## Write-Patch

## Write-Patch-Reload

## Startup

## Load-Patches

## Reload-All-Patches

## Open-Pd

Attempts to start PureData and open OSC channel for communication.

## Lines

## Patch

## Write


