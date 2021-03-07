# Types and Data Structures

## Literal

```clojure
(or (number? arg)
    (string? arg)
    (char? arg)
    (nil? arg))
```
## Hiccup

```clojure
(and (vector? form)
     (or (keyword? (first form))
         (string? (first form))))
```

## Node

### Predicate

```clojure
(and (map? arg)
     (= (:type arg) :node))
```

### Example
```clojure
{
    :type :node ;; <keyword>
    :op "+"     ;; <string>
    :id 1       ;; <int>
    :options {} ;; <hashmap>
    :args []    ;; vector of node arguments, either literals or other nodes
}
```

## Connection

## Parse-Context
