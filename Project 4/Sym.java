public class Sym {
    protected String type;

    public Sym(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }

    public static void main(String[] args) {
        SymFunctional s = new SymFunctional("z");
        System.out.println(s);
    }
}

class SymFunctional extends Sym {
    final String[] paramTypes;
    final String repr;
    SymFunctional(String returnType, String... paramTypes) {
        super(returnType);
        this.paramTypes = paramTypes;
        String input = paramTypes.length != 0 ? String.join(",", paramTypes): " ";
        repr = "<" + input + "->" + type + ">";
    }

    @Override
    public String toString() {
        return repr;
    }
}
