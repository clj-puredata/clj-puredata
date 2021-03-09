# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

hiccup -> pd -> nodes

hiccup -> context -> context
nodes -> lines -> lines

hiccup -> patch -> patch
nodes -> patch -> patch
lines -> patch -> patch

hiccup -> write-patch -> nil
nodes -> write-patch -> nil
lines -> write-patch -> nil

## PD

`pd` converts hiccup syntax into lists of Nodes. 

```clojure
(pd [:float])

=> {:args [], :type :node, :op "float", :unique-id 62, :options {}}

```

## Context

## Patch

## Write-Patch

## Write-Patch-Reloading
