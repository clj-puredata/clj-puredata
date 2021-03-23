# List of supported Options

- [Patch Options](#patch-options)
- [Node Options](#node-options)
  - [Common Options](#common-options)
  - [Atom Options](#atom-options)
  - [UI Options](#ui-options)
  - [Bang Options](#bang-options)
  - [Toggle Options](#toggle-options)
  - [Slider Options](#slider-options)
  - [Canvas Options](#canvas-options)

[Back to Index](index.md)

## Patch Options

These options are supported as the second argument to `write-patch`, `write-patch-reload` and `patch`.

| window properties   | default |                              |
|---------------------|---------|------------------------------|
| `:x`                | 0       | window position, horizontal  |
| `:y`                | 0       | window position, vertical    |
| `:width`            | 450     | window width                 |
| `:height`           | 300     | window height                |
| `:graph-on-parent`  | false   |                              |
| `:hide-object-name` | false   |                              |
| `:view-width`       | 85      | width of visible graph area  |
| `:view-height`      | 60      | height of visible graph area |
| `:view-margin-x`    | 0       |                              |
| `:view-margin-y`    | 0       |                              |
| `:x-range-min`      | 0       |                              |
| `:x-range-max`      | 100     |                              |
| `:y-range-min`      | 1       |                              |
| `:y-range-max`      | -1      |                              |

##### Example

```clojure
(write-patch "example.pd"
  {:width 800 :height 600} ;; <-- put options here
  [:print [:msg "hello world"]])
```

## Node Options

These options are supported as the second item in [hiccup syntax](types.md#hiccup):

##### Common Options

| option | default | comment                  |
|--------|---------|--------------------------|
| `:x`   | 0       | horizontal node position |
| `:y`   | 0       | vertical node position   |

Supported in: _all_ nodes

##### Atom Options

| option            | default | comment                                      |
|-------------------|---------|----------------------------------------------|
| `:send-symbol`    | "-"     |                                              |
| `:receive-symbol` | "-"     |                                              |
| `:label-text`     | "-"     |                                              |
| `:label-pos`      | 0       | 0 is left, 1 is right, 2 is top, 3 is bottom |
| `:width`          | 5       | node width                                   |
| `:lower-limit`    | 0       |                                              |
| `:upper-limit`    | 0       |                                              |

Supported in: `floatatom`, `symbolatom`

##### UI Options

| option            | default | comment |
|-------------------|---------|---------|
| `:send-symbol`    | "empty" |         |
| `:receive-symbol` | "empty" |         |
| `:label-text`     | "empty" |         |
| `:label-x`        | 17      |         |
| `:label-y`        | 7       |         |
| `:font-family`    | 0       |         |
| `:font-size`      | 10      |         |
| `:bg-color`       | -262144 |         |
| `:fg-color`       | -1      |         |
| `:label-color`    | -1      |         |
| `:size`           | 15      |         |

Supported in: `bng`, `tgl`, `hsl`, `vsl`

##### Bang Options

| option       | default | comment |
|--------------|---------|---------|
| `:hold`      | 250     |         |
| `:interrupt` | 50      |         |
| `:init`      | 0       |         |

Supported in: `bng`

##### Toggle Options

| option           | default | comment                                                                        |
|------------------|---------|--------------------------------------------------------------------------------|
| `:init`          | 0       | is node value initialized when patch is loaded                                 |
| `:init-value`    | 0       | if 0, node is initialized to 0. else, node is initialized to `:nonzero-value`. |
| `:nonzero-value` | 1       | value emitted when toggle is on                                                |

Supported in: `tgl`

##### Slider Options

| option             | default | comment                                        |
|--------------------|---------|------------------------------------------------|
| `:width`           | 15      |                                                |
| `:height`          | 15      |                                                |
| `:bottom`          | 0       | minimum value                                  |
| `:top`             | 127     | maximum value                                  |
| `:log`             | 0       |                                                |
| `:init`            | 0       | is node value initialized when patch is loaded |
| `:default`         | 0       | saved slider position, in pixels.              |
| `:steady-on-click` | 1       |                                                |

Supported in: `hsl`, `vsl`

##### Canvas Options

| option       | default | comment |
|--------------|---------|---------|
| `:width`     | 100     |         |
| `:height`    | 60      |         |
| `:label-x`   | 20      |         |
| `:label-y`   | 12      |         |
| `:font-size` | 14      |         |
| `:bg-color`  | -233017 |         |
| `:fg-color`  | -66577  |         |

Supported in: `cnv`
