import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol
import static java.lang.Math.random;

/**
 * @author Yesui Ulziibayar
 *
 * This program is to be used to test the base scanner.
 * This version is set up to test all tokens, but more code is needed to test
 * other aspects of the scanner (e.g., input that causes errors, character
 * numbers, values associated with tokens)
 */
public class P2 {
    static int score;
    static int testsRun;
    static String currTest;
    private final static PrintStream defaultPrintStream;
    private final static String badEscape;
    private final static String unterminated;
    private final static String unterminatedBadEscape;
    private final static String badIntLit;
    private final static String illegalChar;

    static {
        score = 0;
        testsRun = 0;
        currTest = null;
        defaultPrintStream = System.out;
        badEscape = "string literal with bad escaped character ignored";
        unterminated = "unterminated string literal ignored";
        unterminatedBadEscape
            = "unterminated string literal with bad escaped character ignored";
        badIntLit = "integer literal too large - using max value";
        illegalChar = "illegal character ignored: ";
    }

    /**
     * Changes the {@code System.out} PrintStream to a new PrintStream using a
     * {@code ByteArrayOutputStream} that captures the printed bytes,
     * which can be converted to {@code String}.
     *
     * @return A {@code ByteArrayOutputStream} that captures output written to
     *         the {@code System.out} PrintStream
     */
    private final static ByteArrayOutputStream switchPrintStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(outputStream);

        System.setErr(stream);

        return outputStream;
    }

    /**
     * Resets the {@code System.out} PrintStream to stdout
     */
    private final static void resetPrintStream() {
        System.setErr(defaultPrintStream);
    }

    /**
     * Driver
     *
     * @param args  unused
     *
     * @throws IOException Thrown by Yylex
     */
    public static void main(String[] args) throws IOException {
        // exception may be thrown by yylex
        // test all tokens
        testAllTokens();
        CharNum.num = 1;

        // Switch Print Stream to reduce output
        switchPrintStream();

        // 1. Keywords
        testKeywords();

        // 2. Operators
        testOperators();

        // 3. Valid Integer Literals
        testValidIntLits();

        // 4. Invalid Integer Literals
        testInvalidIntLits();

        // 5. Valid String Literals
        testValidStrLits();

        // 6. Invalid String Literals
        testInvalidStrLits();

        // 7. Valid Identifiers
        testValidIdentifiers();

        // 8. Fuzz Testing
        testRandomTokens();

        // 9. Comments
        testComments();

        // 10. White spaces
        testWhitespace();

        // Reset Print Stream
        resetPrintStream();

        // Test Error Messages

        // 11. Error messages on badly escaped String Literals
        testBadEscapeStringErrMsg();

        // 12. Error messages on unterminated String Literals
        testUnterminatedStringErrMsg();

        // 13. Error messages on unterminated badly escaped String Literals
        testUnterminatedBadEscapeStringErrMsg();

        // 14. Error messages on bad Integer Literals
        testOverflowIntegerErrMsg();

        // 15. Error messages on Illegal Characters
        testIllegalCharacterErrMsg();

        System.out.println("PASSED " + score + "/" + testsRun + " TESTS.");
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
     * Tests the Scanner on keywords (12 tests)
     */
    private static void testKeywords() throws IOException {
        currTest = "Keywords";

        Reader reader;
        Yylex scanner;
        for (Token token : new TokenStream(TokenType.KEYWORDS)) {
            CharNum.num = 1;
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Operators (28 tests)
     */
    private static void testOperators() throws IOException {
        currTest = "Operators";

        Reader reader;
        Yylex scanner;

        for (Token token : new TokenStream(TokenType.OPERATORS)) {
            CharNum.num = 1;
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Valid Integer Literals (100 tests)
     */
    private static void testValidIntLits() throws IOException {
        currTest = "Valid Integer Literals";

        TokenStream tokenStream = new TokenStream(TokenType.VALID_INTLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        //generate 10 random valid intlits.
        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Invalid Integer Literals (100 tests)
     */
    private static void testInvalidIntLits() throws IOException {
        currTest = "Invalid Integer Literals";

        TokenStream tokenStream = new TokenStream(TokenType.INVALID_INTLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Valid String Literals (100 tests)
     */
    private static void testValidStrLits() throws IOException {
        currTest = "Valid String Literals";

        TokenStream tokenStream = new TokenStream(TokenType.VALID_STRLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            Symbol sym = scanner.next_token();

            assertEquals(sym.sym, token.sym());
            assertEquals(((StrLitTokenVal)sym.value).strVal, token.token());
        }
    }

    /**
     * Tests the Scanner on Invalid String Literals (100 tests)
     */
    private static void testInvalidStrLits() throws IOException {
        currTest = "Invalid String Literals";

        TokenStream tokenStream = new TokenStream(TokenType.INVALID_STRLIT);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Valid Identifiers (100 tests)
     */
    private static void testValidIdentifiers() throws IOException {
        currTest = "Valid Identifiers";

        TokenStream tokenStream = new TokenStream(TokenType.VALID_IDENTIFIERS);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            Symbol symbol = scanner.next_token();

            assertEquals(symbol.sym, token.sym());
            assertEquals(((IdTokenVal)symbol.value).idVal, token.token());
        }
    }

    /**
     * Tests the Scanner on Comments (100 tests)
     */
    private static void testComments() throws IOException {
        currTest = "comments";

        TokenStream tokenStream = new TokenStream(TokenType.COMMENTS);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the Scanner on Whitespace (100 tests)
     */
    private static void testWhitespace() throws IOException {
        currTest = "white spaces";

        TokenStream tokenStream = new TokenStream(TokenType.WHITESPACE);
        Iterator<Token> iterator = tokenStream.iterator();
        
        Reader reader;
        Yylex scanner;

        for(int i = 0; i < 100; i++) {
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            assertEquals(scanner.next_token().sym, token.sym());
        }
    }

    /**
     * Tests the error messages on badly escaped strings (100 tests)
     */
    private static void testBadEscapeStringErrMsg() throws IOException {
        currTest = "Bad Escape String Error Message";

        ByteArrayOutputStream outputStream = switchPrintStream();

        Iterator<Token> iterator =
            new TokenStream(TokenType.BAD_ESCAPE_STRLIT).iterator();

        Token token;
        String output;

        for (int i = 0; i < 5; i++) {
            outputStream.reset();
            CharNum.num = 1;
            new Yylex(new StringReader(iterator.next().token())).next_token();

            output = outputStream.toString().trim();

            assertTrue(output.contains("ERROR"));
            assertTrue(output.contains(badEscape));
        }

        resetPrintStream();
    }

    /**
     * Tests the error messages on unterminated strings (100 tests)
     */
    private static void testUnterminatedStringErrMsg()  throws IOException {
        currTest = "Unterminated String Error Message";

        ByteArrayOutputStream outputStream = switchPrintStream();

        Iterator<Token> iterator =
            new TokenStream(TokenType.UNTERMINATED_STRLIT).iterator();

        Token token;
        String output;

        for (int i = 0; i < 100; i++) {
            outputStream.reset();
            CharNum.num = 1;
            new Yylex(new StringReader(iterator.next().token())).next_token();

            output = outputStream.toString().trim();

            assertTrue(output.contains("ERROR"));
            assertTrue(output.contains(unterminated));
        }

        resetPrintStream();
    }

    /**
     * Tests the error messages on badly escaped and unterminated strings
     * (100 tests)
     */
    private static void testUnterminatedBadEscapeStringErrMsg()  throws IOException {
        currTest = "Unterminated Bad Escape String Error Message";

        ByteArrayOutputStream outputStream = switchPrintStream();

        Iterator<Token> iterator =
            new TokenStream(TokenType.UNTERMINATED_BAD_ESCAPE_STRLIT).iterator();

        Token token;
        String output;

        for (int i = 0; i < 100; i++) {
            outputStream.reset();
            CharNum.num = 1;
            new Yylex(new StringReader(iterator.next().token())).next_token();

            output = outputStream.toString().trim();

            assertTrue(output.contains("ERROR"));
            assertTrue(output.contains(unterminatedBadEscape));
        }

        resetPrintStream();
    }

    /**
     * Tests the error messages on badly integer literals (100 tests)
     */
    private static void testOverflowIntegerErrMsg()  throws IOException {
        currTest = "Overflow Integer Error Message";

        ByteArrayOutputStream outputStream = switchPrintStream();

        Iterator<Token> iterator =
            new TokenStream(TokenType.INVALID_INTLIT).iterator();

        Token token;
        String output;

        for (int i = 0; i < 100; i++) {
            outputStream.reset();
            CharNum.num = 1;
            new Yylex(new StringReader(iterator.next().token())).next_token();

            output = outputStream.toString().trim();

            assertTrue(output.contains("WARNING"));
            assertTrue(output.contains(badIntLit));
        }

        resetPrintStream();
    }

    /**
     * Tests the error messages on illegal characters (100 tests)
     */
    private static void testIllegalCharacterErrMsg() throws IOException {
        currTest = "Test Overflow Integer Error Message";

        ByteArrayOutputStream outputStream = switchPrintStream();

        Iterator<Token> iterator =
            new TokenStream(TokenType.ILLEGAL_CHARACTERS).iterator();

        Token token;
        String output;

        for (int i = 0; i < 100; i++) {
            outputStream.reset();
            CharNum.num = 1;
            token = iterator.next();
            new Yylex(new StringReader(token.token())).next_token();

            output = outputStream.toString().trim();

            assertTrue(output.contains("ERROR"));
            assertTrue(output.endsWith(illegalChar + token.token()));
        }

        resetPrintStream();
    }

    /**
     * Fuzz Tests the Scanner by giving it random input across all tokens, both
     * valid and invalid (100 tests)
     */
    private static void testRandomTokens() throws IOException {
        currTest = "Fuzz";

        TokenStream tokenStream = new TokenStream(TokenType.RANDOM);
        Iterator<Token> iterator = tokenStream.iterator();

        Reader reader;
        Yylex scanner;

        //generate 100 random tokens
        for(int i = 0; i < 100; i++){
            CharNum.num = 1;
            Token token = iterator.next();
            reader = new StringReader(token.token());
            scanner = new Yylex(reader);

            Symbol symbol = scanner.next_token();

            assertEquals(symbol.sym, token.sym());

            switch (symbol.sym) {
            case sym.ID:
                assertEquals(((IdTokenVal)symbol.value).idVal, token.token());
                break;
            case sym.INTLITERAL:
                int val = Integer.parseInt(token.token());
                assertEquals(((IntLitTokenVal)symbol.value).intVal, val);
                break;
            case sym.STRLITERAL:
                assertEquals(((StrLitTokenVal)symbol.value).strVal, token.token());
                break;
            }
        }
    }

    /**
     * Helper function to check if two values are equal, and display an error
     * message if they are not.
     * Increments the number of tests run and the score value appropriately
     *
     * @param a LHS of the comparison
     * @param b RHS of the comparison
     */
    private final static void assertEquals(int a, int b) {
        testsRun++;
        if (a != b) {
            System.out.printf("%s Test Failed! %d != %d\n", currTest, a, b);
        } else {
            score++;
        }
    }

    /**
     * Helper function to check if two String values are equal, and display
     * an error message if they are not.
     * Increments the number of tests run and the score value appropriately
     *
     * @param a LHS of the comparison (assumed non null)
     * @param b RHS of the comparison (assumed non null)
     */
    private final static void assertEquals(String a, String b) {
        testsRun++;
        if (!a.equals(b)) {
            System.out.printf("%s Test Failed! \"%s\" != \"%s\"\n",
                currTest, a, b);
        } else {
            score++;
        }
    }

    /**
     * Helper function to check if a condition is true, and display an error
     * statement if it is not.
     * Increments the number of tests run and the score value appropriately
     *
     * @param b Result of a condition to be tested for {@code true}
     */
    private final static void assertTrue(boolean b) {
        testsRun++;
        if (!b) {
            System.out.printf("%s Test Failed!\n", currTest);
        } else {
            score++;
        }
    }
}

/**
 * @author Mrigank Kumar
 *
 * Representes a generated token
 *
 * @param  sym   The sym value of the token as in sym.java
 * @param  token The token as a String
 */
record Token(int sym, String token) {}

/**
 * @author Mrigank Kumar
 *
 * Represents the different types of Tokens that can be  generated
 * by the {@code TokenStream}'s Iterator
 */
enum TokenType {
    /* Good values */
    COMMENTS,
    KEYWORDS,
    OPERATORS,
    VALID_IDENTIFIERS,
    VALID_INTLIT,
    VALID_STRLIT,
    VALID_TOKENS,
    WHITESPACE,

    /* Bad Values */
    BAD_ESCAPE_STRLIT,
    ILLEGAL_CHARACTERS,
    INVALID_INTLIT,
    INVALID_STRLIT,
    UNTERMINATED_BAD_ESCAPE_STRLIT,
    UNTERMINATED_STRLIT,

    /* Both Good and Bad Values */
    RANDOM;
}

/**
 * @author Mrigank Kumar
 *
 * Represents a stream of tokens
 */
class TokenStream implements Iterable<Token> {
    private final TokenType type;

    /**
     * Constructs a new TokenStream with type set to {@code TokenType.RANDOM}
     */
    public TokenStream() {
        this.type = TokenType.RANDOM;
    }

    /**
     * Constructs a new TokenStream with the specified {@code TokenType}
     *
     * @param type the TokenType of the TokenStream
     */
    public TokenStream(TokenType type) {
        this.type = type;
    }

    /**
     * Returns an iterator over the tokens in this stream
     *
     * @return an iterator over the tokens in this stream
     */
    public Iterator<Token> iterator() {
        return new TokenStreamIterator(this.type);
    }
}

/**
 * @author Mrigank Kumar
 *
 * An iterator over a stream of tokens
 */
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
    private static final String WHITESPACE;
    private static final String ILLEGAL_CHARACTERS;
    private static final Token[][] KNOWN_TOKENS;


    static {
        /* Set constant values */
        MIN_TOKEN_LENGTH = 1;
        MAX_TOKEN_LENGTH = 32;
        MIN_CHAR = 32;
        MAX_CHAR = 127;
        N_CASES = 7;
        INVALID_SYM = 0;
        ESCAPES = "nst'\\\"";
        WHITESPACE = " \n";

        /* Dynamically determine valid identifier charset */
        // +1 for underscore (_)
        CHARSET_SIZE = ('Z' - 'A' + 1) + ('z' - 'a' + 1) + ('9' - '0' + 1) + 1;
        CHARSET = new char[CHARSET_SIZE];

        // We initialize in ASCII order
        int i = 0;
        for (char c = '0'; c <= '9'; CHARSET[i++] = c++);
        for (char c = 'A'; c <= 'Z'; CHARSET[i++] = c++);
        CHARSET[i++] = '_';
        for (char c = 'a'; c <= 'z'; CHARSET[i++] = c++);

        /* Initialize known tokens, which are keywords and operators */
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

        /* Dynamically determine illegal characters */
        char[] illegal = new char[127-32];

        i = 0; // reset variable

        // We skip the first 32 chars as they are non-printable
        // We only go upto 126, as 127 is DEL, which is also non-printable
        outer: for (char c = (char)33; c < 127; c++) {
            // Letters and digits are always valid characters
            if (Character.isLetterOrDigit(c)) {
                continue;
            }

            // Skip known valid characters, but not necessarily valid tokens
            switch (c) {
                case '_': // Legal, but doesn't fall in any category above
                case '!': // Comment character, not valid token on it's own
                case '$': // Comment character, not valid token on it's own
                case '"': // String termination character is valid
                    continue;
            }

            // Skip operator characters
            for (Token t: KNOWN_TOKENS[1]) {
                if (t.token().indexOf(c) >= 0) {
                    continue outer;
                }
            }

            // If none of the above match, it's an illegal character
            illegal[i++] = c;
        }

        // Reduce size of the char array to compactly fit
        char[] illegal_subset = new char[i];

        for (int j = 0; j < i; j++) {
            illegal_subset[j] = illegal[j];
        }

        // Create a string with the given
        ILLEGAL_CHARACTERS = new String(illegal_subset);
    }

    private final TokenType type;
    private int cur;
    private int idx = -1;

    /**
     * Constructs a new TokenStreamIterator with the specified TokenType.
     *
     * @param type the TokenType of the TokenStreamIterator
     */
    public TokenStreamIterator(TokenType type) {
        this.type = type;
        cur = 0;
        if (type == TokenType.KEYWORDS) {
            idx = 0;
        } else if (type == TokenType.OPERATORS) {
            idx = 1;
        }
    }

    /**
     * Generates a random number in the range [min, max)
     *
     * @param  min Lower bound of the random number (inclusive)
     * @param  max Upper bound of the random number (exclusive)
     *
     * @return random number in the range [min, max)
     */
    private static int rng(int min, int max) {
        return (int) (random() * (max - min)) + min;
    }

    /**
     * Generates a random number in the range [0, max)
     *
     * @param  max Upper bound of the random number (exclusive)
     *
     * @return random number in the range [0, max)
     */
    private static int rng(int max) {
        return (int) (random() * max);
    }

    /**
     * Whether there are more elements in the iterator
     *
     * @return {@code true} if there are more elements in the iterator,
     *         {@code false} otherwise.
     */
    public boolean hasNext() {
        switch (type) {
        case KEYWORDS:
        case OPERATORS:
            return cur < KNOWN_TOKENS[idx].length;
        default:
            return true;
        }
    }

    /**
     * The next token in the iterator
     *
     * @return The next token
     */
    public Token next() {
        switch (type) {
        case VALID_TOKENS:
            switch(rng(6)) {
            case 0:
                return KNOWN_TOKENS[0][rng(KNOWN_TOKENS[0].length)];
            case 1:
                return KNOWN_TOKENS[1][rng(KNOWN_TOKENS[1].length)];
            case 2:
                return new Token(sym.INTLITERAL, generateValidInteger());
            case 3:
                return new Token(sym.STRLITERAL, generateValidString());
            case 4:
                return new Token(sym.ID, generateValidIdentifier());
            case 5:
                return new Token(INVALID_SYM, generateComments());
            default:
                throw new RuntimeException("Unreachable statement reached!");
            }
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

        case UNTERMINATED_STRLIT:
            return new Token(INVALID_SYM, generateUnterminatedString());

        case BAD_ESCAPE_STRLIT:
            return new Token(INVALID_SYM, generateBadEscapeString());

        case UNTERMINATED_BAD_ESCAPE_STRLIT:
            return new Token(INVALID_SYM, generateUnterminatedBadEscapeString());

        case VALID_IDENTIFIERS:
            return new Token(sym.ID, generateValidIdentifier());

        case COMMENTS:
            return new Token(INVALID_SYM, generateComments());

        case WHITESPACE:
            return new Token(INVALID_SYM, generateWhitespace());

        case ILLEGAL_CHARACTERS:
            return new Token(INVALID_SYM, generateIllegalCharacters());

        case RANDOM:
            switch (rng(N_CASES)) {
            case 0: /* Keywords */
                return KNOWN_TOKENS[0][rng(KNOWN_TOKENS[0].length)];

            case 1: /* Operators */
                return KNOWN_TOKENS[1][rng(KNOWN_TOKENS[1].length)];

            case 2: /* Integer literals */
                return new Token(sym.INTLITERAL, generateValidInteger());

            case 3: /* Invalid integer literals */
                return new Token(INVALID_SYM, generateInvalidInteger());

            case 4: /* String literals */
                return new Token(sym.STRLITERAL, generateValidString());

            case 5: /* Invalid String literals */
                return new Token(INVALID_SYM, generateInvalidString());

            case 6: /* Identifiers */
                return new Token(sym.ID, generateValidIdentifier());
            }
        }

        return null;
    }

    /**
     * Generate a valid Integer Literal token
     *
     * @return an integer as a String
     */
    private final static String generateValidInteger() {
        return String.valueOf(rng(Integer.MAX_VALUE));
    }

    /**
     * Generate a invalid Integer Literal token
     *
     * @return an invalid integer as a String
     */
    private final static String generateInvalidInteger() {
        String badNum = String.valueOf(Integer.MAX_VALUE);

        int extra = rng(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
        while (extra-- > 0) {
            badNum += rng(10);
        }

        return badNum;
    }

    /**
     * Generate a valid String Literal token
     *
     * @return a String Literal token as a String
     */
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

    /**
     * Generate a invalid String Literal token
     *
     * @return an invalid String literal token as a String
     */
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

    /**
     * Generate an unterminated String literal
     *
     * @return an unterminated String literal token as a String
     */
    private final static String generateUnterminatedString() {
        String valid = generateValidString();
        return valid.substring(0, valid.length() - 1);
    }

    /**
     * Generate a badly escaped String literal
     *
     * @return a badly escaped String literal token as a String
     */
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

    /**
     * Generate unterminated and badly escaped String
     *
     * @return an unterminated and badly escaped String token as a String
     */
    private final static String generateUnterminatedBadEscapeString() {
        String badEscape = generateBadEscapeString();
        return badEscape.substring(0, badEscape.length() - 1);
    }

    /**
     * Generate a valid identifier
     *
     * @return a valid identifier token as a String
     */
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

    /**
     * Generate Comments
     *
     * @return a comment as a String literal
     */
    private final static String generateComments() {
        String str = null;
        do {
            str = rng(10) < 5 ? generateValidString() : generateInvalidString();
        } while (str.length() < 3);

        int splice_left = rng(0, str.length() / 2);
        int splice_right = rng(str.length() / 2, + 1);

        String comment = rng(10) < 5 ? "!!" : "$";
        if (random() < 0.5) {
            comment += " ";
        }

        return comment + str;
    }

    /**
     * Generate Whitespace
     *
     * @return a Whitespace token as a String
     */
    private final static String generateWhitespace() {
        int len = rng(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
        char[] buf = new char[len];

        for (int i = 0; i < len; i++) {
            buf[i] = WHITESPACE.charAt(rng(WHITESPACE.length()));
        }

        return new String(buf);
    }

    /**
     * Generate illegal characters
     *
     * @return an illegal character token as a String
     */
    private final static String generateIllegalCharacters() {
        char c = ILLEGAL_CHARACTERS.charAt(rng(ILLEGAL_CHARACTERS.length()));
        return String.valueOf(c);
    }
}
