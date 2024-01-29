
/**
 * @author Mrigank Kumar
 *
 * Tests the functionality of the {@code Sym} and {@code SymTable} classes
 */
public class P1 {
    private final static String type = "int";
    private final static int N_TESTS = 5;
    private int score;
    private int testsRun;
    private String currentTest;

    public P1() {
        currentTest = null;
        score = 0;
        testsRun = 0;
    }

    public void testSymConstructor() throws Exception {
        currentTest = "Sym Constructor";
        testsRun++;

        Sym sym = new Sym(type);  // Shouldn't throw exceptions
        score++;

        try {
            testsRun++;
            sym = new Sym(null);  // Should throw an exception
            System.out.println("Sym Constructor did not throw an Exception "
                + "when called with an invalid argument (null)");
        } catch (Exception unused) {
            score++;
        }
    }

    public void testSymAccessor() throws Exception {
        currentTest = "Sym Accessor";
        testsRun++;

        Sym sym = new Sym(type);

        String symType = sym.getSym();

        if (symType == null) {
            System.out.println("Sym.getSym() returned null! Expected: " + type);
            return;
        }

        if (!type.equals(symType)) {
            System.out.println("Sym.getSym() returned " + symType
                + "! Expected: " + type);
            return;
        }

        score++;
    }

    public void testSymToString() throws Exception {
        currentTest = "Sym ToString";
        testsRun++;

        Sym sym = new Sym(type);

        String symType = sym.toString();

        if (symType == null) {
            System.out.println("Sym.toString() returned null! Expected: " + type);
            return;
        }

        if (!type.equals(symType)) {
            System.out.println("Sym.toString() returned " + symType
                + "! Expected: " + type);
            return;
        }

        score++;
    }

    public void testSymTableConstructor() throws Exception {
        currentTest = "SymTable Constructor";
        testsRun++;

        // Shouldn't throw exceptions
        SymTable table = new SymTable(); // unused var

        score++;
    }

    public void testSymTableAddDecl() throws Exception {
        currentTest = 
    }

    public final void printScore() {
        String msg;
        if (testsRun != N_TESTS) {
            msg = score + "/" + testsRun + " Tests Passed. ";
            msg += (N_TESTS - testsRun) + " Tests Skipped";
        } else {
            msg = "All Tests Passed! (" + score + "/" + testsRun + ")";
        }
        System.out.println(msg);
    }

    public final static void main(String[] args) {
        P1 tester = new P1();

        try {
            tester.testSymConstructor();
            tester.testSymAccessor();
            tester.testSymToString();
            tester.testSymTableConstructor();
        } catch (Exception e) {
            System.out.println(tester.currentTest + "FAILED! "
                                + "(Unexpected Exception)");
            e.printStackTrace();
        }

        tester.printScore();
    }
}
