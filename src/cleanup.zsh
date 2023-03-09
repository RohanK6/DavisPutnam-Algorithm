#!/bin/zsh

# Remove all .class files from subdirectories
find . -name "*.class" -delete

# Remove BackEndOutput.txt, FrontEndOutput.txt, and DavisPutnamOutput.txt
rm BackEndOutput.txt FrontEndOutput.txt DavisPutnamOutput.txt

cd FrontEnd/
rm BackEndOutput.txt FrontEndOutput.txt DavisPutnamOutput.txt

