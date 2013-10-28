# ibcm-assembler

A Clojure tool that will take symbolic IBCM code and make it executable, adding columns for memory address, symbolic instructions, and comments

## Usage

java -jar ibcm-assembler "output/file/name" symbolic/ibcm/file"

Will store output in a file with specified name and extension .ibcm

## Example

Here is an example input file, addition.sibcm (sibcm stands for symbolic ibcm file):

jmp 00A ; jump to A, these nop's are just here for future memory storage
nop
nop
nop
nop
nop
nop
nop
nop
nop
readH ; read in a value from the input
store 001 ; store this value in address 001
readH ; read in a second value
store 002 ; store in 002
readH ; read in third value
store 003 ; store that one as well
load 001 ; now load the first value into the accumulator
add 002 ; add the second value to it
add 003 ; add the third value to that sum
jmpe 015 ; if that sum equals 0, jump to B
jmp 00a ; otherwise, go back to A
load 001 ; sum was 0, now we write each of the 3 input values
writeH
load 002
writeH 002
load 003
writeH 003 ; done

If we now java -jar ibcm-assembler "addition.sibcm", we get this as output:

C00A	000	jmp 00A 	; jump to A, these nop's are just here for future memory storage
B000	001	nop	
B000	002	nop	
B000	003	nop	
B000	004	nop	
B000	005	nop	
B000	006	nop	
B000	007	nop	
B000	008	nop	
B000	009	nop	
1000	00a	readH 	; read in a value from the input
4001	00b	store 001 	; store this value in address 001
1000	00c	readH 	; read in a second value
4002	00d	store 002 	; store in 002
1000	00e	readH 	; read in third value
4003	00f	store 003 	; store that one as well
3001	010	load 001 	; now load the first value into the accumulator
5002	011	add 002 	; add the second value to it
5003	012	add 003 	; add the third value to that sum
D015	013	jmpe 015 	; if that sum equals 0, jump to B
C00A	014	jmp 00a 	; otherwise, go back to A
3001	015	load 001 	; sum was 0, now we write each of the 3 input values
1800	016	writeH
3002	017	load 002
1800	018	writeH 002
3003	019	load 003
1800	01a	writeH 003 	; done

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
