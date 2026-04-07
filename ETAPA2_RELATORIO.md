# Relatório da Etapa 2 - AST e Análise Semântica

## Resumo da Etapa

Nesta etapa, implementamos a construção da Árvore Sintática Abstrata (AST) e a análise semântica do compilador MiniJava. O compilador agora segue um fluxo completo: Scanner → Parser (com construção de AST) → Análise Semântica.

---

## 1. Principais Componentes Implementados

### 1.1 Estrutura de AST (Pacote `minijava.compiler.ast`)

Foram criadas **29 classes Java** representando os nós da AST:

#### Classes Base
- **ASTNode**: Classe abstrata base para todos os nós
- **Expression**: Classe base para expressões
- **Statement**: Classe base para comandos
- **Type**: Representa tipos (int, boolean, classes, arrays)

#### Nós de Programa
- **Program**: Raiz da AST (programa completo)
- **MainClass**: Classe principal (main)
- **ClassDecl**: Declaração de classe
- **MethodDecl**: Declaração de método
- **VarDecl**: Declaração de variável

#### Statements (Comandos)
- **BlockStatement**: Bloco de comandos `{ ... }`
- **IfStatement**: Comando `if`
- **WhileStatement**: Comando `while`
- **AssignmentStatement**: Atribuição simples `x = expr`
- **ArrayAssignmentStatement**: Atribuição em array `x[i] = expr`
- **PrintStatement**: `System.out.println(expr)`
- **ReturnStatement**: `return expr`

#### Expressions (Expressões)
- **IntegerLiteral**: Literal inteira (ex: 42)
- **BooleanLiteral**: Literal booleana (true/false)
- **Identifier**: Identificador (variável ou classe)
- **This**: Palavra-chave `this`
- **NewObject**: Criação de objeto `new ClassName()`
- **NewArray**: Criação de array `new int[size]`
- **BinaryOp**: Operação binária (&&, <, +, -, *)
- **UnaryOp**: Operação unária (!)
- **MethodCall**: Chamada de método `obj.method(args)`
- **ArrayAccess**: Acesso a array `arr[index]`
- **ArrayLength**: Acesso a `.length`
- **ParenthesizedExpression**: Expressão com parênteses

### 1.2 Interface Visitor (Design Pattern)

Implementada a interface **Visitor** com métodos para visitar cada tipo de nó da AST:
- Permite percorrer a AST de forma estruturada
- Facilita implementação de múltiplas análises (semântica, geração de código, etc.)

### 1.3 Analisador Semântico (SemanticAnalyzer)

Classe implementando análise semântica em duas fases:

#### Fase 1: Coleta de Declarações
- Coleta todas as declarações de classe antes da análise
- Verifica duplicação de classes
- Verifica se superclasses existem

#### Fase 2: Análise Semântica
- Análise de tipos
- Verificação de escopo de variáveis
- Validação de tipos em:
  - Operações binárias (&&, <, +, -, *)
  - Operações unárias (!)
  - Atribuições
  - Chamadas de método
  - Acessos a arrays
- Verificação de tipos de retorno
- Gerenciamento de escopo (pilha de escopos)

---

## 2. Modificações ao Parser

O Parser foi completamente refatorado para **construir a AST** em vez de apenas validar a sintaxe:

- Métodos agora retornam nós da AST
- Método `parseProgram()` retorna `Program` (raiz da AST)
- Método antigo `parse()` foi removido
- Expressões agora retornam objetos `Expression`
- Comandos agora retornam objetos `Statement`

---

## 3. Integração ao Fluxo Principal

O **Main.java** foi modificado para implementar as 3 fases do compilador:

```
1. Análise Léxica (Scanner) → Lista de Tokens
2. Análise Sintática (Parser) → AST
3. Análise Semântica (SemanticAnalyzer) → Erros Semânticos
```

---

## 4. Funcionalidades de Análise Semântica

### 4.1 Verificação de Tipos
- Verifica tipos em operações binárias
- Valida tipos em atribuições
- Verifica compatibilidade de tipos em retornos

### 4.2 Análise de Escopo
- Implementa pilha de escopos
- Controla visibilidade de variáveis
- Suporta escopos locais, parâmetros e campos de classe

### 4.3 Validação de Chamadas de Método
- Verifica se método existe na classe
- Valida número e tipo de argumentos
- Verifica tipo de retorno

### 4.4 Validação de Arrays
- Verifica acesso a índices (deve ser int)
- Valida atribuição em arrays
- Controla acesso a `.length`

---

## 5. Testes Realizados

### Teste 1: Código Padrão (sem argumentos)
```powershell
java -cp src minijava.compiler.Main
```
**Resultado**: ✅ `Compilation completed successfully!`

### Teste 2: Arquivo Válido (Factorial.java)
```powershell
java -cp src minijava.compiler.Main .\Factorial.java
```
**Resultado**: ✅ `Compilation completed successfully!`

### Teste 3: Arquivo com Erros (FactorialWithError.java)
```powershell
java -cp src minijava.compiler.Main .\FactorialWithError.java
```
**Resultado**: ✅ Erros detectados corretamente

---

## 6. Estrutura de Diretórios

```
src/minijava/compiler/
├── Main.java
├── Scanner.java
├── Parser.java (refatorado)
├── Token.java
├── TokenType.java
├── ast/
│   ├── ASTNode.java
│   ├── Program.java
│   ├── MainClass.java
│   ├── ClassDecl.java
│   ├── MethodDecl.java
│   ├── VarDecl.java
│   ├── Type.java
│   ├── Statement.java
│   ├── Expression.java
│   ├── [24 classes de expressões e comandos]
│   └── Visitor.java
└── semantic/
    └── SemanticAnalyzer.java
```

---

## 7. Próximos Passos (Etapa 3)

Possíveis melhorias para a próxima etapa:
- Análise de dados de fluxo (data flow analysis)
- Geração de código intermediário
- Otimizações de código
- Geração de bytecode ou código nativo

---

## 8. Conclusão

A Etapa 2 foi completada com sucesso! O compilador agora possui:
✅ Árvore Sintática Abstrata completa
✅ Análise semântica robusta
✅ Verificação de tipos
✅ Análise de escopos
✅ Gerenciamento de erros em múltiplas fases

O código está organizado, bem documentado e pronto para extensões futuras.

