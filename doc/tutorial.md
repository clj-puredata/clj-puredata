# Tutorial

Basic Usage - Hiccup, Writing

## Basics

### Nodes

```clojure
(write-patch "basics.pd"
             [:-]           ; Nodes are created from hiccup, which is just plain vectors.
                            ; The first item is always the node name, as a keyword.
             [:+ 1 2]       ; Literals are passed as creation arguments.
             [:msg "bang"]  ;
             [:* [:msg 3]]) ; Nesting nodes creates connections between them.
```

### Arguments

```clojure
(write-patch "arguments.pd" 
             [:pack "f f f" ; Argument position determines the connection inlet:
              [:msg 1]      ; Connected to first inlet.
              nil           ; `nil` skips an inlet.
              [:msg 3]])    ; Connected to third inlet.
```

### Inlet and Outlet

```clojure
(write-patch "inlet-and-outlet.pd"
             [:pack
              (inlet [:msg "second inlet"] 1)              ; Specify target inlet explicitly with `inlet` function.
              (inlet [:msg "first inlet"] 0)]
             [:print "over 9000" (outlet [:moses 9000] 1)] ; Specify target outlet explicitly with `outlet` function.
             [:+ 1 (inlet (outlet [:select 3] 1) 1)])      ; `inlet` and `outlet` can be combined.
```

### Other

```clojure
(write-patch "other.pd"
             [:loadbang {:name 'lb}]                      ; Giving Nodes a `:name` allows them to be referenced by `other`.
             [:print "loaded!" (other 'lb)]
             [:print "zero" (other 'm)]                   ; The named Node can be defined later, too.
             [:print "one or more" (outlet (other 'm) 1)] ; `inlet` and `outlet` also work with `other`.
             [:moses {:name 'm} 1])
```

Advanced Usage - Live Reloading
Patch Options list
Supported Nodes
Node Options list
