import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol
import static java.lang.Math.random;

/**
 * This program is to be used to test the base scanner.
 * This version is set up to test all tokens, but more code is needed to test
 * other aspects of the scanner (e.g., input that causes errors, character
 * numbers, values associated with tokens)
 */
public class P2 {
    static int score;
    static int testsRun;

    static {
        score = 0;
        testsRun = 0;
    }

    public static void main(String[] args) throws IOException {
        // exception may be thrown by yylex
        // test all tokens
        // testAllTokens();
        CharNum.num = 1;

        // ADD CALLS TO OTHER TEST METHODS HERE
        testKeywords();
        testOperators();
        testValidIntLits();
        testInvalidIntLits();
        
        testValidStrLits();
        testInvalidStrLits();
        testValidIdentifiers();
        testInvalidIdentifiers();
        // randomTests();
        System.out.println("PASSED " + score + "/40" + " TESTS.");
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("allTokens.in");
            outFile = new PrintWriter(new FileWriter("allTokens.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("allTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.LOGICAL:
                outFile.println("logical");
                break;
            case sym.INTEGER:
                outFile.println("integer");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.TUPLE:
                outFile.println("tuple");
                break;
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                outFile.println("while");
                break;
            case sym.READ:
                outFile.println("read");
                break;
            case sym.WRITE:
                outFile.println("write");
                break;
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.TRUE:
                outFile.println("True");
                break;
            case sym.FALSE:
                outFile.println("False");
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLITERAL:
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRLITERAL:
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.LSQBRACKET:
                outFile.println("[");
                break;
            case sym.RSQBRACKET:
                outFile.println("]");
                break;
            case sym.COLON:
                outFile.println(":");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.INPUTOP:
                outFile.println(">>");
                break;
            case sym.OUTPUTOP:
                outFile.println("<<");
                break;
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("~");
                break;
            case sym.AND:
                outFile.println("&");
                break;
            case sym.OR:
                outFile.println("|");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQUALS:
                outFile.println("~=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
            case sym.ASSIGN:
                outFile.println("=");
                break;
            default:
                outFile.println("!!! UNKNOWN TOKEN !!!");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
     *
     */
    private static void testKeywords() throws IOException {
        System.out.println("\nStarting keyword test");
        Reader reader;
        Yylex scanner;
        for (Token token : new TokenStream(TokenType.KEYWORDS)) {
            CharNum.num = 1;
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try{
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            }catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }


    /**
     *  
     */
    private static void testOperators() throws IOException {
        System.out.println("\nStarting operator test");
        Reader reader;
        Yylex scanner;
        for (Token token : new TokenStream(TokenType.OPERATORS)) {
            CharNum.num = 1;
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try{
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            }catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }

    /**
     *
     */
    private static void testValidIntLits() throws IOException {
        System.out.println("\nStarting valid INTLIT test");
        TokenStream tokenStream = new TokenStream(TokenType.VALID_INTLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random valid intlits.
        for(int i = 0; i < 10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try {
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            } catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }


    /**
     *
     */
    private static void testInvalidIntLits() throws IOException {
        System.out.println("\nStarting invalid INTLIT test");
        TokenStream tokenStream = new TokenStream(TokenType.INVALID_INTLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random invalid intlits.
        for(int i = 0; i < 10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            
            try {
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            } catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }

    /**
     *
     */
    private static void testValidStrLits() throws IOException {
        System.out.println("\nStarting valid STRLIT test");
        TokenStream tokenStream = new TokenStream(TokenType.VALID_STRLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random valid strlits.
        for(int i = 0; i < 10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try{
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            }catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }


    /**
     *
     */
    private static void testInvalidStrLits() throws IOException {
        System.out.println("\nStarting invalid STRLIT test");
        TokenStream tokenStream = new TokenStream(TokenType.INVALID_STRLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random invalid strlits.
        for(int i = 0; i < 10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try {
                assertEquals(scanner.next_token().sym, token.sym());
                score++;
            } catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
        
    }


    /**
     * 
     */
    private static void testValidIdentifiers() throws IOException {
        System.out.println("\nStarting valid Identifier test");
        TokenStream tokenStream = new TokenStream(TokenType.VALID_IDENTIFIERS);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random valid valid Identifiers.
        for(int i=0; i<10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            try {
                Symbol symbol = scanner.next_token();
                assertEquals(symbol.sym, token.sym());
                score++;
                assertEquals(((IdTokenVal)symbol.value).idVal, token.token());
                score++;
            } catch(AssertionError a){
                System.out.println(a.getMessage());
            }
        }
    }


    /**
     *
     */
    private static void testInvalidIdentifiers() throws IOException{
        System.out.println("\nStarting invalid Identifier test");
        TokenStream tokenStream = new TokenStream(TokenType.INVALID_IDENTIFIERS);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random invalid Identifiers.
        for(int i = 0; i < 10; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);
            Symbol symbol = null;
            try {
                symbol = scanner.next_token();
                assertEquals(symbol.sym, token.sym());
                score++;
            } catch(AssertionError a){
                System.out.println(a.getMessage());
            }

            System.out.println("scannerToken: " + symbol.sym
                    + " token.sym: " + token.sym()
                    + " token: " + token.token());
        }
    }

    // TODO: Cleanup
    private static void randomTests() throws IOException{
        TokenStream tokenStream = new TokenStream(TokenType.RANDOM);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random valid intlits.
        for(int i=0; i<10; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            Symbol scannerToken=scanner.next_token();
            if((scannerToken.sym)==0 && token.sym()==-1){
                score++;
            }
            // else if(){ need to make case for invalid identifiers

            // }
            else{
                try{
                    assertEquals(scannerToken.sym, token.sym());
                    score++;
                    
                }catch(AssertionError a){
                    System.out.println(a.getMessage());
                    System.out.println("symbol: " + token.sym() + " token: " + token.token());
                }
            }
        }
    }

    // /**
    //  *
    //  */
    // private static void commentTest(){

    // }

    // /**
    //  *
    //  */
    // private static void whiteSpaceTest(){

    // }

    // /**
    //  *
    //  */
    // private static void lengthLimitTest(){

    // }


    private final static void assertEquals(int a, int b) {
        if (a != b) {
            throw new AssertionError(a + " != " + b);
        }
    }

    private final static void assertEquals(String a, String b) {
        if (!a.equals(b)) {
            throw new AssertionError(a + " != " + b);
        }
    }
}

record Token(int sym, String token) {}

enum TokenType {
    KEYWORDS,
    OPERATORS,
    VALID_INTLIT,
    INVALID_INTLIT,
    VALID_STRLIT,
    INVALID_STRLIT,
    VALID_IDENTIFIERS,
    INVALID_IDENTIFIERS,
    RANDOM;
}


class TokenStream implements Iterable<Token> {
    private final TokenType type;

    public TokenStream() {
        this.type = TokenType.RANDOM;
    }

    public TokenStream(TokenType type) {
        this.type = type;
    }

    public Iterator<Token> iterator() {
        return new TokenStreamIterator(this.type);
    }
}

class TokenStreamIterator implements Iterator<Token> {
    private static final int MAX_TOKEN_LENGTH;
    private static final int MIN_TOKEN_LENGTH;
    private static final int MIN_CHAR;
    private static final int MAX_CHAR;
    private static final int N_CASES;
    private static final int CHARSET_SIZE;
    private static final int INVALID_SYM;
    private static final char[] CHARSET;
    private static final String ESCAPES;
    private static final Token[][] KNOWN_TOKENS;


    static {
        MIN_TOKEN_LENGTH = 1;
        MAX_TOKEN_LENGTH = 32;
        MIN_CHAR = 32;
        MAX_CHAR = 127;
        N_CASES = 7;
        INVALID_SYM = 0;
        ESCAPES = "nst'\\\"";

        // +1 for underscore (_)
        CHARSET_SIZE = ('Z' - 'A' + 1) + ('z' - 'a' + 1) + ('9' - '0' + 1) + 1;
        CHARSET = new char[CHARSET_SIZE];

        // We initialize in ASCII order
        int i = 0;
        for (char c = '0'; c <= '9';) CHARSET[i++] = c++;
        for (char c = 'A'; c <= 'Z';) CHARSET[i++] = c++;
        CHARSET[i] = '_';
        for (char c = 'a'; c <= 'z';) CHARSET[i++] = c++;

        KNOWN_TOKENS = new Token[][] {
        /* Keywords */
        {

            new Token(sym.VOID, "void"),       new Token(sym.LOGICAL, "logical"),
            new Token(sym.INTEGER, "integer"), new Token(sym.TRUE, "True"),
            new Token(sym.FALSE, "False"),     new Token(sym.TUPLE, "tuple"),
            new Token(sym.READ, "read"),       new Token(sym.WRITE, "write"),
            new Token(sym.IF, "if"),           new Token(sym.ELSE, "else"),
            new Token(sym.WHILE, "while"),     new Token(sym.RETURN, "return"),
        },

        /* Operators */
        {
            new Token(sym.ASSIGN, "="),     new Token(sym.LCURLY, "{"),
            new Token(sym.RCURLY, "}"),     new Token(sym.LPAREN, "("),
            new Token(sym.RPAREN, ")"),     new Token(sym.LSQBRACKET, "["),
            new Token(sym.RSQBRACKET, "]"), new Token(sym.COLON, ":"),
            new Token(sym.COMMA, ","),      new Token(sym.DOT, "."),
            new Token(sym.OUTPUTOP, "<<"),  new Token(sym.INPUTOP, ">>"),
            new Token(sym.ASSIGN, "="),     new Token(sym.NOT, "~"),
            new Token(sym.AND, "&"),        new Token(sym.OR, "|"),
            new Token(sym.PLUSPLUS, "++"),  new Token(sym.MINUSMINUS, "--"),
            new Token(sym.PLUS, "+"),       new Token(sym.MINUS, "-"),
            new Token(sym.TIMES, "*"),      new Token(sym.DIVIDE, "/"),
            new Token(sym.LESS, "<"),       new Token(sym.GREATER, ">"),
            new Token(sym.LESSEQ, "<="),    new Token(sym.GREATEREQ, ">="),
            new Token(sym.EQUALS, "=="),    new Token(sym.NOTEQUALS, "~="),
        },


        };
    }

    private final TokenType type;
    private int cur;
    private int idx = -1;

    public TokenStreamIterator(TokenType type) {
        this.type = type;
        cur = 0;
        if (type == TokenType.KEYWORDS)
            idx = 0;
        else if (type == TokenType.OPERATORS)
            idx = 1;
    }

    private static int rng(int min, int max) {
        return (int) (random() * (max - min)) + min;
    }

    private static int rng(int max) {
        return (int) (random() * max);
    }

    public boolean hasNext() {
        switch (type) {
        case KEYWORDS:
        case OPERATORS:
            return cur < KNOWN_TOKENS[idx].length;
        default:
            return true;
        }
    }

    public Token next() throws NoSuchElementException{
        switch (type) {
        case KEYWORDS:
        case OPERATORS:
            return KNOWN_TOKENS[idx][cur++];

        case VALID_INTLIT:
            return new Token(sym.INTLITERAL, generateValidInteger());

        case INVALID_INTLIT:
            return new Token(INVALID_SYM, generateInvalidInteger());

        case VALID_STRLIT:
            return new Token(sym.STRLITERAL, generateValidString());

        case INVALID_STRLIT:
            return new Token(INVALID_SYM, generateInvalidString());

        case VALID_IDENTIFIERS:
            return new Token(sym.ID, generateValidIdentifier());

        case INVALID_IDENTIFIERS:
            return new Token(sym.ID, generateInvalidIdentifier());

        case RANDOM:
            switch (rng(N_CASES)) {
            case 0: /* Keywords */
                return KNOWN_TOKENS[0][rng(KNOWN_TOKENS[0].length)];

            case 1: /* Operators */
                return KNOWN_TOKENS[1][rng(KNOWN_TOKENS[1].length)];

            case 2: /* Integer literals */
            
            System.out.println("generate valid intlit");
                return new Token(sym.INTLITERAL, generateValidInteger());

            case 3: /* Invalid integer literals */
            System.out.println("generate invalid intlit");

                return new Token(INVALID_SYM, generateInvalidInteger());

            case 4: /* String literals */
            
            System.out.println("generate valid string");
                return new Token(sym.STRLITERAL, generateValidString());

            case 5: /* Invalid String literals */
            
            System.out.println("generate invalid string");
                return new Token(INVALID_SYM, generateInvalidString());

            case 6: /* Identifiers */
            
            System.out.println("generate valid Identifiers");
                return new Token(sym.ID, generateValidIdentifier());
            }
        }

        return null;
    }

    private final static String generateValidInteger() {
        return String.valueOf(rng(Integer.MAX_VALUE));
    }

    private final static String generateInvalidInteger() {
        String badNum = String.valueOf(Integer.MAX_VALUE);

        int extra = rng(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
        for (; extra > 0 ; extra--) {
            badNum += rng(10);
        }

        return badNum;
    }

    private final static String generateValidString() {
        int len = rng(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
        char[] buf = new char[len];

        for (int i = 0; i < len; i++) {
            if (random() > 0.8 && i < len - 2) {
                buf[i++] = '\\';
                buf[i] = ESCAPES.charAt(rng(ESCAPES.length()));
            } else {
                char c;
                do {
                    c = (char) rng(32, 127);
                } while (c == '"' || c == '\n' || c == '\\');

                buf[i] = c;
            }
        }

        return "\"" + new String(buf) + "\"";
    }

    private final static String generateInvalidString() {
        switch (rng(3)) {
        case 0:
            return generateUnterminatedString();
        case 1:
            return generateBadEscapeString();
        case 2:
            return generateUnterminatedBadEscapeString();
        default:
            throw new RuntimeException("Unreachable statement reached!");
        }
    }

    private final static String generateUnterminatedString() {
        String valid = generateValidString();
        return valid.substring(0, valid.length() - 1);
    }

    private final static String generateBadEscapeString() {
        int len = rng(MIN_TOKEN_LENGTH + 2, MAX_TOKEN_LENGTH);
        char[] buf = new char[len];
        int idx = rng(len - 2);

        buf[idx] = '\\';

        char c;
        do {
            c = (char) rng(32, 127);
        } while (ESCAPES.indexOf(c) >= 0);
        buf[idx + 1] = c;

        for (int i = 0; i < len; i++) {
            if (idx == i) {
                i++;
                continue;
            }

            do {
                c = (char) rng(32, 127);
            } while (c == '"' || c == '\n' || c == '\\');

            buf[i] = c;
        }

        return "\"" + new String(buf) + "\"";
    }

    private final static String generateUnterminatedBadEscapeString() {
        String badEscape = generateBadEscapeString();
        return badEscape.substring(0, badEscape.length() - 1);
    }

    private final static String generateValidIdentifier() {
        int len = rng(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);

        char[] buf = new char[len];

        char c;
        do {
            c = CHARSET[rng(CHARSET_SIZE)];
        } while (c <= '9');

        buf[0] = c;

        for (int i = 1; i < len; i++) {
            buf[i] = CHARSET[rng(CHARSET_SIZE)];
        }

        return new String(buf);
    }

    private final static String generateInvalidIdentifier() {
        char[] buf = generateValidIdentifier().toCharArray();

        switch(rng(2)) {
        case 0:
            buf[0] = (char) rng('0', '9' + 1);
            break;
        case 1:
            char c;
            do {
                c = (char) rng(MIN_CHAR + 1, MAX_CHAR);
            } while (Arrays.binarySearch(CHARSET, c) >= 0);

            buf[rng(buf.length)] = c;
            break;
        }

        return new String(buf);
    }
}
