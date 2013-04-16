#!/bin/bash

g++ automata.cpp -Wno-write-strings -o automat
./automat
cat demo.gv | dot -Teps > demo.eps
