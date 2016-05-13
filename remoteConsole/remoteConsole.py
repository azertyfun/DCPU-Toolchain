#!/usr/bin/env python3

import sys
import curses
import socket
import json

colors = [curses.COLOR_BLACK, curses.COLOR_BLUE, curses.COLOR_GREEN, curses.COLOR_CYAN, curses.COLOR_RED, curses.COLOR_MAGENTA, curses.COLOR_RED, curses.COLOR_CYAN, curses.COLOR_WHITE, curses.COLOR_RED, curses.COLOR_GREEN, curses.COLOR_CYAN, curses.COLOR_RED, curses.COLOR_RED, curses.COLOR_YELLOW, curses.COLOR_WHITE]

def init_curses():
    stdscr = curses.initscr()
    curses.noecho()
    curses.raw()
    curses.start_color()
    curses.curs_set(False)
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
    stdscr.clear()

    while True:
        # We can't make a receive of 12*32*2 bytes at once because a bytearray doesn't work after 512 items for some reason.
        j = ""
        while True:
            b = socket.recv(1)
            if b == b'\x04':
                sys.exit()
            elif b == b'\x03':
                break
            else:
                j += b.decode('utf-8')

        pairs = []
        pairPointer = 1

        stdscr.move(1, 1)

        data = json.loads(j)

        for i in range(0, 12*32):
            d = data["ram"][i]

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

            if c >= 0x20 and c < 0x7F:
                c = chr(c)
            else:
                c = ' '

            stdscr.addstr(c, curses.color_pair(pair[0]))
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

        stdscr.addstr(13, 0, "A: " + str(data["registers"][0]) + ", B: " + str(data["registers"][1]) + ", C: " + str(data["registers"][2]) + ", X: " + str(data["registers"][3]) + ", Y: " + str(data["registers"][4]) + ", Z: " + str(data["registers"][5]) + ", I: " + str(data["registers"][6]) + ", J: " + str(data["registers"][7]))
        stdscr.addstr(14, 0, "PC: " + str(data["pc"]) + ", SP: " + str(data["sp"]) + ", EX: " + str(data["ex"]) + ", IA: " + str(data["ia"]))

        stdscr.refresh()

finally:
    free_curses()
