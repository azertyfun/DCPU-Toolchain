DCPU Emulator
=============

This is an emulator for the techcompliant DCPU (formerly notch's in 0x10^c).

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

Run the program with command line options `run <file> [--clock] [--keyboard] [--lem1802] [--M35FD=/path/to/file] [--M525HD=/path/to/file]`.

To stop the program, type `stop` in the console.