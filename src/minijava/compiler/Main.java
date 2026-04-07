package minijava.compiler;

import minijava.compiler.ast.*;
import minijava.compiler.semantic.SemanticAnalyzer;
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
        // Fase 1: Análise Léxica (Scanner)
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For debugging scanner:
        // for (Token token : tokens) {
        //     System.out.println(token);
        // }

        // Fase 2: Análise Sintática (Parser) - Constrói AST
        Parser parser = new Parser(tokens);
        Program ast = parser.parseProgram();

        boolean lexicalError = scanner.hadError();
        boolean syntaxError = parser.hadError();

        // Fase 3: Análise Semântica
        boolean semanticError = false;
        if (!lexicalError && !syntaxError && ast != null) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(ast);
            semanticAnalyzer.analyze();
            semanticError = semanticAnalyzer.hadError();
        }

        // Relatório final
        if (!lexicalError && !syntaxError && !semanticError) {
            System.out.println("Compilation completed successfully!");
        } else if (lexicalError && syntaxError && semanticError) {
            System.out.println("Compilation failed: lexical, syntax, and semantic errors detected.");
        } else if (lexicalError && syntaxError) {
            System.out.println("Compilation failed: lexical and syntax errors detected.");
        } else if (lexicalError && semanticError) {
            System.out.println("Compilation failed: lexical and semantic errors detected.");
        } else if (syntaxError && semanticError) {
            System.out.println("Compilation failed: syntax and semantic errors detected.");
        } else if (lexicalError) {
            System.out.println("Compilation failed: lexical errors detected.");
        } else if (syntaxError) {
            System.out.println("Compilation failed: syntax errors detected.");
        } else if (semanticError) {
            System.out.println("Compilation failed: semantic errors detected.");
        }
    }
}

