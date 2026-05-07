package minijava.compiler.temp;

public class Label {
    private String name;
    private static int count = 0;

    /**
     * Cria um novo label com um nome sequencial automático
     */
    public Label() {
        this.name = "L" + count++;
    }

    /**
     * Cria um novo label com um nome específico fornecido
     */
    public Label(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return name;
    }
}