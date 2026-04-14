# Relatório da Etapa 2 - AST e Análise Semântica
### Alunos: João Henrique Lima, João Lucas Paiva, João Lucas Caetano

## 1. Conclusão da Etapa

**A etapa foi completamente ou parcialmente concluída?**

A etapa foi completamente concluída.

---

## 2. Pendências (se parcialmente concluída)

**No caso de parcialmente concluída, o que não foi concluído?**

A etapa foi completamente concluída.

---

## 3. Entradas Testadas

**O programa foi testado com quais entradas?**

- [x] Teste 1: Código interno (sem arquivo)
- [x] Teste 2: `Factorial.java` (código correto)
- [x] Teste 3: `FactorialWithError.java` (código com erros)

Foram feitos 3 testes. O primeiro teste utilizou um código interno embarcado no `Main.java`, o segundo teste utilizou o arquivo `Factorial.java` contendo um código correto, e o terceiro teste utilizou o arquivo `FactorialWithError.java` contendo um código propositalmente com erros, para testar se o compilador identifica os erros de sintaxe corretamente. Todos os testes verificaram a construção da Árvore Sintática Abstrata (AST) e a execução da análise semântica.

---

## 4. Erros Encontrados

**Algum erro de execução foi encontrado em alguma das entradas? Quais?**

Apenas os erros no arquivo `FactorialWithError.java` foram encontrados, como esperado (erros propositalmente inseridos). O compilador identificou corretamente os erros de sintaxe:
- **Linha 4**: Falta de `;` após `System.out.println(new Fac().ComputeFac(10))`
- **Linha 7**: Falta de `;` após `int num_aux`

O compilador gerou mensagens de erro apropriadas e impediu a continuação da análise semântica. Os testes com o código interno do Main e o `Factorial.java` foram executados sem erros, produzindo a AST completa e análise semântica bem-sucedida.

---

## 5. Dificuldades Encontradas

**Quais as dificuldades encontradas na realização da etapa do projeto?**

As principais dificuldades foram: (1) Estruturação da hierarquia de classes para representar corretamente todos os nós da AST, garantindo coesão entre nós base e especializados; (2) Implementação do padrão Visitor de forma que pudesse iterar sobre a árvore sem acoplamento excessivo; (3) Criação do ASTVisualizer para permitir visualização clara da estrutura da árvore gerada, facilitando debug e validação. A integração entre as três fases (léxica, sintática e semântica) também apresentou desafios iniciais na passagem correta de contexto e estado entre as fases.

---

## 6. Participação da Equipe

**Qual a participação de cada membro da equipe na etapa?**

Os três membros participaram da implementação. Um focou na refatoração do Parser para construir a AST, outro implementou a estrutura de classes AST e o padrão Visitor, e o terceiro implementou o SemanticAnalyzer e o ASTVisualizer. Durante a integração e testes, todos participaram da validação da solução e ajustes finais.

---

## 7. Demonstração de Execução

**Instruções para executar o programa:**

### Passo 1: Compilar o compilador

Na raiz do projeto, executamos:

```
javac -d src src/minijava/compiler/ast/*.java src/minijava/compiler/semantic/*.java src/minijava/compiler/*.java
```

### Passo 2: Executar os testes

#### Teste 1: Código interno de testes do Main (sem argumentos)

```
java -cp src minijava.compiler.Main
```

**Entrada:** Código embarcado no `Main.java` (Factorial com método recursivo)

**Saída:**
```
Running built-in test code:

=== ABSTRACT SYNTAX TREE ===

Program
  MainClass: Factorial
    PrintStatement
      MethodCall: ComputeFac
        Object:
          NewObject: Fac
        Arguments:
          IntegerLiteral: 10
  Classes:
    ClassDecl: Fac
      Methods:
        MethodDecl: ComputeFac
          ReturnType: int
          Parameters:
            VarDecl: num : int
          LocalVariables:
            VarDecl: num_aux : int
          Body:
            IfStatement
              Condition:
                BinaryOp: LESS_THAN
                  Left:
                    Identifier: num
                  Right:
                    IntegerLiteral: 1
              ThenBranch:
                AssignmentStatement: num_aux
                  IntegerLiteral: 1
              ElseBranch:
                AssignmentStatement: num_aux
                  BinaryOp: TIMES
                    Left:
                      Identifier: num
                    Right:
                      ParenthesizedExpression
                        MethodCall: ComputeFac
                          Object:
                            This
                          Arguments:
                            BinaryOp: MINUS
                              Left:
                                Identifier: num
                              Right:
                                IntegerLiteral: 1
          Return:
            Identifier: num_aux

=== END OF AST ===

Compilation completed successfully!
```

---

#### Teste 2: Factorial.java (código correto)

```powershell
java -cp src minijava.compiler.Main .\Factorial.java
```

**Entrada:** Arquivo `Factorial.java` com código correto

**Saída:** AST completa mostrando a estrutura da árvore, seguida de:
```
Compilation completed successfully!
```

---

#### Teste 3: FactorialWithError.java (código com erros propositais)

```powershell
java -cp src minijava.compiler.Main .\FactorialWithError.java
```

**Entrada:** Arquivo `FactorialWithError.java` com erros propositais de sintaxe

**Saída:**
```
[Line 4] Error at '}': Expect ';'.
[Line 7] Error at 'class': Expect '}' after main method.
Compilation failed: syntax errors detected.
```