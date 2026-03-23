# Compilador MiniJava

Este projeto implementa o front-end de um compilador para MiniJava (subconjunto de Java), com:

- **Scanner (analisador léxico):** transforma o código-fonte em tokens.
- **Parser LL (descida recursiva):** valida a sequência de tokens de acordo com a gramática implementada no código.
- **Precedência de operadores:** tratada pela estrutura dos métodos de expressão no parser.
- **Recuperação de erro (panic mode):** ao encontrar erro sintático, tenta sincronizar em pontos de fronteira.

## Como testar corretamente (PowerShell)

> Execute os comandos na **raiz do projeto** (`Construção de Compiladores`).

### 1) Compilar o compilador

```powershell
javac src/minijava/compiler/*.java
```

### 2) Rodar o teste embutido (sem passar arquivo)

```powershell
java -cp src minijava.compiler.Main
```

Quando você roda sem argumentos, o `Main` usa um código de teste interno (não lê `Factorial.java` do disco).

### 3) Rodar com um arquivo alvo (ex.: `Factorial.java`)

```powershell
java -cp src minijava.compiler.Main .\Factorial.java
```

Para testar um arquivo com erro proposital:

```powershell
java -cp src minijava.compiler.Main .\factorialWithError.java
```

## O que significa `-cp src`

- `-cp` significa **classpath**.
- `-cp src` diz para a JVM procurar classes compiladas a partir da pasta `src`.
- Isso é necessário porque a classe principal está no pacote `minijava.compiler`.

## Saída esperada e erros

- Em caso de sucesso, você verá `Parsing completed.`.
- Em caso de erro léxico/sintático, mensagens são exibidas no stderr (ex.: `[Line X] Error at '...': ...`).
- Mensagem final por tipo de erro:
  - `Parsing failed: lexical errors detected.`
  - `Parsing failed: syntax errors detected.`
  - `Parsing failed: lexical and syntax errors detected.`

## Estrutura

- `src/minijava/compiler/Main.java`: ponto de entrada e leitura de arquivo.
- `src/minijava/compiler/Scanner.java`: implementação do analisador léxico.
- `src/minijava/compiler/Parser.java`: implementação do analisador sintático LL.
- `src/minijava/compiler/Token.java` e `src/minijava/compiler/TokenType.java`: definição de tokens.

