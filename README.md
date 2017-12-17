# clj-puredata

FIXME: description

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar clj-puredata-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Roadmap

- [x] 0: live reloading of newly generated patches into PureData
- [ ] 1: parsing hiccup syntax.
  `[:+ [:* 2 2] 1]` should create 2 nodes and 1 connection.
  `(let [add (parse [:+ 1 2])] [:- add add])` should correctly refer to the same `add` node within the `[:- ...]` form.
- [ ] 2: writing of PureData patch format.

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
