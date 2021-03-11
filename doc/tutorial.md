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

![basics](img/basics.png)

### Node Arguments

```clojure
(write-patch "arguments.pd" 
             [:pack "f f f" ; Argument position determines the connection inlet:
              [:msg 1]      ; Connected to first inlet.
              nil           ; `nil` skips an inlet.
              [:msg 3]])    ; Connected to third inlet.
```

![arguments](img/arguments.png)

### Options

Options can be passed as the second argument to both Nodes (through hiccup) and Patches (through `write-patch` etc.).
You can find a list of supported options in the [List of supported Options](options.md).
These are relevant for UI nodes like Sliders, Toggles, Bangs etc., and also contain the graphing options for subpatches.

```clojure
(write-patch "options.pd"
             {:width 200 :height 80} ; Set patch options as the second argument to `write-patch`.
             [:bng {:name 'b         ; Set node options as the second element in hiccup vectors.
                    :x 100 :y 5
                    :size 64
                    :label-text "red!"
                    :fg-color -258049}
              [:metro 500 [:loadbang]]])
```

![options](img/options.png)

### Inlets and Outlets

```clojure
(write-patch "inlet-and-outlet.pd"
             [:pack
              (inlet [:msg "second inlet"] 1)              ; Specify target inlet explicitly with `inlet` function.
              (inlet [:msg "first inlet"] 0)]
             [:print "over 9000" (outlet [:moses 9000] 1)] ; Specify target outlet explicitly with `outlet` function.
             [:+ 1 (inlet (outlet [:select 3] 1) 1)])      ; `inlet` and `outlet` can be combined.
```

![inlet and outlet](img/inlet-and-outlet.png)

### Referencing Nodes

```clojure
(write-patch "other.pd"
             [:loadbang {:name 'lb}]                      ; Giving Nodes a `:name` allows them to be referenced by `other`.
             [:print "loaded!" (other 'lb)]
             [:print "zero" (other 'm)]                   ; The named Node can be defined later, too.
             [:print "one or more" (outlet (other 'm) 1)] ; `inlet` and `outlet` also work with `other`.
             [:moses {:name 'm} 1])
```

![other](img/other.png)

### Connecting Nodes

```clojure
(write-patch "connect.pd"
             (connect [:loadbang] [:print "hello world!"])    ; Use `connect` to connect nodes explicitly.
             (connect [:moses] 1 [:pack] 1)                   ; Use 4 arguments to specify inlet and outlet ...
             (connect (outlet (inlet [:select] 1) 1) [:pack]) ; ... or use `inlet` and `outlet`.
                                                              ; Note: they are used on the originating node.
             [:msg {:name 'tik} "bang"]
             [:metro {:name 'tok} 200]
             (connect (other 'tik) (other 'tok)))             ; It works fine with `other` as well.
```

![connect](img/connect.png)

### Colors

```clojure
(let [hues 32
      size 18
      width (+ 32 (* hues size))]
  (write-patch
   "colors.pd"
   {:width width
    :view-width width
    :view-height 32 :graph-on-parent 1}
   [:msg "; all-bangs color $1 0 0"
    (inlet [:msg (color-runtime 255 0 0) [:msg "red"]] 0)       ; Use `color-runtime` for sending color values during runtime.
    (inlet [:msg (color-runtime 0 255 0) [:msg "green"]] 0)
    (inlet [:msg (color-runtime 0 0 255) [:msg "blue"]] 0)]
   (map #(vector :bng {:x (+ 16 (* % size)) :y 5
                       :receive-symbol "all-bangs"
                       :bg-color (color-file                    ; Use `color-file` for color values stored in the file.
                                  (hsl2rgb (/ % hues) 1 0.5))}) ; Helper function `hsl2rgb` is available.
        (range hues))))
```

![colors](img/colors.png)


Patch Options list
Supported Nodes
Node Options list
Live Reloading
Advanced Usage
Recursion



