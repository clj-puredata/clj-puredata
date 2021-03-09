# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

## Table of Contents

##### User-Facing Functions

| Function                                    | Input                                               | Output                 | Internal |
|---------------------------------------------|-----------------------------------------------------|------------------------|----------|
| [`pd`](#PD)                                 | [hiccup](types.md#hiccup)                           | [nodes](types.md#node) |          |
| [`outlet`](#outlet)                         | [hiccup](types.md#hiccup) or [node](types.md#node)  |                        |          |
| [`inlet`](#inlet)                           | [hiccup](types.md#hiccup) or [nodes](types.md#node) |                        |          |
| [`other`](#other)                           | reference                                           |                        |          |
| [`write-patch`](#write-patch)               | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |          |
| [`write-patch-reload`](#write-patch-reload) | [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)  |          |
| [`startup`](#startup)                       | one or more filenames                               |                        |          |
| [`load-patches`](#load-patches)             | one or more filenames                               |                        |          |
| [`reload-all-patches`](#reload-all-patches) |                                                     |                        |          |
| [`open-pd`](#open-pd)                       |                                                     |                        |          |

##### Internal Functions

| Function                                    | Input                                               | Output                  | Internal |
|---------------------------------------------|-----------------------------------------------------|-------------------------|----------|
| [`lines`](#lines)                           | [nodes](types.md#node)                              | [lines](types.md#lines) | yes      |
| [`patch`](#patch)                           | [lines](types.md#lines)                             | [patch](types.md#patch) | yes      |
| [`write`](#write)                           | [patch](types.md#patch)                             | [file](types.md#file)   | yes      |



## PD

`pd` converts hiccup syntax into lists of Nodes. 

```clojure
(pd [:float])

=> {:args [], :type :node, :op "float", :unique-id 62, :options {}}

```

## Lines

## Patch

## Write

## Write-Patch

## Write-Patch-Reload
