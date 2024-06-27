# CS536

The projects in this repository incrementally build a compiler for a langauge called **Base**. **Base** is derived from a small subset of a C++ like language.

The complete (and correct) technical specification in terms of a context free grammar for **Base** can be
found in [Project 4/base.cup](https://github.com/mkpro118/CS536/blob/d813708a7c48b9c9fd2f68fba62bc83faf5395dd/Project%204/base.cup)\
[Project 3/base.cup](https://github.com/mkpro118/CS536/blob/d813708a7c48b9c9fd2f68fba62bc83faf5395dd/Project%203/base.cup) 
also contains a correct specification of the **Base** grammar, and was developed by me for P3.
The P4 version is the official solution to P3, and should considered as the official specification.

## Table of Contents

- [P1: Symbol Table](#symbol-table)
- [P2: Scanner/Lexical Analyzer](#scannerlexical-analyzer)
- [P3: Parser](#parser)
- [P4: Name Analyzer](#name-analyzer)
- [P5: Type Checker](#type-Checker)
- [P6: Code Generator](#code-generator)

## Symbol Table

Implement a Symbol Table (This is generic, and not specific to the **Base** language).

A symbol table is a data structure that stores
- The identifiers declared in the program being compiled (e.g., function and variable names)
- Information about each identifier (e.g., its type, where it will be stored at runtime).

The symbol table is implemented as a *Sequence of Maps*,
where each Map stores the identifiers declared in one scope in the program being compiled.

The Map's keys are the declared identifier names and the associated information are
symbols. At this stage, the only information in a symbol is the type of the identifier,
represented as a string (e.g., "int", "double", etc.).

#### Known bugs[^wontfix]
- The implementation has no *known* bugs, but the project lacks some test cases.[^test_bugs]

## Scanner/Lexical Analyzer

Implement a Scanner/Lexical Analyzer for the **Base** language.

The main job of the scanner is to identify and return the next token. The value to returned includes:

- The token "name" (e.g., INTLITERAL). Token names are defined in the file sym.java.
- The line number in the input file on which the token starts.
- The number of the character on that line at which the token starts.
- For identifiers, integer literals, and string literals: the actual value (a String, an int, or a String, respectively).
For a string literal, the value includes the double quotes that surround the string, as well as any backslashes
used inside the string as part of an "escaped" character.

#### Known bugs[^wontfix]
- Integer literals have incorrect line and character numbers.
- Some string literals with bad escaped characters and unterminated string are not correctly handled.

## Parser

 Implement a Parser and Unparser for the **Base** language. 

 The parser uses the tokens returned by the Scanner from [P2](#p2-scannerlexical-analyzer),
 and **Base** grammer rules to find syntax errors.
 For syntactically correct programs, it builds an abstract-syntax tree (AST) representation of the program.
 
 The unparser prints well formatted version of the program.
 The output produced by the unparses is the same as the input to the parser except that:

- There are no comments in the output.
- The output is "pretty printed" (newlines and indentation are used to make the program readable)
- Expressions are fully parenthesized to reflect the order of evaluation.

#### Known bugs[^wontfix]
- Parentheses are missing in chained assignment statements.
- Unnecessary parenthesis in while statements.

## Name Analyzer

Implement a Name Analyzer for **Base** programs represented as abstract-syntax trees.\
*This is the first part of the Static Semantic Analyzer.*

The name analyzer performs the following tasks:
- Build symbol tables. Uses the "list of hashtables" approach (using the `SymTable` class from [P1](#p1-symbol-table)).
- Find multiply declared names, uses of undeclared names, bad tuple accesses, and bad declarations.
    - Like C, **but unlike Java**, the **Base** language allows the same name to be declared in non-overlapping or nested scopes.
    - The formal parameters of a function are considered to be in the same scope as the function body.
    - All names must be declared before they are used.
    - A bad tuple access happens when either the left-hand side of the colon-access is not a name already declared
to be of a tuple type or the right-hand side of the colon-access is not the name of a field for the appropriate type of tuple.
    - A bad declaration is a declaration of anything other than a function to be of type void as well as the declaration of
a variable to be of a bad tuple type (the name of the tuple type doesn't exist or is not a tuple type).
- Add identifier links: For each identifier in the abstract-syntax tree that represents a use of a name
(not a declaration) add a "link" to the corresponding symbol-table entry.

#### Known bugs[^wontfix]
- Types are printed for declarations (should only be printed for uses)
- Throws extra errors. (I do not understand why that is a bad thing, the extra errors are still real errors in the program.)
- Some productions are untested.[^test_bugs]
- Some erroneous inputs are untested.[^test_bugs]

## Type Checker

Implement a type checker for **Base** programs represented as abstract-syntax trees.\
*This is the second part of the Static Semantic Analyzer.*

**Base** is a strict statically typed language. It does not perform any type inference, and does not support dynamic types.

The type checker will determine the type of every expression represented in the abstract-syntax tree
and will use that information to identify type errors.

In the base language we have the following types:
- `integer`[^integer], `logical`[^logical] and `void`[^void]: These are the only types supported as function return types.
- `string`[^string]: Strings are only supported in `write` statements.
- `tuple`: Tuples are like C structs. A tuple type includes the name of the tuple (i.e., when it was declared/defined)
- `function`: A function type includes the types of the parameters and the return type.

#### Known bugs[^wontfix]
- The implementation has no *known* bugs, but the project lacks some test cases.[^test_bugs]

## Code Generator

Implement a code generator that generates MIPS assembly code (suitable as input to the Spim interpreter)
for **Base** programs represented as abstract-syntax trees.

The code generator supports the following features:
- Global variable declarations, function entry, and function exit.
- Integer and logical literals string literals.
- Identifiers and assignments of the form `id=literal` and `id=id`.
- Function call and expressions, return statements.
- Expressions other than function calls.
- Statements other than function calls and returns.
- Short circuited logical operators (`&` and `|`).

#### Known bugs[^wontfix]
- Result of assignment statement is not popped off the stack by assignment statement, leading to stack overflow.
- Conditions of if/while not popped off the stack, leading to stack overflow.
- Call statement return value is not popped off stack, leading to stack overflow.

[^wontfix]: While these bugs are known, they will not be fixed to retain history.
[^test_bugs]: These are not bugs in the implementation, but the project lacks these tests for the implementation.
[^integer]: The `integer` type in **Base** is a 4-byte/32-bit signed integer. It is equivalent to the `int` type in Java, and the `int32_t` type in C.
[^logical]: The `logical` type in **Base** is equivalent to the `boolean` type in Java.
[^void]: The `void` type in **Base** is equivalent to it's namesake in Java and C, but not equivalent to the `void*` type in C.
The `void` type truly represents the absence of a value, and not a generic type.
[^string]: The `string` type in **Base** is equivalent to the `char[]` type in C and Java.
