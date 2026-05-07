package minijava.compiler.temp;

public class Temp {
    private static int count = 0;
    private final int num;

    public Temp() {
        this.num = count++;
    }

    @Override
    public String toString() {
        return "t" + num;
    }
}