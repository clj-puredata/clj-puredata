# Functions

This explains the main functions in detail. For a tutorial covering their interplay, see the [Tutorial](tutorial.md).

| input           | output | function             | internal |
|-----------------|--------|----------------------|----------|
| hiccup          | nodes  | `pd`                 |          |
| hiccup or nodes | file   | `write-patch`        |          |
|                 |        | `write-patch-reload` |          |
| nodes           | lines  | `lines`              | yes      |
| lines           | patch  | `patch`              | yes      |
| patch           | file   | `write`              | yes      |

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
