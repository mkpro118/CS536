import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
* @author Mrigank Kumar
*
* Represents a symbol table in a compiler
*/
public class SymTable {
    /**
    * The symbol table is a {@code LinkedList} of {@code HashMap}s.
    * The maps store a {@code String}/{@code Sym} pair,
    * Which map the names of the symbol to their associated metadata.
    */
    private List<HashMap<String, Sym>> table;

    /**
    * Initializes a symbol table with one scope
    */
    public SymTable() {
        table = new LinkedList<>();
        table.add(new HashMap<>());
    }

    /**
    * Adds a new scope to the top of the symbol table
    */
    public void addScope() {
        this.table.add(0, new HashMap<>());
    }

    /**
    * Removes the first local scope from the symbol table
    *
    * @throws EmptySymTableException If there are no entries in the symbol table
    */
    public void removeScope() throws EmptySymTableException {
        checkNonEmpty();

        table.remove(0);
    }

    /**
    * Returns Sym instance associated with the given symbol name
    * if it exists in the <i>first</i> scope, {@code null} otherwise
    *
    * @param  name  The symbol to find in the first scope
    *
    * @return  The Sym instance for the given symbol name,
    *          {@code null} if not found
    *
    * @throws EmptySymTableException If there are no entries in the symbol table
    */
    public Sym lookupLocal(String name) throws EmptySymTableException {
        checkNonEmpty();
        checkNonNull(name);

        return this.table.get(0).get(name);
    }

    /**
    * Returns Sym instance associated with the given symbol name
    * if it exists in <i>any</i> scope, {@code null} otherwise
    *
    * @param  name  The symbol to find in the symbol table
    *
    * @return  The Sym instance for the given symbol name,
    *          {@code null} if not found
    *
    * @throws EmptySymTableException If there are no entries in the symbol table
    */
    public Sym lookupGlobal(String name) throws EmptySymTableException {
        checkNonEmpty();
        checkNonNull(name);

        for (HashMap<String, Sym> scope : table) {
            Sym sym = scope.get(name);

            if (sym != null) {
                return sym;
            }
        }

        return null;
    }

    /**
    * Add a declaration to the first scope
    *
    * @param  name Name of the symbol
    * @param  sym  Associated metadata of the given symbol
    *
    * @throws DuplicateSymNameException If the given name already exists in the first scope
    * @throws EmptySymTableException    If the symbol table has no available scopes
    */
    public void addDecl(String name, Sym sym)
            throws DuplicateSymNameException, EmptySymTableException {
        checkNonEmpty();
        checkNonNull(name, sym);

        HashMap<String, Sym> currScope = this.table.get(0);

        if (currScope.containsKey(name)) {
        throw new DuplicateSymNameException();
        }

        currScope.put(name, sym);
    }

    /**
    * Prints the Symbol Table for debugging purposes
    */
    public void print() {
        System.out.print("\n++++ SYMBOL TABLE\n");

        for (HashMap<String, Sym> scope: table) {
            System.out.print(scope.toString() + "\n");
        }

        System.out.print("\n++++ END TABLE\n");
    }

    /**
    * Checks if all the arguments are not {@code null}.
    * Throws an {@code IllegalArgumentException} is any argument is {@code null}.
    *
    * @param args Variable number of arguments to check for nullity
    *
    * @throws IllegalArgumentException If any of the {@code args} are {@code null}
    */
    private void checkNonNull(Object... args) {
    for (Object obj: args) {
    if (obj == null) {
    throw new IllegalArgumentException();
    }
    }
    }

    /**
    * Checks if the symbol table is not empty.
    * Throws an {@code EmptySymTableException} if the check fails.
    *
    * @throws EmptySymTableException If there are no entries in the symbol table
    */
    private void checkNonEmpty() throws EmptySymTableException {
    if (table.isEmpty()) {
    throw new EmptySymTableException();
    }
    }
}
