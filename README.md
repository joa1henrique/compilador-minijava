# Relatório da Etapa 1
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

- [ ] Teste 1: Código interno (sem arquivo)
- [ ] Teste 2: `Factorial.java` (código correto)
- [ ] Teste 3: `FactorialWithError.java` (código com erros)

Foram feitos 3 testes. O primeiro teste utilizou um código interno embarcado no `Main.java`, o segundo teste utilizou o arquivo `Factorial.java` contendo um código correto, e o terceiro teste utilizou o arquivo `FactorialWithError.java` contendo um código propositalmente com erros, para testar se o compilador identifica os erros corretamente.

---

## 4. Erros Encontrados

**Algum erro de execução foi encontrado em alguma das entradas? Quais?**

Apenas os erros no arquivo `FactorialWithError.java` foram encontrados, como esperado (erros propositalmente inseridos). O compilador identificou os erros de sintaxe e semântica presentes no código, e gerou mensagens de erro apropriadas. Os testes com o código interno do Main e o `Factorial.java` foram executados sem erros, produzindo a saída esperada.

---

## 5. Dificuldades Encontradas

**Quais as dificuldades encontradas na realização da etapa do projeto?**

Na primeira implementação surgiram algumas questões, como o a detecção de erros de sintaxe e semântica, e a geração de mensagens de erro claras para o usuário. Também houve desafios na organização do código e na estruturação do projeto para garantir que o compilador fosse modular e fácil de manter.

---

## 6. Participação da Equipe

**Qual a participação de cada membro da equipe na etapa?**

Os três membros participaram da implementação inicial. A partir daí, dividimos as tarefas para um fazer melhorias no scanner, outro para fazer melhorias no parser, e outro para cuidar do tratamento de erros. Após concluída a implementação final, todos participaram dos testes.

---

## 7. Demonstração de Execução

**Instruções para executar o programa:**

### Passo 1: Compilar o compilador

Na raiz do projeto, executamos:

```
javac src/minijava/compiler/*.java
```

### Passo 2: Executar os testes

#### Teste 1: Código interno de testes do Main (sem argumentos)

```
java -cp src minijava.compiler.Main
```

**Entrada:** Código embarcado no `Main.java`

**Saída:**
```
Running built-in test code:
Parsing completed.
```

---

#### Teste 2: Factorial.java (código correto)

```powershell
java -cp src minijava.compiler.Main .\Factorial.java
```

**Entrada:** Arquivo `Factorial.java` com código correto

**Saída:**
```
Parsing completed.
```

---

#### Teste 3: FactorialWithError.java (código com erros propositais)

```powershell
java -cp src minijava.compiler.Main .\FactorialWithError.java
```

**Entrada:** Arquivo `FactorialWithError.java` com erros propositais

**Saída:**
```
[Line 4] Error at '}': Expect ';'.
[Line 7] Error at 'class': Expect '}' after main method.
Parsing failed: syntax errors detected.
```


