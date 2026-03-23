package minijava.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: minijava [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            // Run a default test if no args
            String testCode = 
                "class Factorial {\n" +
                "    public static void main(String[] a) {\n" +
                "        System.out.println(new Fac().ComputeFac(10));\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class Fac {\n" +
                "    public int ComputeFac(int num) {\n" +
                "        int num_aux;\n" +
                "        if (num < 1)\n" +
                "            num_aux = 1;\n" +
                "        else\n" +
                "            num_aux = num * (this.ComputeFac(num - 1));\n" +
                "        return num_aux;\n" +
                "    }\n" +
                "}";
            System.out.println("Running built-in test code:");
            run(testCode);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes));
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For debugging scanner:
        // for (Token token : tokens) {
        //     System.out.println(token);
        // }

        Parser parser = new Parser(tokens);
        parser.parse();

        boolean lexicalError = scanner.hadError();
        boolean syntaxError = parser.hadError();

        if (!lexicalError && !syntaxError) {
            System.out.println("Parsing completed.");
        } else if (lexicalError && syntaxError) {
            System.out.println("Parsing failed: lexical and syntax errors detected.");
        } else if (lexicalError) {
            System.out.println("Parsing failed: lexical errors detected.");
        } else {
            System.out.println("Parsing failed: syntax errors detected.");
        }
    }
}

