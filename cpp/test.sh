#!/bin/bash

g++ automata.cpp -Wno-write-strings -o automat -g
./automat $1 $2
cat $1.gv | dot -Teps > $1.eps
cat $2.gv | dot -Teps > $2.eps
rm *.gv
