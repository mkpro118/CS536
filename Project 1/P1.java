import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.lang.Math.random;

/**
 * @author Mrigank Kumar
 *
 * Tests the functionality of the {@code Sym} and {@code SymTable} classes
 */
public final class P1 {
    // Default Symbol type
    private final static String TYPE = "int";
    // Default Symbol instance
    private final static Sym SYM = new Sym(TYPE);
    // Total number of tests
    private final static int N_TESTS = 100;
    // Default Print Stream
    private final static PrintStream defaultPrintStream = System.out;
    // Maximum length of a randomly generated string (16)
    private final static int MAX_STRING_LENGTH = 16;
    // Minimum number of iterations for the fuzz tests
    private final static int MIN_FUZZ_ITERS = 128;
    // Extra range of fuzz test iterations (1024 - 128 = 896)
    private final static int FUZZ_ITERS_RANGE = 1024 - MIN_FUZZ_ITERS;

    // Score over the tests
    private int score;
    // Numbers of tests executed
    private int testsRun;
    // Last run test
    private String currentTest;

    /**
     * Constructor to initialize a tester instance
     */
    public P1() {
        currentTest = null;
        score = 0;
        testsRun = 0;
    }

    /**
     * Print a formatted error message to the default {@code System.out}
     * PrintStream
     *
     * @param msg The error message to print
     */
    private final void printError(final String msg) {
        resetPrintStream();
        System.out.println(currentTest + " Failed! " + msg);
        System.out.println();
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

        System.setOut(stream);

        return outputStream;
    }

    /**
     * Resets the {@code System.out} PrintStream to stdout
     */
    private final static void resetPrintStream() {
        System.setOut(defaultPrintStream);
    }

    /**
     * Parses a String of the format
     * "name1=type1, name2=type, ..., nameN=typeN"
     * To compare against the given hashmap for key-value pairs
     *
     * @param  symbols   List of all symbols
     * @param  testMap   HashMap containing key-value pairs for the symbols
     * @param  content   The string to parse
     */
    private final void checkSymbolString(String[][] symbols,
        HashMap<String, String> testMap, String content) {
        String[] pairs = content.split(",");

        testsRun++;
        if (pairs.length != symbols.length) {
            printError("Mismatched number of symbols.\n"
                + "Expected: " + symbols.length + " symbols, " + testMap
                + "\nActual:   " + pairs.length + " symbols, {"
                + content+ "}");
            return;
        }
        score++;

        for (String pair : pairs) {
            String[] data = pair.split("=");

            testsRun++;
            if (data.length != 2) {
                printError("Invalid format!\n"
                    + "Expected format is \"name=type\"\n"
                    + "Actual contents: \"" + pair + "\"");
                return;
            }
            score++;

            String key = data[0].trim();
            String value = data[1].trim();

            testsRun++;
            if (!testMap.containsKey(key)) {
                printError("Unexpected symbol!\n"
                    + "Found unexpected symbol \"" + key + "\"\n"
                    + "Expected symbols are " + testMap);
                return;
            }
            score++;

            testsRun++;
            String expectedValue = testMap.get(key);
            if (!expectedValue.equals(value)) {
                printError("Incorrect mapping!\n"
                    + "Expected symbol \"" + key + "\" to have type \""
                    + expectedValue + "\"\n"
                    + "Found type \"" + value + "\"");
                return;
            }
            score++;
        }
    }

    /**
     * Generates a random string of alphabets for fuzz testing
     *
     * @return Randomly generated string of length lesser than or equal to 16
     */
    private final static String generateRandomString() {
        final int length = (int) (random() * MAX_STRING_LENGTH) + 1;

        String random = "";

        for (int i = 0; i < length; i++) {
            char start = (random() > 0.5 ? 'a' : 'A');
            random += (char) (start + (char)(random() * 26));
        }

        return random;
    }

    /**
     * Tests the {@code Sym} class constructor
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymConstructor() throws Exception {
        currentTest = "Sym Constructor";

        // 1. Valid input
        {
            testsRun++;

            Sym sym = SYM;
            score++;
        }

        // 2. Invalid input
        {
            testsRun++;

            try {
                Sym sym = new Sym(null);  // Should throw an exception

                printError("Sym Constructor did not throw an Exception "
                    + "when called with an invalid argument (null)");
            } catch (Exception unused) {
                score++;
            }
        }
    }

    /**
     * Tests the {@code Sym.getSym()} method
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymAccessor() throws Exception {
        currentTest = "Sym Accessor";
        Sym sym;
        String symType;

        // 1. Accessor should not return null
        {
            testsRun++;
            sym = SYM;
            symType = sym.getSym();

            if (symType == null) {
                printError("Sym.getSym() returned null! Expected: " + TYPE);
                return;
            }

            score++;
        }

        // 2. Accessor should return the expected type value
        {
            testsRun++;
            if (!TYPE.equals(symType)) {
                printError("Sym.getSym() returned " + symType
                    + "! Expected: " + TYPE);
                return;
            }

            score++;
        }
    }

    /**
     * Tests the {@code Sym.toString()} method
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymToString() throws Exception {
        currentTest = "Sym ToString";
        Sym sym;
        String symType;

        // 1. toString should not return null
        {
            testsRun++;
            sym = SYM;
            symType = sym.getSym();

            if (symType == null) {
                printError("Sym.getSym() returned null! Expected: " + TYPE);
                return;
            }

            score++;
        }

        // 2. toString should return the correct string representation
        {
            testsRun++;
            if (!TYPE.equals(symType)) {
                printError("Sym.getSym() returned " + symType
                    + "! Expected: " + TYPE);
                return;
            }

            score++;
        }
    }

    /**
     * Tests the {@code SymTable} class constructor
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableConstructor() throws Exception {
        currentTest = "SymTable Constructor";
        SymTable table;

        // 1. Shouldn't throw exceptions
        {
            testsRun++;

            table = new SymTable(); // unused var

            score++;
        }
    }

    /**
     * Tests the {@code SymTable.addScope()} and {@code SymTable.removeScope()}
     * methods
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableAddRemoveScope() throws Exception {
        currentTest = "SymTable Add/Remove Scope";
        SymTable table = new SymTable();

        // 1. Add a new scope
        {
            testsRun++;
            table.addScope();
            score++;
        }

        // 2. Remove the new scope
        {
            testsRun++;
            table.removeScope();
            score++;
        }

        // 3. Add another new scope
        {
            testsRun++;
            table.addScope();
            score++;
        }

        // 4. Remove scope
        {
            testsRun++;
            table.removeScope();
            score++;
        }

        // 5. Remove global scope
        {
            testsRun++;
            table.removeScope();
            score++;
        }

        // 6. Remove scope from empty table
        {
            try {
                testsRun++;
                table.removeScope();
                printError("Expected EmptySymTableException");
            } catch (EmptySymTableException unused) {
                score++;
            }
        }
    }

    /**
     * Tests the {@code SymTable.addDecl()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableAddDecl() throws Exception {
        currentTest = "SymTable Add Declaration";

        SymTable table = new SymTable();

        // 1. Invalid params, name is null
        {
            testsRun++;
            try {
                table.addDecl(null, SYM);
                printError("Expected IllegalArgumentException");
            } catch (IllegalArgumentException unused) {
                score++;
            }
        }

        // 2. Invalid params, sym is null
        {
            testsRun++;
            try {
                table.addDecl("x", null);
                printError("Expected IllegalArgumentException");
            } catch (IllegalArgumentException unused) {
                score++;
            }
        }

        // 3. Invalid params, both are null
        {
            testsRun++;
            try {
                table.addDecl(null, null);
                printError("Expected IllegalArgumentException");
            } catch (IllegalArgumentException unused) {
                score++;
            }
        }

        // 4. Global Declaration
        {
            testsRun++;

            table.addDecl("x", SYM);

            score++;
        }

        // 5. Duplicate global declaration
        {
            try {
                testsRun++;

                table.addDecl("x", SYM);

                printError("Expected DuplicateSymNameException");
            } catch (DuplicateSymNameException unused) {
                score++;
            }
        }

        // 6. Add a local scope and declaration
        {
            testsRun++;

            table.addScope();
            table.addDecl("y", SYM);

            score++;
        }

        // 7. Duplicate declaration in local scope
        {
            try {
                testsRun++;

                table.addDecl("y", SYM);

                printError("Expected DuplicateSymNameException");
            } catch (DuplicateSymNameException unused) {
                score++;
            }
        }

        // 8. Shadow declaration in local scope
        {
            testsRun++;

            table.addDecl("x", SYM);

            score++;
        }

        // 9. Remove local and global scopes, add declaration to empty table
        {
            table.removeScope(); // remove local scope
            table.removeScope(); // remove global scope

            testsRun++;

            try {
                table.addDecl("x", SYM);
                printError("Expected EmptySymTableException");
            } catch (EmptySymTableException unused) {
                score++;
            }
        }
    }

    /**
     * Tests the {@code SymTable.lookupLocal()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableLookupLocal() throws Exception {
        currentTest = "SymTable Lookup Local";
        SymTable table = new SymTable();

        table.addDecl("x", SYM); // In Global Scope

        // 1. Add a declaration, lookup the same
        {
            table.addScope(); // First local scope

            String expected = "double";
            table.addDecl("x", new Sym(expected));

            testsRun++;

            Sym sym = table.lookupLocal("x");
            String type = sym.getSym();


            if (!expected.equals(type)) {
                printError("Expected sym type: " + expected
                            + ". Actual sym type: " + type);
                return;
            }
            score++;
        }

        // 2. Add another scope and declaration, lookup new local scope
        {
            table.addScope(); // Second local scope

            String expected = "boolean";
            table.addDecl("x", new Sym(expected));

            testsRun++;

            Sym sym = table.lookupLocal("x");
            String type = sym.getSym();


            if (!expected.equals(type)) {
                printError("Expected sym type: " + expected
                            + ". Actual sym type: " + type);
                return;
            }
            score++;
        }

        // 3. Lookup symbol that does not exist
        {
            testsRun++;

            Sym sym = table.lookupLocal("y");

            if (sym != null) {
                printError("Expected sym to be null. Actual sym: "
                           + sym);
                return;
            }

            score++;
        }

        // 4. Remove all scopes, lookup on empty table
        {
            // 2 local scopes + 1 global scope
            for (int i = 0; i < 3; i++) {
                table.removeScope();
            }

            testsRun++;

            try {
                table.lookupLocal("x");
                printError("Expected EmptySymTableException");
                return;
            } catch (EmptySymTableException unused) {
                score++;
            }
        }
    }

    /**
     * Tests the {@code SymTable.lookupGlobal()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableLookupGlobal() throws Exception {
        currentTest = "SymTable Lookup Global";
        SymTable table = new SymTable();

        String[][] data = {
            {"i", "int"},
            {"l", "long"},
            {"d", "double"},
            {"f", "float"},
            {"b", "boolean"}
        };

        for (int i = 0; i < data.length; i++) {
            if (i % 3 == 2) {
                table.addScope();
            }

            table.addDecl(data[i][0], new Sym(data[i][1]));
        }

        // 1. Lookup all syms
        {
            for (String[] symbols: data) {
                testsRun++;
                Sym sym = table.lookupGlobal(symbols[0]);

                if (!symbols[1].equals(sym.getSym())) {
                    printError("Expected sym: " + symbols[1]
                               + ". Actual sym: " + sym);
                    return;
                }

                score++;
            }
        }

        // 2. Lookup syms that do not exist
        {
            String[] badSyms = {"q", "w", "e", "p"};

            for (String badSym: badSyms) {
                testsRun++;

                Sym sym = table.lookupGlobal(badSym);
                if (sym != null) {
                    printError("Expected sym to be null. Actual sym:"
                               + sym);
                    return;
                }

                score++;
            }
        }
    }

    /**
     * Tests the {@code SymTable.print()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTablePrint() throws Exception {
        currentTest = "SymTable Print";

        Pattern pattern = compile("\\+{4}\\s*SYMBOL\\s*TABLE\\s*\\"
                + "{(.*)\\}\\s*\\+{4}\\s*END\\sTABLE");

        // 1. Symbol Table with global scope, no declarations
        {
            SymTable table = new SymTable();

            ByteArrayOutputStream outputStream = switchPrintStream();

            testsRun++;
            table.print();

            resetPrintStream();

            String output = outputStream.toString().trim();
            Matcher matcher = pattern.matcher(output);

            if (!matcher.find() || matcher.group(1).trim().length() != 0) {
                printError("Output mismatch!\n"
                    + "Expected output should contain:\n"
                    + "\"++++ SYMBOL TABLE\n{}\n++++ END TABLE\"\n"
                    + "Actual output:\n\"" + output + "\"");
                return;
            }

            score++;
        }

        // 2. Symbol Table with global scope, one declaration
        {
            SymTable table = new SymTable();
            table.addDecl("x", SYM);

            ByteArrayOutputStream outputStream = switchPrintStream();

            testsRun++;
            table.print();

            resetPrintStream();

            String output = outputStream.toString().trim();
            String expected = "x=int";
            Matcher matcher = pattern.matcher(output);

            if (!matcher.find() || !expected.equals(matcher.group(1).trim())) {
                printError("Output mismatch!\n"
                    + "Expected output should contain:\n"
                    + "\"++++ SYMBOL TABLE\n{"+ expected
                    + "}\n++++ END TABLE\"\n"
                    + "Actual output:\n\"" + output + "\"");
                return;
            }

            score++;
        }

        // 3. Symbol Table with global scope, multiple declarations
        {
            SymTable table = new SymTable();
            String[][] symbols = new String[][] {
                {"b", "boolean"},
                {"c", "char"},
                {"d", "double"},
                {"f", "float"},
                {"i", "int"},
                {"l", "long"},
                {"s", "short"},
            };

            final int last = symbols.length - 1;

            HashMap<String, String> testMap = new HashMap<>();

            for (String[] symbol: symbols) {
                Sym sym = new Sym(symbol[1]);
                testMap.put(symbol[0], symbol[1]);
                table.addDecl(symbol[0], sym);
            }

            ByteArrayOutputStream outputStream = switchPrintStream();

            testsRun++;
            table.print();

            resetPrintStream();

            String output = outputStream.toString().trim();
            Matcher matcher = pattern.matcher(output);

            if (!matcher.find()) {
                printError("Output mismatch!\n"
                    + "Expected output should contain:\n"
                    + "\"++++ SYMBOL TABLE\n{" + testMap
                    + "}\n++++ END TABLE\"\n"
                    + "Actual output:\n\"" + output + "\"");
                return;
            }
            score++;

            String content = matcher.group(1);
            checkSymbolString(symbols, testMap, content);
        }

        // 4. Symbol Table with multiple scopes, multiple declarations
        {
            SymTable table = new SymTable();

            // In global scope
            table.addDecl("i", new Sym("int"));

            String[][][] symbols= {
                {{"i", "int"}, {"l", "long"}, {"s", "short"}},
                {{"c", "char"}, {"b", "byte"}, {"l", "long"}},
                {{"b", "boolean"}, {"d", "double"}, {"f", "float"}},
            };

            final int numberOfScopes = 1 + symbols.length;

            ArrayList<HashMap<String, String>> testMapList
                = new ArrayList<>(numberOfScopes);

            testMapList.add(new HashMap<>());
            testMapList.get(0).put("i", "int");

            String expectedOutput = testMapList.get(0) + "\n";

            for (String[][] scope: symbols) {
                table.addScope();
                HashMap<String, String> testMap = new HashMap<>();

                for (String[] symbol: scope) {
                    testMap.put(symbol[0], symbol[1]);
                    table.addDecl(symbol[0], new Sym(symbol[1]));
                }
                testMapList.add(testMap);
                expectedOutput = testMap + "\n" + expectedOutput;
            }

            Pattern multiScopePattern = compile("\\+{4}\\s*SYMBOL\\s*"
                +"TABLE\\s*((\\{.*\\}\\s*){" + numberOfScopes
                + "})\\s*\\+{4}\\s*END\\s*TABLE");

            ByteArrayOutputStream outputStream = switchPrintStream();

            testsRun++;
            table.print();

            resetPrintStream();

            String output = outputStream.toString().trim();
            Matcher matcher = multiScopePattern.matcher(output);

            if (!matcher.find()) {
                printError("Output mismatch!\n"
                    + "Expected output should contain:\n"
                    + "\"++++ SYMBOL TABLE\n" + expectedOutput
                    + "\n++++ END TABLE\"\n"
                    + "Actual output:\n\"" + output + "\"");
                return;
            }
            score++;


            String content = matcher.group(1).trim();
            String[] lines = content.split("\n");

            testsRun++;
            if (lines.length != numberOfScopes) {
                printError("Mismatched number of scopes.\n"
                    + "Expected: " + numberOfScopes + " scopes, " + testMapList
                    + "\nActual:   " + lines.length + " scopes, " + content);
                return;
            }
            score++;

            for (int i = 0; i < numberOfScopes - 1; i++) {
                String line = lines[i].substring(1, lines[i].length() - 1);

                HashMap<String, String> testMap =
                    testMapList.get(numberOfScopes - i - 1);

                String[][] symbolsInScope = symbols[i];

                checkSymbolString(symbolsInScope, testMap, line);
            }
        }
    }

    /**
     * Fuzz tests the {@code Sym} class constructor
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymConstructorFuzz() throws Exception {
        currentTest = "Sym Constructor Fuzz";

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;

        testsRun++;
        for (int i = 0; i < iters; i++) {
            new Sym(generateRandomString());
        }
        score++;
    }

    /**
     * Fuzz tests the {@code Sym.getSym()} method
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymAccessorFuzz() throws Exception {
        currentTest = "Sym Accessor Fuzz";

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;

        testsRun++;
        for (int i = 0; i < iters; i++) {
            String randomType = generateRandomString();
            String actual = (new Sym(randomType)).getSym();
            if (!randomType.equals(actual)) {
                printError("getSym() returned the wrong sym\n"
                    + "Expected: " + randomType + "\n"
                    + "Actual:   " + actual);
                return;
            }
        }
        score++;
    }

    /**
     * Fuzz tests the {@code Sym.toString()} method
     *
     * @throws Exception Propagates from the {@code Sym} class
     */
    public final void testSymToStringFuzz() throws Exception {
        currentTest = "Sym Accessor Fuzz";

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;

        testsRun++;
        for (int i = 0; i < iters; i++) {
            String randomType = generateRandomString();
            String actual = (new Sym(randomType)).toString();
            if (!randomType.equals(actual)) {
                printError("getSym() returned the wrong sym\n"
                    + "Expected: " + randomType + "\n"
                    + "Actual:   " + actual);
                return;
            }
        }
        score++;
    }

    /**
     * Fuzz tests the {@code SymTable.addDecl()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableAddDeclFuzz() throws Exception {
        currentTest = "SymTable Add Declaration Fuzz";

        SymTable table = new SymTable();

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;

        HashSet<String> symbolSet = new HashSet<>();

        testsRun++;
        while (symbolSet.size() < iters) {
            String name;
            do {
                name = generateRandomString();
            }  while (symbolSet.contains(name));
            symbolSet.add(name);
            table.addDecl(name, new Sym(name));
        }
        score++;

        String[] symbols = new String[iters];
        symbols = symbolSet.toArray(symbols);

        // Duplicates
        final int dups = (int) (random() * iters) + MIN_FUZZ_ITERS;

        testsRun++;
        for (int i = 0; i < dups; i++) {
            final int index = (int) (random() * iters);
            try {
                table.addDecl(symbols[index], new Sym(symbols[index]));
                printError("Expected DuplicateSymNameException");
            } catch (DuplicateSymNameException e) {}
        }
        score++;
    }

    /**
     * Fuzz tests the {@code SymTable.addScope()} and
     * {@code SymTable.removeScope()} methods
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableAddRemoveScopeFuzz() throws Exception {
        currentTest = "SymTable Add/Remove Fuzz";

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;

        SymTable table = new SymTable();
        int stack = 1;

        testsRun++;
        for (int i = 0; i < iters; i++) {
            if (random() > 0.5) {
                try {
                    table.removeScope();
                    stack--;
                    if (stack < 0) {
                        printError("Expected EmptySymTableException");
                        return;
                    }
                } catch (EmptySymTableException e) {
                    if (stack > 0) {
                        throw e;
                    }
                }
            } else {
                table.addScope();
                stack++;
            }

        }
        score++;
    }

    /**
     * Fuzz tests the {@code SymTable.lookupLocal()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableLookupLocalFuzz() throws Exception {
        currentTest = "SymTable Lookup Local Fuzz";

        final int numberOfScopes = (int) (random() * 75) + 25;
        final int numberOfSymbols = (int) (random() * 75) + 25;

        String[] symbols = new String[numberOfSymbols];

        SymTable table = new SymTable();

        for (int i = 0; i < numberOfSymbols; i++) {
            symbols[i] = generateRandomString();
        }

        testsRun++;
        for (int i = 0; i < numberOfScopes - 1; i++) {
            table.addScope();

            final int nSyms = (int) (random() * 15) + 5;
            HashMap<String, String> usedSymbols = new HashMap<>();

            for (int j = 0; j < nSyms; j++) {

                String name = symbols[(int) (random() * numberOfSymbols)];
                String type = symbols[(int) (random() * numberOfSymbols)];

                if (usedSymbols.containsKey(name)) {
                    continue;
                }

                usedSymbols.put(name, type);
                table.addDecl(name, new Sym(type));
            }

            for (String symbol: symbols) {
                String type = usedSymbols.get(symbol);

                if (type != null) {
                    String actual = table.lookupLocal(symbol).toString();
                    if (!type.equals(actual)) {
                        printError("Wrong type found!\n"
                            + "Expected: " + type + "\n"
                            + "Actual:   " + actual);
                        return;
                    }
                } else {
                    if (table.lookupLocal(symbol) != null) {
                        printError("Unexpected symbol found in local scope!\n"
                            + "Symbol " + symbol + " should not exist.\n"
                            + "Symbols in current scope are: "
                            + usedSymbols.keySet());
                        return;
                    }
                }
            }
        }
        score++;
    }

    /**
     * Fuzz tests the {@code SymTable.lookupGlobal()} method
     *
     * @throws Exception Propagates from the {@code SymTable} class
     */
    public final void testSymTableLookupGlobalFuzz() throws Exception {
        currentTest = "SymTable Lookup Local Fuzz";

        final int iters = (int) (random() * FUZZ_ITERS_RANGE) + MIN_FUZZ_ITERS;
        final int numberOfSymbols = (int) (random() * 75) + 25;

        String[] symbols = new String[numberOfSymbols];

        SymTable table = new SymTable();

        for (int i = 0; i < numberOfSymbols; i++) {
            symbols[i] = generateRandomString();
        }

        HashMap<String, String> usedSymbols = new HashMap<>();

        for (int i = 0; i < iters; i++) {
            if (random() > 0.85) {
                table.addScope();
            }

            String name;

            do {
                name = generateRandomString();
            } while (usedSymbols.containsKey(name));

            String type = generateRandomString();

            usedSymbols.put(name, type);

            table.addDecl(name, new Sym(type));
        }

        testsRun++;
        for (String name: usedSymbols.keySet()) {
            String type = usedSymbols.get(name);
            Sym sym = table.lookupGlobal(name);

            if (sym == null || !type.equals(sym.toString())) {
                printError("Sym not found!\n"
                    + "Expected: " + name + "=" + type + "\n"
                    + "Actual:   " + name + "=null");
                return;
            }
        }

        for (int i = 0; i < iters; i++) {
            String randomName;

            do {
                randomName = generateRandomString();
            } while(usedSymbols.containsKey(randomName));

            if (table.lookupGlobal(randomName) != null) {
                printError("Unexpected symbol found!\n"
                    + "Found symbol " + randomName + "\n"
                    + "List of symbols across all scopes are: " + usedSymbols);
            }
        }

        score++;
    }

    /**
     * Formats and prints the score from the tester
     */
    public final void printScore() {
        if (score > testsRun) {
            System.out.println("INVALID TESTER!!! Score is greater than the "
                + "number of tests");
            System.out.println("Score: " + score);
            System.out.println("Tests Run: " + testsRun);
            System.out.println("Total Number of Tests: " + N_TESTS);
            return;
        }

        String msg;
        if (testsRun != N_TESTS || score != testsRun) {
            msg = score + "/" + testsRun + " Tests Passed. ";
            if (N_TESTS > testsRun) {
                msg += (N_TESTS - testsRun) + " Tests Skipped";
            } else if (N_TESTS < testsRun) {
                msg += (testsRun - N_TESTS) + " Tests Re-Run";
            }
        } else {
            msg = "All Tests Passed! (" + score + "/" + testsRun + ")";
        }
        System.out.println(msg);
    }

    /**
     * Tester driver, runs the tests as follows
     *
     * +-------------------------------+------------+-------+
     * |              Test             |  # Points  | Total |
     * +-------------------------------+------------+-------+
     * | Sym Constructor               |  2 points  |    2  |
     * | Sym Accessor                  |  2 points  |    4  |
     * | Sym ToString                  |  2 points  |    6  |
     * |-------------------------------+------------+-------|
     * | SymTable Constructor          |  1 point   |    7  |
     * | SymTable Add/Remove Scope     |  6 points  |   13  |
     * | SymTable Add Declaration      |  9 points  |   22  |
     * | SymTable Lookup Local         |  4 points  |   26  |
     * | SymTable Lookup Global        |  9 points  |   35  |
     * | SymTable Print                | 57 points  |   92  |
     * |-------------------------------+------------+-------|
     * | Sym Constructor Fuzz          |  1 point   |   93  |
     * | Sym Accessor Fuzz             |  1 point   |   94  |
     * | Sym Accessor Fuzz             |  1 point   |   95  |
     * |-------------------------------+------------+-------|
     * | SymTable Add/Remove Fuzz      |  1 point   |   96  |
     * | SymTable Lookup Local Fuzz    |  1 point   |   97  |
     * | SymTable Lookup Local Fuzz    |  1 point   |   98  |
     * | SymTable Add Declaration Fuzz |  2 points  |  100  |
     * +-------------------------------+------------+-------+
     *
     * @param args unused
     */
    public final static void main(final String[] args) {
        P1 tester = new P1();

        try {
            tester.testSymConstructor();
            tester.testSymAccessor();
            tester.testSymToString();
            tester.testSymTableConstructor();
            tester.testSymTableAddRemoveScope();
            tester.testSymTableAddDecl();
            tester.testSymTableLookupLocal();
            tester.testSymTableLookupGlobal();
            tester.testSymTablePrint();
            tester.testSymConstructorFuzz();
            tester.testSymToStringFuzz();
            tester.testSymAccessorFuzz();
            tester.testSymTableAddRemoveScopeFuzz();
            tester.testSymTableLookupLocalFuzz();
            tester.testSymTableLookupGlobalFuzz();
            tester.testSymTableAddDeclFuzz();
        } catch (Exception e) {
            tester.printError("(Unexpected Exception)");
            e.printStackTrace();
        }

        tester.printScore();
    }
}
