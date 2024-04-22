###
# This Makefile can be used to make a parser for the base language
# (parser.class) and to make a program (P5.class) that tests the 
# parser and the unparse and name-analysis methods in ast.java.
#
# make clean removes all generated files
#
###

JC = javac
TARGET_DIR := bin
DEPENDENCY_DIR := deps
TESTS_DIR := tests

FLAGS = -g -d $(TARGET_DIR)

ifeq ($(OS),Windows_NT)
	CP = $(DEPENDENCY_DIR);$(TARGET_DIR)
	PY = py
else
	CP = ./$(DEPENDENCY_DIR):./$(TARGET_DIR)
	PY = python3
endif

P5.class: P5.java parser.class Yylex.class ASTnode.class
	$(JC) $(FLAGS) -cp $(CP) P5.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) $(FLAGS) -cp $(CP) parser.java

parser.java: base.cup
	java -cp $(CP) java_cup.Main < base.cup

Yylex.class: base.jlex.java sym.class ErrMsg.class
	$(JC) $(FLAGS) -cp $(CP) base.jlex.java

ASTnode.class: ast.java Type.java SymTable.class
	$(JC) $(FLAGS) -cp $(CP) ast.java

base.jlex.java: base.jlex sym.class
	java -cp $(CP) JLex.Main base.jlex

sym.class: sym.java
	$(JC) $(FLAGS) -cp $(CP) sym.java

sym.java: base.cup
	java -cp $(CP) java_cup.Main < base.cup

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) -cp $(CP) ErrMsg.java

Sym.class: Sym.java Type.class ast.java
	$(JC) $(FLAGS) -cp $(CP) Sym.java

SymTable.class: SymTable.java Sym.class DuplicateSymNameException.class EmptySymTableException.class
	$(JC) $(FLAGS) -cp $(CP) SymTable.java

Type.class: Type.java
	$(JC) $(FLAGS) -cp $(CP) Type.java ast.java
	
DuplicateSymNameException.class: DuplicateSymNameException.java
	$(JC) $(FLAGS) -cp $(CP) DuplicateSymNameException.java

EmptySymTableException.class: EmptySymTableException.java
	$(JC) $(FLAGS) -cp $(CP) EmptySymTableException.java

all:
	$(JC) $(FLAGS) -cp $(CP) DuplicateSymNameException.java
	$(JC) $(FLAGS) -cp $(CP) EmptySymTableException.java
	$(JC) $(FLAGS) -cp $(CP) ErrMsg.java
	java -cp $(CP) java_cup.Main < base.cup
	$(JC) $(FLAGS) -cp $(CP) sym.java
	java -cp $(CP) JLex.Main base.jlex
	$(JC) $(FLAGS) -cp $(CP) base.jlex.java
	$(JC) $(FLAGS) -cp $(CP) ast.java Sym.java SymTable.java Type.java
	$(JC) $(FLAGS) -cp $(CP) parser.java
	$(JC) $(FLAGS) -cp $(CP) P5.java

##test
test:
	@echo Compiling $(TESTS_DIR)/typeErrors.base...
	@java -cp $(CP) P5 $(TESTS_DIR)/typeErrors.base $(TESTS_DIR)/typeErrors.out 2>&1 | $(PY) testErrors.py

	@echo "\n"
	@echo Compiling $(TESTS_DIR)/test.base...
	@java -cp $(CP) P5 $(TESTS_DIR)/test.base $(TESTS_DIR)/test.out

###
# clean
###
clean:
	rm -f *~ bin/*.class parser.java base.jlex.java sym.java

## cleantest (delete test artifacts)
cleantest:
	rm -f *.out

example: example.base example.err
	java -cp $(CP) P5 $(TESTS_DIR)/example.base $(TESTS_DIR)/example.out 2> $(TESTS_DIR)/errs
	diff $(TESTS_DIR)/errs $(TESTS_DIR)/example.err 
