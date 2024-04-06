import java.io.*;
import java.util.*;

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
//     FctnBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       LogicalNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       TupleNode           IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       -IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       -TupleAccessNode     ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
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
    protected static SymTable symTable;
    protected static final SymTable globalContext;
    protected static final String MULTIPLY_DECLARED;
    protected static final String UNDECLARED;
    protected static final String BAD_COLON_ACCESS;
    protected static final String INVALID_TUPLE_FIELD;
    protected static final String BAD_VOID_DECLARED;
    protected static final String INVALID_TUPLE_NAME;

    static {
        symTable = new SymTable();
        globalContext = symTable;
        MULTIPLY_DECLARED = "Multiply-declared identifier";
        UNDECLARED = "Undeclared identifier";
        BAD_COLON_ACCESS = "Colon-access of non-tuple type";
        INVALID_TUPLE_FIELD = "Invalid tuple field name";
        BAD_VOID_DECLARED = "Non-function declared void";
        INVALID_TUPLE_NAME = "Invalid name of tuple type";
    }

    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    public void resolveNames() throws EmptySymTableException {}

    public static <E extends ASTnode> void resolver(E e) {
        try {
            e.resolveNames();
        } catch (EmptySymTableException | IllegalArgumentException ex) {
            // ex.printStackTrace();
        }
    }

    protected static void switchContext(SymTable symTable) {
        if (symTable == null)
            throw new NullPointerException("Expect symTable to be non null");

        ASTnode.symTable = symTable;
    }

    protected static void restoreContext() {
        ASTnode.symTable = globalContext;
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

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    @Override
    public void resolveNames() throws EmptySymTableException {
        myDeclList.resolveNames();
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
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

    @Override
    public void resolveNames() throws EmptySymTableException {
        myDecls.forEach(ASTnode::resolver);
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        } 
    }

    @Override
    public void resolveNames() {
        myStmts.forEach(ASTnode::resolver);
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
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

    @Override
    public void resolveNames() {
        myExps.forEach(ASTnode::resolver);
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
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

    public TypeNode[] getTypes() {
        return myFormals.stream()
                        .map(f -> f.getType())
                        .toArray(TypeNode[]::new);
    }

    @Override
    public void resolveNames() {
        myFormals.forEach(ASTnode::resolver);
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FctnBodyNode extends ASTnode {
    public FctnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    @Override
    public void resolveNames() throws EmptySymTableException {
        myDeclList.resolveNames();
        myStmtList.resolveNames();
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


// **********************************************************************
// ****  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    protected TypeNode myType;
    protected IdNode myId;

    public DeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public TypeNode getType() {
        return myType;
    }

    public IdNode getId() {
        return myId;
    }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        super(type, id);
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(".");
    }

    @Override
    public void resolveNames() throws EmptySymTableException {
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         BAD_VOID_DECLARED);
        }

        myType.resolveNames();

        try {
            Sym sym;
            if (myType instanceof TupleNode) {
                sym = globalContext.lookupGlobal(myType.getType());
            } else {
                sym = new Sym(myType.getType());
            }
            symTable.addDecl(myId.getName(), sym);
        } catch (DuplicateSymNameException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         MULTIPLY_DECLARED);
        }
    }

    // 3 children
    private int mySize;  // use value NON_TUPLE if this is not a tuple type

    public static int NON_TUPLE = -1;
}

class FctnDeclNode extends DeclNode {
    public FctnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FctnBodyNode body) {
        super(type, id);
        myFormalsList = formalList;
        myBody = body;
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
    
    @Override
    public void resolveNames() throws EmptySymTableException{
        String[] types= Arrays.stream(myFormalsList.getTypes())
                              .map(f -> f.getType())
                              .toArray(String[]::new);
        try {
            symTable.addDecl(myId.getName(),
                             new SymFunctional(myType.getType(), types));
        } catch (DuplicateSymNameException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         MULTIPLY_DECLARED);
        }

        symTable.addScope();
        
        myFormalsList.resolveNames();
        myBody.resolveNames();
        symTable.removeScope();
    }

    // 4 children
    private FormalsListNode myFormalsList;
    private FctnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        super(type, id);
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    @Override
    public void resolveNames() throws EmptySymTableException {
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         BAD_VOID_DECLARED);
        }

        try {
            symTable.addDecl(myId.getName(), new Sym(myType.getType()));
        } catch (DuplicateSymNameException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         MULTIPLY_DECLARED);
        }
    }
}

class TupleDeclNode extends DeclNode {
    public TupleDeclNode(IdNode id, DeclListNode declList) {
        super(null, id);
        myDeclList = declList;
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

    public void resolveNames() throws EmptySymTableException {
        SymTuple symTuple = new SymTuple(myId.getName());
        try {
            symTable.addDecl(myId.getName(), symTuple);
        } catch (DuplicateSymNameException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         MULTIPLY_DECLARED);
        }

        // Switch context
        switchContext(symTuple.symTable);

        myDeclList.resolveNames();

        // Restore context
        restoreContext();
    }

    // 2 children
    private DeclListNode myDeclList;
}

// **********************************************************************
// *****  TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    abstract String getType();
}

class LogicalNode extends TypeNode {
    public LogicalNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("logical");
    }
    
    public String getType(){
        return("logical");
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
    
    public String getType(){
        return("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
    
    public String getType(){
        return("void");
    }
}

class TupleNode extends TypeNode {
    public TupleNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("tuple ");
        myId.unparse(p, 0);
    }

    public String getType(){
        return myId.getName();
    }

    public void resolveNames() throws EmptySymTableException {
        myId.forceGlobalContext();
        myId.resolveNames();
        if (!(myId.getSym() instanceof SymTuple)) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         INVALID_TUPLE_NAME);
        }
    }

    // 1 child
    private IdNode myId;
}

// **********************************************************************
// ****  StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    public void resolveNames() throws EmptySymTableException {
        myAssign.resolveNames();
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
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

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();

        { // If-then scope
            symTable.addScope();

            myDeclList.resolveNames();
            myStmtList.resolveNames();

            symTable.removeScope();
        }
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
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

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();

        { // If-then scope
            symTable.addScope();

            myThenDeclList.resolveNames();
            myThenStmtList.resolveNames();

            symTable.removeScope();
        }

        { // Else scope
            symTable.addScope();

            myElseStmtList.resolveNames();
            myElseDeclList.resolveNames();

            symTable.removeScope();
        }
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
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

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();

        { // While scope
            symTable.addScope();

            myDeclList.resolveNames();
            myStmtList.resolveNames();

            symTable.removeScope();
        }
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

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("read >> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();
    }

    // 1 child (actually can only be an IdNode or a TupleAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("write << ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();
    }

    // 1 child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    public void resolveNames() throws EmptySymTableException {
        myCall.resolveNames();
    }

    // 1 child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
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

    public void resolveNames() throws EmptySymTableException {
        if (myExp != null)
            myExp.resolveNames();
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ****  ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    protected final int myLineNum;
    protected final int myCharNum;

    ExpNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public int getLineNum() {
        return myLineNum;
    }

    public int getCharNum() {
        return myCharNum;
    }
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        super(lineNum, charNum);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("True");
    }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        super(lineNum, charNum);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("False");
    }
}

class IdNode extends ExpNode {
    private boolean tupleContext = false;
    private boolean forcedGlobalContext = false;

    public void enableTupleContext() { tupleContext = true; }
    public void disableTupleContext() { tupleContext = false; }
    public void forceGlobalContext() { forcedGlobalContext = true; }

    public IdNode(int lineNum, int charNum, String strVal) {
        super(lineNum, charNum);
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public void resolveNames() throws EmptySymTableException {
        Sym sym;
        if (forcedGlobalContext)
            sym = globalContext.lookupGlobal(myStrVal);
        else
            sym = symTable.lookupGlobal(myStrVal);

        // If sym was found in a previous call, we keep the link
        if (mySym == null)
            mySym = sym;

        if (sym == null) {
            if (tupleContext) {
                ErrMsg.fatal(myLineNum, myCharNum, INVALID_TUPLE_FIELD);
            } else {
                ErrMsg.fatal(myLineNum, myCharNum, UNDECLARED);
            }
        }
        forcedGlobalContext = false;
    }

    public String getName() {
        return myStrVal;
    }

    public Sym getSym() {
        return mySym;
    }

    private String myStrVal;
    private Sym mySym;

    public String toString() {
        return "ID(name='" + myStrVal + "', type=<" + mySym.getType() + ">)";
    }
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        super(lineNum, charNum);
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myIntVal;
}

class StrLitNode extends ExpNode {
    public StrLitNode(int lineNum, int charNum, String strVal) {
        super(lineNum, charNum);
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private String myStrVal;
}

class TupleAccessNode extends ExpNode {
    private SymTable nextContext = null;
    private boolean track = false;

    private void startTracking() { track = true; }
    private void stopTracking() { track = false; }
    private SymTable getContext() { return nextContext; }

    public TupleAccessNode(ExpNode loc, IdNode id) {
        super(0, 0);
        myLoc = loc;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    public void resolveNames() throws EmptySymTableException {
        if (track) {
            if (myLoc instanceof TupleAccessNode)
                ((TupleAccessNode)myLoc).startTracking();

            myLoc.resolveNames();

            if (myLoc instanceof TupleAccessNode) {
                ((TupleAccessNode)myLoc).stopTracking();
                SymTable context = ((TupleAccessNode)myLoc).getContext();
                if (context == null) {
                    ErrMsg.fatal(((IdNode)myLoc).getLineNum(),
                                 ((IdNode)myLoc).getCharNum(),
                                 BAD_COLON_ACCESS);
                    return;
                } else {
                    switchContext(context);
                }

            } else if (myLoc instanceof IdNode) {
                if (!(((IdNode)myLoc).getSym() instanceof SymTuple)) {
                    ErrMsg.fatal(((IdNode)myLoc).getLineNum(),
                                 ((IdNode)myLoc).getCharNum(),
                                 BAD_COLON_ACCESS);
                    return;
                } else {
                    switchContext(((SymTuple)((IdNode)myLoc).getSym()).symTable);
                }
            }
            myId.enableTupleContext();
            myId.resolveNames();
            myId.disableTupleContext();

            Sym idSym = symTable.lookupGlobal(myId.getName());

            if (!(idSym instanceof SymTuple)) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                             BAD_COLON_ACCESS);
            }
            else {
                nextContext = ((SymTuple)idSym).symTable;
            }
            return;
        } // End tracked

        // Untracked
        if (myLoc instanceof TupleAccessNode)
            ((TupleAccessNode)myLoc).startTracking();

        myLoc.resolveNames();

        if (myLoc instanceof TupleAccessNode)
            ((TupleAccessNode)myLoc).stopTracking();

        if (myLoc instanceof TupleAccessNode) {
            SymTable context = ((TupleAccessNode)myLoc).getContext();
            if (context == null) {
                ErrMsg.fatal(((TupleAccessNode)myLoc).myId.getLineNum(),
                             ((TupleAccessNode)myLoc).myId.getCharNum(),
                            BAD_COLON_ACCESS);
            } else {
                switchContext(context);
            }
        } else if (myLoc instanceof IdNode) {
            if (!(((IdNode)myLoc).getSym() instanceof SymTuple)) {
                ErrMsg.fatal(((IdNode)myLoc).getLineNum(),
                             ((IdNode)myLoc).getCharNum(),
                             BAD_COLON_ACCESS);
            } else {
                switchContext(((SymTuple)((IdNode)myLoc).getSym()).symTable);
            }
        }

        myId.enableTupleContext();
        myId.resolveNames();
        myId.disableTupleContext();

        restoreContext();
    }

    // 2 children
    private ExpNode myLoc;
    private IdNode myId;
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        super(0, 0);
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");    
    }

    public void resolveNames() throws EmptySymTableException {
        myLhs.resolveNames();
        myExp.resolveNames();
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        super(0, 0);
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        super(0, 0);
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
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

    public void resolveNames() throws EmptySymTableException {
        myId.resolveNames();

        if (myExpList != null)
            myExpList.resolveNames();
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        super(0, 0);
        myExp = exp;
    }

    public void resolveNames() throws EmptySymTableException {
        myExp.resolveNames();
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        super(0, 0);
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void resolveNames() throws EmptySymTableException {
        myExp1.resolveNames();
        myExp2.resolveNames();
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
}

// **********************************************************************
// ****  Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
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

class MinusNode extends BinaryExpNode {
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

class TimesNode extends BinaryExpNode {
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

class DivideNode extends BinaryExpNode {
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

class EqualsNode extends BinaryExpNode {
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

class NotEqualsNode extends BinaryExpNode {
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

class GreaterNode extends BinaryExpNode {
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

class GreaterEqNode extends BinaryExpNode {
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

class LessNode extends BinaryExpNode {
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

class LessEqNode extends BinaryExpNode {
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


class AndNode extends BinaryExpNode {
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

class OrNode extends BinaryExpNode {
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
