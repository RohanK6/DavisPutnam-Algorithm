# Programming Assignment 2
The following README contains instructions on compiling and running the programs, with the reccomended directories to compile from and the order in which to compile in. Note that each "output" file from the program gets piped as an input automatically into the next program. That means that you only need to run the `FrontEnd.Main` program and the both `DavisPutnam.Main` and `BackEnd.Main` will run sequentially.

## Input
The input file that is read through by the `FrontEnd.Main` program should be stored in the `FrontEnd` directory, named as `input.txt`. As mentioned above, the programs will pipe the respective outputs as inputs into the next program (i.e. FrontEnd.Main > DavisPutnam.Main and DavisPutnam.Main > BackEnd.Main).

## Outputs
The outputs from each of the programs are titled as follows:
- FrontEndOutput.txt
- DavisPutnamOutput.txt
- BackEndOutput.txt

## Scripts
There are two scripts (one to compile and one to clean) in the project folder for conveneince, but **they were used for development purposes, and may not run when testing the programs.** They are included for convenience, but I recommend following the steps to manually compile and run the programs as listed below.

## Manual Compilation
Please run the following commands, in order, from within the `src` directory.

```
cd src/

javac -cp . FrontEnd/*.java   

javac -cp . DavisPutnam/*.java

javac -cp . BackEnd/*.java
```

## How to Run
Once you've compiled and made sure that there is an `input.txt` file located in the `FrontEnd` directory, run the following command to execute all of the programs sequentially:

`java FrontEnd.Main`