# List of supported Nodes

These are defined in [translate.clj](../src/clj_puredata/translate.clj#L13).

## General

`bang`
`b`
`float`
`f`
`symbol`
`int`
`i`
`send`
`s`
`receive`
`r`
`select`
`route`
`pack`
`unpack`
`trigger`
`t`
`spigot`
`moses`
`until`
`print`
`makefilename`
`change`
`swap`
`value`
`v`
`list`


## Time

`delay`
`metro`
`line`
`timer`
`cputime`
`realtime`
`pipe`


## Math

`+`
`-`
`*`
`/`
`pow`
`==`
`!=`
`>`
`<`
`>=`
`<=`
`&`
`&&`
`||`
`||||`
`%`
`<<`
`>>`
`mtof`
`powtodb`
`rmstodb`
`ftom`
`dbtopow`
`dbtorms`
`mod`
`div`
`sin`
`cos`
`tan`
`atan`
`atan2`
`sqrt`
`log`
`exp`
`abs`
`random`
`max`
`min`
`clip`
`wrap`

## MIDI and OSC

`notein`
`ctlin`
`pgmin`
`bendin`
`touchin`
`polytouchin`
`midiin`
`sysexin`
`noteout`
`ctlout`
`pgmout`
`bendout`
`touchout`
`polytouchout`
`midiout`
`makenote`
`stripnote`
`oscparse`
`oscformat`

## Arrays and Tables

`tabread`
`tabread4`
`tabwrite`
`soundfiler`
`table`
`array`

## Miscellaneous

`loadbang`
`serial`
`netsend`
`netreceive`
`qlist`
`textfile`
`openpanel`
`savepanel`
`bag`
`poly`
`key`
`keyup`
`keyname`
`declare`

## Audio Math

`+~`
`-~`
`*~`
`/~`
`max~`
`min~`
`clip~`
`q8_rsqrt~`
`q8_sqrt~`
`sqrt~`
`wrap~`
`fft~`
`ifft~`
`rfft~`
`rifft~`
`pow~`
`log~`
`exp~`
`abs~`
`framp~`
`mtof~`
`ftom~`
`rmstodb~`
`dbtorms~`

## Audio Manipulation    

`dac~`
`adc~`
`sig~`
`line~`
`vline~`
`threshold~`
`snapshot~`
`vsnapshot~`
`bang~`
`samplerate~`
`send~`
`receive~`
`throw~`
`catch~`
`block~`
`switch~`
`readsf~`
`writesf~`

## Audio Oscillators and Tables

`phasor~`
`cos~`
`osc~`
`tabwrite~`
`tabplay~`
`tabread~`
`tabread4~`
`tabosc4~`
`tabsend~`
`tabreceive~`

## Audio Filters

`vcf~`
`noise~`
`env~`
`hip~`
`lop~`
`bp~`
`biquad~`
`samphold~`
`print~`
`rpole~`
`rzero~`
`rzero_rev~`
`cpole~`
`czero~`
`czero_rev~`

## Audio Delay

`delwrite~`
`delread~`
`vd~`
                    
## Subwindows

`inlet`
`outlet`
`inlet~`
`outlet~`

## Data Templates

`struct`
`drawcurve`
`filledcurve`
`drawpolygon`
`filledpolygon`
`plot`
`drawnumber`

## Accessing Data

`pointer`
`get`
`set`
`element`
`getsize`
`setsize`
`append`
`scalar`

## Extras

`sigmund~`
`bonk~`
`choice`
`hilbert~`
`complex-mod~`
`expr~`
`expr`
`fexpr~`
`loop~`
`lrshift~`
`pd~`
`rev1~`
`rev2~`
`rev3~`
`bob~`

## Others

`msg`
`text`
`floatatom`
`symbolatom`
`bng`
`tgl`
`hsl`
`vsl`
