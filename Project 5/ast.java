import java.io.*;
import java.util.*;
import java.util.function.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a base program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FctnDeclNode        TypeNode, IdNode, FormalsListNode, FctnBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       TupleDeclNode       IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     -FctnBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       LogicalNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       TupleNode           IdNode
//
//     StmtNode:
//       -AssignStmtNode      AssignExpNode
//       -PostIncStmtNode     ExpNode
//       -PostDecStmtNode     ExpNode
//       -IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       -IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       -WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       -ReadStmtNode        ExpNode
//       -WriteStmtNode       ExpNode
//       -CallStmtNode        CallExpNode
//       -ReturnStmtNode      ExpNode
//
//     -ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       -IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       -TupleAccessNode     ExpNode, IdNode
//       -AssignExpNode       ExpNode, ExpNode
//       -CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         -UnaryMinusNode
//         -NotNode
//       BinaryExpNode       ExpNode ExpNode
//         -PlusNode : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -MinusNode : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -TimesNode : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -DivideNode : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -EqualsNode  : IMPLEMENT EQUALS_OP INTERFACE
//         -NotEqualsNode : IMPLEMENT EQUALS_OP INTERFACE
//         -LessNode  : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -LessEqNode  : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -GreaterNode  : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -GreaterEqNode  : IMPLEMENT ARITHMETIC_OP INTERFACE
//         -AndNode : IMPLEMENT LOGICAL_OP INTERFACE
//         -OrNode : IMPLEMENT LOGICAL_OP INTERFACE
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        LogicalNode,  IntegerNode,  VoidNode,    IdNode,  
//        TrueNode,     FalseNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FctnDeclNode,  FormalDeclNode,
//        TupleDeclNode,   FctnBodyNode,    TupleNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,    WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  TupleAccessNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,  NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,       TimesNode,     DivideNode,
//        EqualsNode,      NotEqualsNode,   LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,   AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    protected static final Type ERROR;
    protected static final Type FCTN;
    protected static final Type INT;
    protected static final Type LOGICAL;
    protected static final Type STR;
    protected static final Type TUPLE;
    protected static final Type TUPLE_DEF;
    protected static final Type VOID;

    static {
        ERROR = new ErrorType();
        FCTN = new FctnType();
        INT = new IntegerType();
        LOGICAL = new LogicalType();
        STR = new StringType();
        TUPLE = new TupleType(null);
        TUPLE_DEF = new TupleDefType();
        VOID = new VoidType();
    }
}

// **********************************************************************
//   ProgramNode, DeclListNode, StmtListNode, ExpListNode, 
//   FormalsListNode, FctnBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /***
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, tuple defintions, and functions in the program.
     ***/
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public Type resolveTypes() {
        return myDeclList.resolveTypes();
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing tuple names in variable decls), process all of the 
     * decls in the list.
     ***/    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }

    public Type resolveTypes() {
        return myDecls.stream()
               .map(node -> node.resolveTypes())
               .reduce(VOID, (acc, e) -> acc.equals(ERROR) ? acc : e);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

    public Type resolveTypes(final Type expectedRet) {
        Function<StmtNode, Type> resolver = (node) -> {
            if (node instanceof IReturnable)
                return ((IReturnable) node).resolveTypes(expectedRet);

            return node.resolveTypes();
        };
        return myStmts.stream()
                      .map(resolver)
                      .reduce(VOID, (acc, e) -> acc.equals(ERROR) ? acc : e);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        } 
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    public Type resolveTypes() {
        return myExps.stream()
               .map(node -> node.resolveTypes())
               .reduce(VOID, (acc, e) -> acc.equals(ERROR) ? acc : e);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) {         // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public Iterator<ExpNode> getExps() {
        return myExps.iterator();
    }

    public int nExps() {
        return myExps.size();
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     ***/
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    /***
     * Return the number of formals in this list.
     ***/
    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FctnBodyNode extends ASTnode {
    public FctnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     ***/
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public Type resolveTypes(Type returnType) {
        return myStmtList.resolveTypes(returnType);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


// **********************************************************************
// ****  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /***
     * Note: a formal decl needs to return a sym
     ***/
    abstract public Sym nameAnalysis(SymTable symTab);

    /* Not all Declarations need to be resolved */
    public Type resolveTypes() {return VOID;}
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /***
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a tuple type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for tuple field decls)
     * globalTab is global symbol table (for tuple type names)
     * symTab and globalTab can be the same
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public Sym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode tupleId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof TupleNode) {
            tupleId = ((TupleNode)myType).idNode();
			try {
				sym = globalTab.lookupGlobal(tupleId.name());
            
				// if the name for the tuple type is not found, 
				// or is not a tuple type
				if (sym == null || !(sym instanceof TupleDefSym)) {
					ErrMsg.fatal(tupleId.lineNum(), tupleId.charNum(), 
								"Invalid name of tuple type");
					badDecl = true;
				}
				else {
					tupleId.link(sym);
				}
			} catch (EmptySymTableException ex) {
				System.err.println("Unexpected EmptySymTableException " +
								    " in VarDeclNode.nameAnalysis");
				System.exit(-1);
			} 
        }
        
		try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Multiply-declared identifier");
				badDecl = true;            
			}
		} catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof TupleNode) {
                    sym = new TupleSym(tupleId);
                }
                else {
                    sym = new Sym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(".");
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NON_TUPLE if this is not a tuple type

    public static int NON_TUPLE = -1;
}

class FctnDeclNode extends DeclNode {
    public FctnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FctnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FctnSym sym = null;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(),
							"Multiply-declared identifier");
			}
        
			else { // add function name to local symbol table
				try {
					sym = new FctnSym(myType.type(), myFormalsList.length());
					symTab.addDecl(name, sym);
					myId.link(sym);
				} catch (DuplicateSymNameException ex) {
					System.err.println("Unexpected DuplicateSymNameException " +
									" in FctnDeclNode.nameAnalysis");
					System.exit(-1);
				} catch (EmptySymTableException ex) {
					System.err.println("Unexpected EmptySymTableException " +
									" in FctnDeclNode.nameAnalysis");
					System.exit(-1);
				}
			}
		} catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FctnDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
        myBody.nameAnalysis(symTab); // process the function body
        
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FctnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("{");
        myFormalsList.unparse(p, 0);
        p.println("} [");
        myBody.unparse(p, indent+4);
        p.println("]\n");
    }

    public Type resolveTypes() {
        Type expectedRet = ((FctnSym)myId.sym()).getReturnType();
        return myBody.resolveTypes(expectedRet);
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FctnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        try { 
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Multiply-declared identifier");
				badDecl = true;
			}
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FormalDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }  

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // 2 children
    private TypeNode myType;
    private IdNode myId;
}

class TupleDeclNode extends DeclNode {
    public TupleDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
		myDeclList = declList;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this tuple definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this tuple
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Multiply-declared identifier");
				badDecl = true;            
			}
		} catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in TupleDeclNode.nameAnalysis");
            System.exit(-1);
        } 

        SymTable tupleSymTab = new SymTable();
        
        // process the fields of the tuple
        myDeclList.nameAnalysis(tupleSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                TupleDefSym sym = new TupleDefSym(tupleSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                                   " in TupleDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in TupleDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("tuple ");
        myId.unparse(p, 0);
        p.println(" {");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}.\n");
    }

    // 2 children
    private IdNode myId;
	private DeclListNode myDeclList;
}

// **********************************************************************
// *****  TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class LogicalNode extends TypeNode {
    public LogicalNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new LogicalType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("logical");
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new IntegerType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /***
     * type
     ***/
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class TupleNode extends TypeNode {
    public TupleNode(IdNode id) {
		myId = id;
    }
 
    public IdNode idNode() {
        return myId;
    }
       
    /***
     * type
     ***/
    public Type type() {
        return new TupleType(myId);
    }
    public void unparse(PrintWriter p, int indent) {
        p.print("tuple ");
        p.print(myId.name());
    }
	
	// 1 child
    private IdNode myId;
}

// **********************************************************************
// ****  StmtNode and its subclasses
// **********************************************************************

interface IReturnable {
    public Type resolveTypes(Type expectedRet);
}

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);

    public Type resolveTypes() { return ASTnode.VOID; }
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    public Type resolveTypes() {
        return myAssign.resolveTypes();
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode implements IReturnable {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if ");
        myExp.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("]");  
    }

    public Type resolveTypes(Type expectedRet) {
        Type condType = myExp.resolveTypes();
        if (!condType.equals(LOGICAL)) {
            int lineNum = ((IPosition) myExp).lineNum();
            int charNum = ((IPosition) myExp).charNum();
            ErrMsg.fatal(lineNum, charNum,
                         "Non-logical expression used in if condition");
        }

        return myStmtList.resolveTypes(expectedRet);
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode implements IReturnable {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if ");
        myExp.unparse(p, 0);
        p.println(" [");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("]");
        doIndent(p, indent);
        p.println("else [");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("]"); 
    }

    public Type resolveTypes(Type expectedRet) {
        Type condType = myExp.resolveTypes();
        if (!condType.equals(LOGICAL)) {
            int lineNum = ((IPosition) myExp).lineNum();
            int charNum = ((IPosition) myExp).charNum();
            ErrMsg.fatal(lineNum, charNum,
                         "Non-logical expression used in if condition");
        }

        Type thenType = myThenStmtList.resolveTypes(expectedRet);
        Type elseType = myElseStmtList.resolveTypes(expectedRet);

        if (thenType.equals(ERROR) || elseType.equals(ERROR))
            return ERROR;

        return thenType;
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode implements IReturnable {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while ");
        myExp.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("]");
    }

    public Type resolveTypes(Type expectedRet) {
        Type condType = myExp.resolveTypes();
        if (!condType.equals(LOGICAL)) {
            int lineNum = ((IPosition) myExp).lineNum();
            int charNum = ((IPosition) myExp).charNum();
            ErrMsg.fatal(lineNum, charNum,
                         "Non-logical expression used in while condition");
        }

        return myStmtList.resolveTypes(expectedRet);
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("read >> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    public Type resolveTypes() {
        Type readType = myExp.resolveTypes();
        if (readType.equals(INT) || readType.equals(LOGICAL))
            return readType;

        int lineNum = ((IPosition) myExp).lineNum();
        int charNum = ((IPosition) myExp).charNum();

        if (readType.equals(FCTN)) {
            ErrMsg.fatal(lineNum, charNum, "Read attempt of function name");
            return ERROR;
        }

        if (readType.equals(TUPLE)) {
            ErrMsg.fatal(lineNum, charNum, "Read attempt of tuple variable");
            return ERROR;
        }

        if (readType.equals(TUPLE_DEF)) {
            ErrMsg.fatal(lineNum, charNum, "Read attempt of tuple name");
            return ERROR;
        }
        return ERROR;
    }

    // 1 child (actually can only be an IdNode or a TupleAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("write << ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    public Type resolveTypes() {
        Type writeType = myExp.resolveTypes();
        if (writeType.equals(INT) || writeType.equals(LOGICAL))
            return writeType;

        int lineNum = ((IPosition) myExp).lineNum();
        int charNum = ((IPosition) myExp).charNum();

        if (writeType.equals(FCTN)) {
            ErrMsg.fatal(lineNum, charNum, "Write attempt of function name");
            return ERROR;
        }

        if (writeType.equals(TUPLE)) {
            ErrMsg.fatal(lineNum, charNum, "Write attempt of tuple variable");
            return ERROR;
        }

        if (writeType.equals(TUPLE_DEF)) {
            ErrMsg.fatal(lineNum, charNum, "Write attempt of tuple name");
            return ERROR;
        }

        if (writeType.equals(VOID)) {
            ErrMsg.fatal(lineNum, charNum, "Write attempt of void");
            return ERROR;
        }
        return ERROR;
    }

    // 1 child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    public Type resolveTypes() {
        return myCall.resolveTypes();
    }

    // 1 child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode implements IReturnable {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     ***/
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(".");
    }

    public Type resolveTypes(Type expectedRet) {
        Type actualRet = myExp != null ? myExp.resolveTypes() : VOID;

        if (expectedRet.equals(actualRet))
            return expectedRet;

        int lineNum = myExp != null ? ((IPosition) myExp).lineNum() : 0;
        int charNum = myExp != null ? ((IPosition) myExp).charNum() : 0;

        if (expectedRet.equals(VOID)) {
            ErrMsg.fatal(lineNum, charNum, "Return with value in void function");
            return ERROR;
        } else if (!expectedRet.equals(VOID) && actualRet.equals(VOID)) {
            ErrMsg.fatal(lineNum, charNum, "Return value missing");
            return ERROR;
        }

        ErrMsg.fatal(lineNum, charNum, "Return value wrong type");
        return ERROR;
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ****  ExpNode and its subclasses
// **********************************************************************

interface IPosition {
    int lineNum();
    int charNum();
}

interface ExpType {
    Type resolveTypes();
}

abstract class ExpNode extends ASTnode implements ExpType {
    /***
     * Default version for nodes with no names
     ***/
    public void nameAnalysis(SymTable symTab) { }
}

class TrueNode extends ExpNode implements IPosition {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("True");
    }

    public Type resolveTypes() {
        return LOGICAL;
    }

    public int lineNum() { return myLineNum; }
    public int charNum() { return myCharNum; }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode implements IPosition {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("False");
    }

    public Type resolveTypes() {
        return LOGICAL;
    }

    public int lineNum() { return myLineNum; }
    public int charNum() { return myCharNum; }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode implements IPosition {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /***
     * Link the given symbol to this ID.
     ***/
    public void link(Sym sym) {
        mySym = sym;
    }
    
    /***
     * Return the name of this ID.
     ***/
    public String name() {
        return myStrVal;
    }
    
    /***
     * Return the symbol associated with this ID.
     ***/
    public Sym sym() {
        return mySym;
    }
    
    /***
     * Return the line number for this ID.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this ID.
     ***/
    public int charNum() {
        return myCharNum;
    }    
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     ***/
    public void nameAnalysis(SymTable symTab) {
		try {
            Sym sym = symTab.lookupGlobal(myStrVal);
            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
            } else {
                link(sym);
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IdNode.nameAnalysis");
            System.exit(-1);
        } 
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("<" + mySym + ">");
        }
    }

    public Type resolveTypes() {
        return mySym.getType();
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class IntLitNode extends ExpNode implements IPosition {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    public Type resolveTypes() {
        return INT;
    }

    public int lineNum() { return myLineNum; }
    public int charNum() { return myCharNum; }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StrLitNode extends ExpNode implements IPosition {
    public StrLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public Type resolveTypes() {
        return STR;
    }

    public int lineNum() { return myLineNum; }
    public int charNum() { return myCharNum; }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TupleAccessNode extends ExpNode implements IPosition {
    public TupleAccessNode(ExpNode loc, IdNode id) {
        myLoc = loc;	
        myId = id;
    }

    /***
     * Return the symbol associated with this colon-access node.
     ***/
    public Sym sym() {
        return mySym;
    }    
    
    /***
     * Return the line number for this colon-access node. 
     * The line number is the one corresponding to the RHS of the colon-access.
     ***/
    public int lineNum() {
        return myId.lineNum();
    }
    
    /***
     * Return the char number for this colon-access node.
     * The char number is the one corresponding to the RHS of the colon-access.
     ***/
    public int charNum() {
        return myId.charNum();
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the colon-access
     * - process the RHS of the colon-access
     * - if the RHS is of a tuple type, set the sym for this node so that
     *   a colon-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate tuple definition
     ***/
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable tupleSymTab = null; // to lookup RHS of colon-access
        Sym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a tuple type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof TupleSym) { 
                // get symbol table for tuple type
                Sym tempSym = ((TupleSym)sym).getTupleType().sym();
                tupleSymTab = ((TupleDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a tuple type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Colon-access of non-tuple type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a colon-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a tuple type, or
        // a link to the Sym for the tuple type RHSid was declared to be
        else if (myLoc instanceof TupleAccessNode) {
            TupleAccessNode loc = (TupleAccessNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this colon-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no tuple in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Colon-access of non-tuple type");
                    badAccess = true;
                }
                else {  // get the tuple's symbol table in which to lookup RHS
                    if (sym instanceof TupleDefSym) {
                        tupleSymTab = ((TupleDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in TupleAccessNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of colon-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of colon-access in the tuple's symbol table
        if (!badAccess) {
			try {
				sym = tupleSymTab.lookupGlobal(myId.name()); // lookup
				if (sym == null) { // not found - RHS is not a valid field name
					ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
								"Invalid tuple field name");
					badAccess = true;
				}
            
				else {
					myId.link(sym);  // link the symbol
					// if RHS is itself as tuple type, link the symbol for its tuple 
					// type to this colon-access node (to allow chained colon-access)
					if (sym instanceof TupleSym) {
						mySym = ((TupleSym)sym).getTupleType().sym();
					}
				}
			} catch (EmptySymTableException ex) {
				System.err.println("Unexpected EmptySymTableException " +
								" in TupleAccessNode.nameAnalysis");
				System.exit(-1);
			} 
        }
    }    

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    public Type resolveTypes() {
        return myId.resolveTypes();
    }

    // 4 children
    private ExpNode myLoc;	
    private IdNode myId;
    private Sym mySym;          // link to Sym for tuple type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignExpNode extends ExpNode implements IPosition {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public int lineNum() {
        return ((IPosition) myLhs).lineNum();
    }

    public int charNum() {
        return ((IPosition) myLhs).charNum();
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");    
    }

    public Type resolveTypes() {
        Type lhsType = myLhs.resolveTypes();
        Type rhsType = myExp.resolveTypes();

        if (lhsType.equals(ERROR) || rhsType.equals(ERROR))
            return ERROR;

        if (lhsType.equals(rhsType)) {
            if (lhsType.equals(INT) || lhsType.equals(LOGICAL))
                return lhsType;
        } else {
            ErrMsg.fatal(((IPosition)myLhs).lineNum(), ((IPosition)myLhs).charNum(),
                     "Mismatched type");
            return ERROR;
        }

        int lineNum = ((IPosition) myLhs).lineNum();
        int charNum = ((IPosition) myLhs).charNum();

        if (lhsType.equals(FCTN)) {
            ErrMsg.fatal(lineNum, charNum, "Assignment to function name");
            return ERROR;
        }
        else if (lhsType.equals(TUPLE)) {
            ErrMsg.fatal(lineNum, charNum, "Assignment to tuple variable");
            return ERROR;
        }
        else if (lhsType.equals(TUPLE_DEF)) {
            ErrMsg.fatal(lineNum, charNum, "Assignment to tuple name");
            return ERROR;
        }

        return ERROR;
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode implements IPosition {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public int lineNum() {
        return myId.lineNum();
    }

    public int charNum() {
        return myId.charNum();
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    } 

    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");   
    }

    public Type resolveTypes() {
        Type idType = myId.resolveTypes();

        if (!idType.equals(FCTN)) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Call attempt on non-function");
            return ERROR;
        }

        FctnSym funcSym = (FctnSym) myId.sym();

        int expArgCount = funcSym.getNumParams();
        int actArgCount = myExpList.nExps();

        if (expArgCount != actArgCount) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Function call with wrong # of args");
            return ERROR;
        }

        List<Type> params = funcSym.getParamTypes();
        Iterator<ExpNode> args = myExpList.getExps();

        for (Type paramType: params) {
            ExpNode arg = args.next();
            Type argType = arg.resolveTypes();

            if (paramType.equals(argType))
                continue;

            int lineNum = ((IPosition) arg).lineNum();
            int charNum = ((IPosition) arg).charNum();

            ErrMsg.fatal(lineNum, charNum,
                         "Actual type does not match formal type");
        }
        return funcSym.getReturnType();
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode implements IPosition {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public int lineNum() {
        return ((IPosition) myExp).lineNum();
    }

    public int charNum() {
        return ((IPosition) myExp).charNum();
    }
    
    // 1 child
    protected ExpNode myExp;
}

interface IBinaryOps {
    Type resolveTypes(ExpNode exp1, ExpNode exp2);
}

abstract class BinaryExpNode extends ExpNode implements IBinaryOps, IPosition {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    public Type resolveTypes() {
        return resolveTypes(myExp1, myExp2);
    }

    public int lineNum() {
        return ((IPosition) myExp1).lineNum();
    }

    public int charNum() {
        return ((IPosition) myExp1).charNum();
    }
    
    // 2 children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// ****  Subclasses of UnaryExpNode
// **********************************************************************

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(~");
        myExp.unparse(p, 0);
        p.print(")");
    }

    public Type resolveTypes() {
        Type expType = myExp.resolveTypes();

        if (expType.equals(LOGICAL))
            return expType;

        int lineNum = ((IPosition) myExp).lineNum();
        int charNum = ((IPosition) myExp).charNum();
        ErrMsg.fatal(lineNum, charNum,
                     "Logical operator used with non-logical operand");
        return ERROR;
    }
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }

    public Type resolveTypes() {
        Type expType = myExp.resolveTypes();

        if (expType.equals(INT))
            return expType;

        int lineNum = ((IPosition) myExp).lineNum();
        int charNum = ((IPosition) myExp).charNum();
        ErrMsg.fatal(lineNum, charNum,
                     "Arithmetic operator used with non-integer operand");
        return ERROR;
    }
}

// **********************************************************************
// ****  Subclasses of BinaryExpNode
// **********************************************************************

interface IIntegerOps extends IBinaryOps {
    @Override
    default Type resolveTypes(ExpNode exp1, ExpNode exp2) {
        Type t1 = exp1.resolveTypes();
        Type t2 = exp2.resolveTypes();
        boolean errT1 = false;
        boolean errT2 = false;

        if (t1.equals(ASTnode.ERROR) || t2.equals(ASTnode.ERROR))
            return ASTnode.ERROR;

        int lineNum1 = ((IPosition) exp1).lineNum();
        int charNum1 = ((IPosition) exp1).charNum();

        int lineNum2 = ((IPosition) exp2).lineNum();
        int charNum2 = ((IPosition) exp2).charNum();

        if (!t1.equals(ASTnode.INT)) {
            ErrMsg.fatal(lineNum1, charNum1, errMsg());
            errT1 = true;
        }

        if (!t2.equals(ASTnode.INT)) {
            errT2 = true;
            ErrMsg.fatal(lineNum2, charNum2, errMsg());
        }

        return (errT1 || errT2) ? ASTnode.ERROR : ASTnode.INT;
    }

    String errMsg();
}

interface IArithmeticOps extends IIntegerOps {
    @Override
    default String errMsg() {
        return "Arithmetic operator used with non-integer operand";
    }
}

interface IRelationalOps extends IIntegerOps {
    @Override
    default String errMsg() {
        return "Relational operator used with non-integer operand";
    }
}

interface ILogicalOps extends IBinaryOps {
    @Override
    default Type resolveTypes(ExpNode exp1, ExpNode exp2) {
        Type t1 = exp1.resolveTypes();
        Type t2 = exp2.resolveTypes();
        boolean errT1 = false;
        boolean errT2 = false;

        if (t1.equals(ASTnode.ERROR) || t2.equals(ASTnode.ERROR))
            return ASTnode.ERROR;

        int lineNum1 = ((IPosition) exp1).lineNum();
        int charNum1 = ((IPosition) exp1).charNum();

        int lineNum2 = ((IPosition) exp2).lineNum();
        int charNum2 = ((IPosition) exp2).charNum();

        if (!t1.equals(ASTnode.LOGICAL)) {
            errT1 = true;
            ErrMsg.fatal(lineNum1, charNum1,
                         "Logical operator used with non-logical operand");
        }

        if (!t2.equals(ASTnode.LOGICAL)) {
            errT2 = true;
            ErrMsg.fatal(lineNum2, charNum2,
                         "Logical operator used with non-logical operand");
        }

        return (errT1 || errT2) ? ASTnode.ERROR : ASTnode.LOGICAL;
    }
}

interface IEqualityOps extends IBinaryOps {
    @Override
    default Type resolveTypes(ExpNode exp1, ExpNode exp2) {
        Type t1 = exp1.resolveTypes();
        Type t2 = exp2.resolveTypes();
        boolean err = false;
        int lineNum1 = ((IPosition) exp1).lineNum();
        int charNum1 = ((IPosition) exp1).charNum();

        int lineNum2 = ((IPosition) exp1).lineNum();
        int charNum2 = ((IPosition) exp1).charNum();

        if (t1.equals(ASTnode.ERROR) || t2.equals(ASTnode.ERROR))
            return ASTnode.ERROR;

        if (t1.equals(t2)) {
            if (t1.equals(ASTnode.INT) || t1.equals(ASTnode.LOGICAL))
                return t1;
        } else {
            ErrMsg.fatal(lineNum1, charNum1, "Mismatched type");
            return ASTnode.ERROR;
        }

        // T1 == T2 at this point
        if (t1.equals(ASTnode.VOID)) {
            err = true;
            ErrMsg.fatal(lineNum1, charNum1,
                         "Equality operator used with void function calls");
        }

        if (t1.equals(ASTnode.FCTN)) {
            err = true;
            ErrMsg.fatal(lineNum1, charNum1,
                         "Equality operator used with function names");
        }

        if (t1.equals(ASTnode.TUPLE_DEF)) {
            err = true;
            ErrMsg.fatal(lineNum1, charNum1,
                         "Equality operator used with tuple names");
        }

        if (t1.equals(ASTnode.TUPLE)) {
            err = true;
            ErrMsg.fatal(lineNum1, charNum1,
                         "Equality operator used with tuple variables");
        }

        if (err)
            return ASTnode.ERROR;

        return t1;
    }
}

class PlusNode extends BinaryExpNode implements IArithmeticOps {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode implements IArithmeticOps {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode implements IArithmeticOps {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode implements IArithmeticOps {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode implements IEqualityOps {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode implements IEqualityOps {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ~= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode implements IRelationalOps {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode implements IRelationalOps {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode implements IRelationalOps {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode implements IRelationalOps {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode implements ILogicalOps {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode implements ILogicalOps {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
