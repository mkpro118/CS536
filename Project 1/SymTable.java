import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mrigank Kumar
 *
 * Represents a symbol table in a compiler
 */
public class SymTable {
    public SymTable() {

    }

    public void addDecl(String name, Sym sym) throws DuplicateSymNameException, EmptySymTableException {

    }

    public void addScope() {

    }

    public Sym lookupLocal(String name) throws EmptySymTableException {
        return null;
    }

    public Sym lookupGlobal(String name) throws EmptySymTableException {
        return null;
    }

    public void removeScope() throws EmptySymTableException {

    }

    public void print() {

    }
}
