import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mrigank Kumar
 *
 * Tests the functionality of the {@code Sym} and {@code SymTable} classes
 */
public final class P1 {
    private final static String TYPE = "int";
    private final static Sym SYM = new Sym(TYPE);
    private final static int N_TESTS = 92;
    private final static PrintStream defaultPrintStream = System.out;

    private int score;
    private int testsRun;
    private String currentTest;

    public P1() {
        currentTest = null;
        score = 0;
        testsRun = 0;
    }

    private final void printError(final String msg) {
        System.out.println(currentTest + " Failed! " + msg);
        System.out.println();
    }

    private final static ByteArrayOutputStream switchPrintStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(outputStream);

        System.setOut(stream);

        return outputStream;
    }

    private final static void resetPrintStream() {
        System.setOut(defaultPrintStream);
    }

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

    private final void checkSymbolString(String[][] symbols,
        HashMap<String, String> testMap, String content) throws Exception {
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

    public final void testSymTablePrint() throws Exception {
        currentTest = "SymTable Print";

        Pattern pattern = Pattern.compile("\\+{4}\\s*SYMBOL\\s*TABLE\\s*\\"
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

            Pattern multiScopePattern = Pattern.compile("\\+{4}\\s*SYMBOL\\s*"
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
        } catch (Exception e) {
            tester.printError("(Unexpected Exception)");
            e.printStackTrace();
        }

        tester.printScore();
    }
}
