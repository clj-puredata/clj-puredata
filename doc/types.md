# Types and Data Structures

## Literal

Numbers, Strings, Chars and NIL are considered literals.
When literals are used as arguments in Nodes, they are passed (mostly) unchanged into PureData file format.
(Exception: `nil` is used to skip a positional argument.)

##### Predicate

```clojure
(or (number? arg)
    (string? arg)
    (char? arg)
    (nil? arg))
```

## Hiccup

Hiccup markup is used to create PureData nodes using [`pd`](functions.md#pd).

##### Predicate

```clojure
(and (vector? form)
     (or (keyword? (first form))
         (string? (first form))))
```

## Node

Nodes are created from hiccup markup by the `pd` command.

##### Predicate

```clojure
(and (map? arg)
     (= (:type arg) :node))
```

##### Content

```clojure
{
    :type :node    ;; <keyword>
    :op "+"        ;; <string>
    :id 1          ;; <int>, assigned when being put into context by `in-context`
    :unique-id 123 ;; <int>, generated from global counter when `pd`
    :options {}    ;; <hashmap>
    :args []       ;; vector of node arguments, either literals or other nodes
}
```

## Connection

##### Content

```clojure
{
    :type :connection
    :from-node {
        :id <int>
        :outlet <int>
    }
    :to-node {
        :id <int>
        :inlet <int>
    }
}
```

## Context

```clojure
{
    :current-node-id 0     ;; <int>
    :lines []              ;; <vector> of type Node and Connection
    :processed-node-ids {} ;; keys are <int> (`:unique-id` of Node), values are <int> (`:id` of Node)
}
```
