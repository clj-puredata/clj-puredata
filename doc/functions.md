# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

| input                                               | output                  | function                                                                   | internal |
|-----------------------------------------------------|-------------------------|----------------------------------------------------------------------------|----------|
| [hiccup](types.md#hiccup)                           | [nodes](types.md#node)  | [`pd`](#PD)                                                                |          |
| [hiccup](types.md#hiccup) or [nodes](types.md#node) | [file](types.md#file)   | [`write-patch`](#write-patch), [`write-patch-reload`](#write-patch-reload) |          |
| [nodes](types.md#node)                              | [lines](types.md#lines) | [`lines`](#lines)                                                          | yes      |
| [lines](types.md#lines)                             | [patch](types.md#patch) | [`patch`](#patch)                                                          | yes      |
| [patch](types.md#patch)                             | [file](types.md#file)   | [`write`](#write)                                                          | yes      |

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
