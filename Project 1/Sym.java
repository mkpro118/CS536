/**
 * @author Mrigank Kumar
 *
 * Container for the metadata of a Symbol.
 */
public class Sym {

    private final String type;

    /**
     * Initialize the Sym with the given type
     *
     * @param  type A String representation of the type of the symbol
     */
    public Sym(final String type) {
        this.type = type;
    }

    /**
     * Accessor for the symbol's type
     *
     * @return The type of the symbol
     */
    public String getSym() {
        return this.type;
    }

    /**
     * Returns a string representation of the metadata of the symbol
     * Current implementation returns the type of the symbol
     *
     * @return String representation of the symbol's metadata
     */
    @Override
    public String toString() {
        return this.type;
    }
}
