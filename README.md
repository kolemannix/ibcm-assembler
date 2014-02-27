# ibcm-assembler

IBCM, or Itty-Bitty-Computing-Machine, is "a machine language designed specifically to be taught to undergraduate students", according to its creators Aaron Bloomfield and William Wulf of the University of Virginia. Unfortunately, there exists no assembler for IBCM, so programmers must manually write the actual bytecode for their programs. I have written a simple clojure tool that will take symbolic IBCM code and assemble it as an executable, adding columns for memory address, symbolic instructions, and comments

## Usage

java -jar ibcm-assembler input-file output-file

Will store output in a file with specified name and extension .ibcm.

## Example

Take a look at the example directory: addition.sibcm is a sample input file (sibcm stands for source-IBCM or symbolic-IBCM)
addition.ibcm is the output for that file.

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
