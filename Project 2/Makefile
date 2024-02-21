###
# This Makefile can be used to make a scanner for the base language
# (Yylex.class) and to make a program that tests the scanner (P2.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: P2.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac

TARGET_DIR := bin
DEPENDENCY_DIR := deps

FLAGS = -g -cp $(CP) -d $(TARGET_DIR)

ifeq ($(OS),Windows_NT)
	CP = $(DEPENDENCY_DIR);$(TARGET_DIR)
else
	CP = ./$(DEPENDENCY_DIR):./$(TARGET_DIR)
endif

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: base.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) base.jlex.java

base.jlex.java: base.jlex sym.class
	java -cp $(CP) JLex.Main base.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java


###
# testing - add more here to run your tester and compare its results
# to expected results
###
test: base.jlex
	java -cp $(CP) P2

###
# clean up
###

clean:
	rm -f *~ *.class base.jlex.java bin/*.class

cleantest:
	@echo "Nothing to clean"
