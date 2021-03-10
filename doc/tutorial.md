# Tutorial

Basic Usage - Hiccup, Writing

## Basics

### Nodes

```clojure
(write-patch-reload "basics.pd"
                    [:-]           ; Nodes are created from hiccup, which is just plain vectors.
                                   ; The first item is always the node name, as a keyword.
                    [:+ 1 2]       ; Literals are passed as creation arguments.
                    [:msg "bang"]  ;
                    [:* [:msg 3]]) ; Nesting nodes creates connections between them.
```

### Arguments

```clojure
(write-patch-reload "arguments.pd" 
                    [:pack "f f f" ; Argument position determines the connection inlet:
                     [:msg 1]      ; Connected to first inlet.
                     nil           ; `nil` skips an inlet.
                     [:msg 3] ])   ; Connected to third inlet.
```

### Inlet and Outlet

```clojure
(write-patch-reload "inlet-and-outlet.pd"
                    [:pack "f f f"
                     (inlet [:msg 2] 2)         ; Specify target  outlet explicitly with `outlet` function
                     (inlet [:msg 0] 0)
                     (inlet [:msg 1] 1)]
                    [:print "over 9000"
                     (outlet [:moses 9000] 1)]) ; Specify target  outlet explicitly with `outlet` function
```


Advanced Usage - Live Reloading
Patch Options list
Supported Nodes
Node Options list
