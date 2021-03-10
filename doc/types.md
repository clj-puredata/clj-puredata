# Types and Data Structures

## Table of Contents

| User-Facing Types     | Example                     |
|-----------------------|-----------------------------|
| [Literals](#Literals) | `123`, `"bang"`, `x`, `nil` |
| [Hiccup](#Hiccup)     | `[:float 3 [:msg "bang"]]`  |

| Internal Types            | Example                                                                          |
|---------------------------|----------------------------------------------------------------------------------|
| [Node](#Node)             | `{:args ["bang"], :type :node, :op "msg", :unique-id 76, :options {}}`           |
| [Connection](#Connection) | `{:type :connection, :from-node {:outlet 0, :id 1}, :to-node {:id 0, :inlet 0}}` |
| [Lines](#Lines)           | `[<Node> \| <Connection>, ...]`                                                  |
| [Context](#Context)       |                                                                                  |
| [File](#File)             | `"#N canvas 0 0 450 300 10;\n#X obj 5 5 +;"`                                     |

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

[back to top](#table-of-contents)

## Hiccup

Hiccup markup is used to create PureData nodes using [`pd`](functions.md#pd).
The syntax is `[operator options? arguments*]`:

| position    | type                   | example                     | how many   |
|-------------|------------------------|-----------------------------|------------|
| `operator`  | keyword                | `:+`                        | exactly 1  |
| `options?`  | map                    | `{:x 10}`                   | 1 or none  |
| `arguments` | literal or more hiccup | `123`, `"bang"`, `[:- 3 4]` | any number |

Or, shown differently:

```
<hiccup> ::= "[" <operator> " " <options-maybe> <arguments-maybe> "]"
<options-maybe> ::= <options> | ""
<options> ::= "{" <key-value-pair-maybe> "}"
<key-value-pair-maybe> ::= <key-value-pair> | <key-value-pair> <key-value-pair-maybe> | ""
<key-value-pair> ::= <key> " " <value>
<key> ::= ":" <string>
<value> ::= <literal>
<arguments-maybe> ::= <argument> | <argument> <arguments-maybe> | ""
<argument> ::= <hiccup> | <literal>
```

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
[back to top](#table-of-contents)

## Node

Nodes are created from hiccup markup by [`pd`](functions.md#pd). They are further processed by [`lines`](functiond.md#lines).

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

[back to top](#table-of-contents)

## Connection

Connections are created by `walk-node-args!` and added to output of [`lines`](functions.md#lines) whenever two nodes that are connected are found during parsing.

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
(last (lines (pd [:float [:msg]])))
    
=> {:type :connection,
    :from-node {:outlet 0, :id 1},
    :to-node {:id 0, :inlet 0}}
```

[back to top](#table-of-contents)

## Lines

Lines are a sorted vector consisting of [Nodes](#node) and [Connections](#connection). They are produced by [`lines`](functions.md#lines) and consumed by [`patch`](functions.md#patch).

##### Example

```clojure
(lines (pd [:+ [:-]]))

=> [{:args [], :type :node, :op "+", :col 0, :id 0, :unique-id 1, :options {:y 45, :x 5}, :auto-layout true, :row 1}
    {:args [], :type :node, :op "-", :col 0, :id 1, :unique-id 2, :options {:y 5, :x 5}, :auto-layout true, :row 0}
    {:type :connection, :from-node {:outlet 0, :id 1}, :to-node {:id 0, :inlet 0}}]
```

[back to top](#table-of-contents)

## Context

Context is created temporarily by `lines`, and discarded after it has finished. It might see future use when supporting subpatches (e.g. nested patches).

##### Content

```clojure
{
    :current-node-id 0     ;; <int>
    :lines []              ;; <vector> of type Node and Connection
    :processed-node-ids {} ;; keys are <int> (`:unique-id` of Node), values are <int> (`:id` of Node)
}
```

[back to top](#table-of-contents)

## Patch

##### Example

A patch is represented internally as the list of Strings that make up the [File](#File) describing the patch when concatenated.

```clojure
(patch "example.pd" {} (lines (pd [:+])))

=> ("#N canvas 0 0 450 300 10;" "#X obj 5 5 +;")
```

[back to top](#table-of-contents)

## File

File is the actual file on disk, and also the return value of `write`, `write-patch` and `write-patch-reload`.
As a return value, it is just the file contents as a single String.

##### Example

```clojure
(write "example.pd" (patch "example.pd" {} (lines (pd [:+]))))

=> "#N canvas 0 0 450 300 10;\n#X obj 5 5 +;"

```

[back to top](#table-of-contents)
