# Relatório da Etapa 3 - Geração de Código Intermediário (IRTree) e Frames
### Alunos: João Henrique Lima, João Lucas Paiva, João Lucas Caetano

## 1. Conclusão da Etapa

**A etapa foi completamente ou parcialmente concluída?**

A etapa foi completamente concluída. O compilador agora é capaz de converter a Árvore Sintática Abstrata (AST) em uma Árvore de Representação Intermediária (IRTree) focada na arquitetura MIPS.

---

## 2. Pendências (se parcialmente concluída)

**No caso de parcialmente concluída, o que não foi concluído?**

A etapa foi completamente concluída. Todos os nós do MiniJava foram mapeados para instruções de código intermediário, incluindo alocação de objetos no Heap e aritmética de ponteiros para manipulação de Arrays.

---

## 3. Entradas Testadas

**O programa foi testado com quais entradas?**

- [x] Teste 1: Código interno (sem arquivo)
- [x] Teste 2: `Factorial.java` (código correto)
- [x] Teste 3: `FactorialWithError.java` (código com erros)

Foram feitos 3 testes. O primeiro e o segundo teste verificaram o fluxo de compilação completo (Scanner -> Parser -> Semântica -> Tradução), culminando na geração correta dos Fragmentos de Procedimento (`ProcFrag`) contendo os Frames e as IRTrees para os métodos `main` e `ComputeFac`. O terceiro teste avaliou a resiliência do compilador, garantindo que o módulo de tradução não é acionado caso as fases anteriores detectem erros estruturais.

---

## 4. Erros Encontrados

**Algum erro de execução foi encontrado em alguma das entradas? Quais?**

Apenas os erros no arquivo `FactorialWithError.java` foram encontrados, como esperado (erros propositadamente inseridos nas linhas 4 e 7). O compilador barrou o avanço do processo logo após a detecção das falhas sintáticas, impedindo a etapa de Tradução de tentar processar uma árvore incompleta ou gerar referências nulas de memória. Nos testes com código correto, o gerador de IRTree operou sem levantar exceções, produzindo o mapeamento exato de variáveis para a memória local (pilha).

---

## 5. Dificuldades Encontradas

**Quais as dificuldades encontradas na realização da etapa do projeto?**

As principais dificuldades encontradas nesta etapa envolveram a mudança de paradigma de alto nível (orientação a objetos) para baixo nível (arquitetura de computadores). 

1. **Tradução Condicional:** Implementar as classes `IfThenElseExp` e `RelCx` foi um desafio estrutural, pois exigiu gerenciar rótulos (`Label`) e saltos condicionais (`CJUMP`) sem instanciar valores booleanos desnecessários na memória.
2. **Gerenciamento de Memória:** O mapeamento do FramePointer (`$fp`) na classe `MipsFrame` exigiu precisão no cálculo matemático de offsets (-4 bytes para cada alocação) e na busca do endereço real de parâmetros e variáveis locais por meio do dicionário de escopos (`varEnv`).
3. **Arrays e Objetos:** Traduzir instâncias de Arrays exigiu a aplicação de aritmética de ponteiros diretamente via nós `BINOP` e `MEM`, além de chamadas ao sistema operacional (`_allocRecord`, `initArray`).

---

## 6. Participação da Equipe

**Qual a participação de cada membro da equipe na etapa?**

Para gerenciar a complexidade arquitetural do Back-End, dividimos as tarefas nas seguintes frentes:

* **João Henrique Lima:** Ficou responsável por pavimentar o gerenciamento de memória. Criou a abstração da arquitetura, implementando as classes `Frame`, `MipsFrame`, `Access` e `InFrame`, além de estruturar as listas de fragmentos (`Frag` e `ProcFrag`).
* **João Lucas Paiva:** Assumiu o núcleo de tradução das expressões e comandos básicos construindo o esqueleto principal do `TranslateVisitor`. Implementou o mapeamento matemático das operações (`BinaryOp`), alocação e leitura de identificadores locais (`Identifier` e `AssignmentStatement`), além de integrar o tradutor na classe `Main`.
* **João Lucas Caetano:** Focou na tradução de fluxo de controle avançado e estruturas do Heap. Implementou as lógicas das classes `IfThenElseExp` e `RelCx` para os desvios (`visitIfStatement` e `visitWhileStatement`), além de desenvolver toda a aritmética de ponteiros para lidar com Instanciação de Objetos e leitura/escrita em Arrays. 

Todos participaram dos testes de integração e validação final.

---

## 7. Demonstração de Execução

**Instruções para executar o programa:**

### Passo 1: Compilar o compilador

Na raiz do projeto, executamos o comando configurado com `-sourcepath` para abranger todos os pacotes das fases de compilação:

```powershell
javac -d bin -sourcepath src src/minijava/compiler/Main.java
```

### Passo 2: Executar os testes

#### Teste 1: Código interno de testes do Main (sem argumentos)

```powershell
java -cp bin minijava.compiler.Main
```

**Entrada:** Código embarcado no `Main.java` (Factorial)

**Saída:** ```text
=== TRANSLATION PHASE ===

Fragmento #0 (Procedimento): ComputeFac
Fragmento #1 (Procedimento): main

Total de fragmentos gerados: 2
=== END OF TRANSLATION ===

Compilation and Translation completed successfully!
```

---

#### Teste 2: Factorial.java (código correto)

```powershell
java -cp bin minijava.compiler.Main Factorial.java
```

**Entrada:** Arquivo `Factorial.java` com código correto

**Saída:** ```text
=== TRANSLATION PHASE ===

Fragmento #0 (Procedimento): ComputeFac
Fragmento #1 (Procedimento): main

Total de fragmentos gerados: 2
=== END OF TRANSLATION ===

Compilation and Translation completed successfully!
```

---

#### Teste 3: FactorialWithError.java (código com erros propositais)

```powershell
java -cp bin minijava.compiler.Main FactorialWithError.java
```

**Entrada:** Arquivo `FactorialWithError.java` com erros propositais de sintaxe

**Saída:**

```text
[Line 4] Error at '}': Expect ';'.
[Line 7] Error at 'class': Expect '}' after main method.
Compilation failed due to errors detected in previous phases.
```