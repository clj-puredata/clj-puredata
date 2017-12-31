# clj-puredata

FIXME: description

## Roadmap

- [x] live reloading of newly generated patches into PureData
- [x] parsing hiccup syntax.
- [x] writing of PureData patch format.
- [ ] layout engine: uses ascii dims, maybe nicer with image dims?
- [ ] layout engine: don't overwrite nodes that already have :x or :y set.
- [ ] missing node types: float, symbol, sliders/buttons etc.
- [ ] finish patch footer template for "graph on parent" options.
- [ ] patches (e.g. ["patch.pd" ...] should be recognized).
- [ ] subpatches (needs multiple parsing contexts? e.g. map of context instead of single atom)

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
