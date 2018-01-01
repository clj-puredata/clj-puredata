# clj-puredata

FIXME: description

## Roadmap

- [x] live reloading of newly generated patches into PureData
- [x] parsing hiccup syntax.
- [x] writing of PureData patch format.
- [x] layout engine: uses ascii dims, maybe nicer with image dims?
- [x] layout engine: don't overwrite nodes that already have :x or :y set.
- [x] layout engine: push auto-layouted nodes down (so they're outside the graph-on-parent frame).
- [ ] missing node types: float, symbol, sliders/buttons etc.
- [x] finish patch footer template for "graph on parent" options.
- [x] patches (e.g. ["patch.pd" ...] should be recognized).
- [ ] subpatches (needs multiple parsing contexts? e.g. map of context instead of single atom)
- [ ] remove the need for explicit PD anywhere - make it implicit in WITH-PATCH, INLET, OUTLET, OTHER.

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar clj-puredata-0.1.0-standalone.jar [args]

## Examples

...

### Bugs

...

## License

Copyright © 2017 Philipp Dikmann

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
