An extended version of [this interpreter for the Bare Bones language](https://github.com/tpoomlmly/SCBareBones).  
Bare Bones has three simple commands for manipulating a variable:
```
clear X;
incr X;
decr X;
```
...which respectively set the variable X to zero, increment it by one and decrement it by one.

The language also contains one control sequence, a simple loop:
```
while name not 0 do;
...
...
end;
```
... where name is a variable. Note that variables need not be declared before they are used and must be non-negative integers.

This extended version also adds the ability to print to the terminal, as well as create functions and use if statements.

Statements are delimited by the `;` character, and files must end in a newline.

Note that while loops must be terminated by an end statement, but they can be nested.

To run, compile Interpreter.java and then run it with the file to interpret as the first argument.