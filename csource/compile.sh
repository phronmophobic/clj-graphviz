#!/bin/bash


clang example.c -I/opt/local/include/graphviz -o example -lcgraph -lgvc -L /opt/local/lib/
