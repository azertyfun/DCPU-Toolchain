DCPU Emulator & Assembler
=========================

This is an emulator/debugger and a (buggy) assembler for the techcompliant DCPU (formerly notch's in 0x10^c).

Supported hardware
------------------

* DCPU;
* CPU-Control (IACM);
* Clock;
* Keyboard;
* LEM1802;
* EDC;
* M35FD.

Planned hardware
----------------

Everything planned by [Paul](https://github.com/paultech/TC-Specs).

How to build
------------

* Import with IntelliJ idea;
* Install org.lwjgl:lwjgl:3.0.0b from Maven;
* Download and add the 3.0.0b LWJGL natives from [the LWJGL downloads page](https://www.lwjgl.org/download);
* Enjoy!

Usage
-----

```
Usage:
  java -jar DCPU-Toolchain.jar assemble <input file> <output file> [--bootloader=/path/to/file] [--big-endian] [--disable-shortLiterals]
  java -jar DCPU-Toolchain.jar run <file> [--assemble] [--big-endian] [--rom-big-endian] [--bootloader=/path/to/file] [--debugger] [--clock] [--keyboard] [--lem1802] [--edc] [--M35FD=/path/to/file] [--M525HD=/path/to/file] [--console]

Options:
  --assemble                 The specified input file is assembly instead of binary and must be assembled at runtime.
  --big-endian               Treat binary files as big endian instead of little endian by default (does not affect res/rom.bin).
  --rom-big-endian           Treat res/rom.bin as big endian instead of little endian by default.
  --bootloader-big-endian    Treat the bootloader (set with optional --bootloader flag) as big endian instead of little endian by default.
  --bootloader=path/to/file  If assembling, adds a bootloader at the beggining of the output floppy. If not, adds a bootloader internally to run a binary that doesn't have one.
  --disable-shortLiterals    Disables optimization of short literals (-1 -> 30) to be included in the opcode instead of the next word.
  --debugger                 Enable the debugger interface.
  --clock                    Adds a clock device.
  --keyboard                 Adds a keyboard device.
  --lem1802                  Adds a LEM1802 device.
  --edc                      Adds an EDC device.
  --M35FD=path/to/file       Adds an M35FD device with a floppy stored in path/to/file.
  --M525HD=path/to/file      Adds an M525HD device with a hard disk stored in path/to/file.
  --console                  Disables debugger and EDC, and creates a server on port 25570 that can be used by remoteConsole.py to control the LEM1802 and keyboard via a remote console. Useful if you want to run the emulator headless.
```

Screenshot
----------

![Screenshot of DCPU-Toolchain](https://raw.githubusercontent.com/azertyfun/DCPU-Toolchain/master/res/screenshot.png)
