# Types and Data Structures

##### User-Facing

- [Literals](#Literals)
- [Hiccup](#Hiccup)

##### Internal

- [Node](#Node)
- [Connection](#Connection)
- [Context](#Context)

## Literals

Numbers, Strings, Chars and NIL are considered literals.
When literals are used as arguments in Nodes, they are passed (mostly) unchanged into PureData file format.
    Exception: `nil` is used to skip a positional argument. Strings are escaped (notably the characters `$`, `;` and `,`) by [`to-string`](../src/clj_puredata/translate.clj#L157).

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

##### Example

```clojure
(pd [:float {:x 0 :y 0} 3 [:msg "bang"]])

=> {:args
    [3
      {:args ["bang"],
       :type :node,
       :op "msg",
       :unique-id 76,
       :options {}}],
     :type :node,
     :op "float",
     :unique-id 75,
     :options {:x 0, :y 0}}
```

## Node

Nodes are created from hiccup markup by [`pd`](functions.md#pd). They are further processed by `walk-node!` and put into a (Context)[#context] when passed to `in-context`.

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
    :id 1          ;; <int>, assigned by `walk-node!` during contextualization by `in-context`
    :unique-id 123 ;; <int>, generated from global counter when `pd`
    :options {}    ;; <hashmap>
    :args []       ;; vector of node arguments, either literals or other nodes
}
```

##### Example

```clojure
(pd [:float])

=> {:args [], :type :node, :op "float", :unique-id 71, :options {}}
```

## Connection

Connections are created by `walk-node-args!` and added to the `:lines` of a (Context)[#context] whenever two nodes that are connected are found during parsing.

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

##### Example

```clojure
(-> (in-context (pd [:float [:msg]])) 
    :lines last)
    
=> {:type :connection,
    :from-node {:outlet 0, :id 1},
    :to-node {:id 0, :inlet 0}}
```

## Context

Context is created by `in-context` for further processing in 

##### Content

```clojure
{
    :current-node-id 0     ;; <int>
    :lines []              ;; <vector> of type Node and Connection
    :processed-node-ids {} ;; keys are <int> (`:unique-id` of Node), values are <int> (`:id` of Node)
}
```

##### Example
