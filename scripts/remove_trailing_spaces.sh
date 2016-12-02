#!/bin/bash

find . \( -name '*.h' -o -name '*.c' -o -name '*.java' \) -print0 | xargs -i -r -0 sed -r -i 's/\s*$//' {}
