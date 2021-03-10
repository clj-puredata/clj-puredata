# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

## Table of Contents

##### User-Facing Functions

| Function                                    | Input                                               | Output                 | Source                                         |
|---------------------------------------------|-----------------------------------------------------|------------------------|------------------------------------------------|
| [`pd`](#PD)                                 | [hiccup](types.md#hiccup)                           | [nodes](types.md#node) | [parse.clj#221](../src/clj-puredata/parse.clj#221) |
| [`outlet`](#outlet)                         | [hiccup](types.md#hiccup) or [node](types.md#node)  |                        |                                                |
| [`inlet`](#inlet)                           | [hiccup](types.md#hiccup) or [node](types.md#node)  |                        |                                                |
| [`other`](#other)                           | reference                                           |                        |                                                |
| [`write-patch`](#write-patch)               | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |                                                |
| [`write-patch-reload`](#write-patch-reload) | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |                                                |
| [`startup`](#startup)                       | one or more filenames                               |                        |                                                |
| [`load-patches`](#load-patches)             | one or more filenames                               |                        |                                                |
| [`reload-all-patches`](#reload-all-patches) |                                                     |                        |                                                |
| [`open-pd`](#open-pd)                       |                                                     |                        |                                                |

##### Internal Functions

| Function                                    | Input                                               | Output                  |
|---------------------------------------------|-----------------------------------------------------|-------------------------|
| [`lines`](#lines)                           | [nodes](types.md#node)                              | [lines](types.md#lines) |
| [`patch`](#patch)                           | [lines](types.md#lines)                             | [patch](types.md#patch) |
| [`write`](#write)                           | [patch](types.md#patch)                             | [file](types.md#file)   |

## PD

`pd` converts hiccup syntax into a list of Nodes. This is implicitly called by [`write-patch`](#write-patch) and [`write-patch-reload`](#write-patch-reload).
The reason to use it explicitly is when re-using Nodes, because Nodes are assigned unique ids by `pd` that are recognized during further processing.
Another way to reuse nodes is [`other`](#other).

```clojure
(pd [:float])

=> {:args [], :type :node, :op "float", :unique-id 62, :options {}}

(defn count-number-of-nodes [x]
  (->> x lines (filter #(-> % :type (= :node))) count))

(->> (let [a (pd [:msg 1])]
      (pd [:+ a a])) ;; each node is only used once
     count-number-of-nodes)
=> 2

(->> (let [b [:msg 1]]
       (pd [:+ b b])) ;; identical hiccup is still evaluated twice
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


