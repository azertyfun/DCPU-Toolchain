#!/usr/bin/env python3

import sys
import curses
import socket

colors = [curses.COLOR_BLACK, curses.COLOR_BLUE, curses.COLOR_GREEN, curses.COLOR_CYAN, curses.COLOR_RED, curses.COLOR_MAGENTA, curses.COLOR_RED, curses.COLOR_CYAN, curses.COLOR_WHITE, curses.COLOR_RED, curses.COLOR_GREEN, curses.COLOR_CYAN, curses.COLOR_RED, curses.COLOR_RED, curses.COLOR_YELLOW, curses.COLOR_WHITE]

def init_curses():
    stdscr = curses.initscr()
    curses.noecho()
    curses.raw()
    curses.start_color()
    stdscr.keypad(True)

    stdscr.timeout(0)

    return stdscr

def free_curses():
    curses.nocbreak()
    stdscr.keypad(False)
    curses.echo()
    curses.endwin()

def init_networking():
    mySocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    mySocket.connect((sys.argv[1], int(sys.argv[2])))
    return mySocket

if len(sys.argv) != 3:
    print("Error: two arguments needed.")
    print("Usage:\n./remoteConsole.py <address> <port>")
    sys.exit()

stdscr = init_curses()
try:
    socket = init_networking()
    while True:

        # We can't make a receive of 12*32*2 bytes at once because a bytearray doesn't work after 512 items for some reason.
        data = []
        for i in range(0, 12*32*2):
            b = socket.recv(1)
            data.append(b[0])

        stdscr.clear()

        pairs = []
        pairPointer = 1

        for i in range(0, 12*32):
            try:
                d = (data[i * 2] << 8) | data[i * 2 + 1]
            except IndexError:
                stdscr.addstr("error at index " + str(i))

            if i % 32 == 0 and i != 0:
                stdscr.addstr("\n");

            c = d & 0x7F
            blink = (d >> 7) & 1
            color = (d >> 8) & 0xFF
            #stdscr.addstr("Color: " + str(colors[color >> 4]) + ", " + str(colors[color & 0xF]));
            #stdscr.getch()
            pair = [pairPointer, colors[color >> 4], colors[color & 0xF]]
            foundPair = False
            for p in pairs:
                if p[1] == pair[1] and p[2] == pair[2]:
                    pair[0] = p[0]
                    foundPair = True
                    break

            if not foundPair:
                pairs.append(pair)
                pairPointer = pairPointer + 1
                try:
                    curses.init_pair(pair[0], pair[1], pair[2])
                except curses.error:
                    stdscr.addstr("pairPointer: " + str(pairPointer))
                    stdscr.getch()

            stdscr.addstr(chr(c), curses.color_pair(pair[0]))
            #stdscr.getch()

        c = stdscr.getch()
        if c != curses.ERR:
            b = bytearray()
            b.append(c)
            b.append(0)
            socket.send(b)
            if c == 3:
                free_curses()
                break

        stdscr.refresh()

finally:
    free_curses()
