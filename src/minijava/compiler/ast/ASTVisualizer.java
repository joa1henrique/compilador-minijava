package minijava.compiler.ast;

public class ASTVisualizer {
    private StringBuilder output;
    private String indent;

    public ASTVisualizer() {
        this.output = new StringBuilder();
        this.indent = "";
    }

    public String visualize(Program program) {
        output = new StringBuilder();
        indent = "";
        visitProgram(program);
        return output.toString();
    }

    private void visitProgram(Program node) {
        println("Program");
        increaseIndent();
        println("MainClass: " + node.mainClass.className.lexeme);
        increaseIndent();
        visitStatement(node.mainClass.mainStatement);
        decreaseIndent();
        if (!node.classes.isEmpty()) {
            println("Classes:");
            increaseIndent();
            for (ClassDecl classDecl : node.classes) {
                visitClassDecl(classDecl);
            }
            decreaseIndent();
        }
        decreaseIndent();
    }

    private void visitClassDecl(ClassDecl node) {
        println("ClassDecl: " + node.className.lexeme);
        increaseIndent();
        if (node.superClassName != null) {
            println("SuperClass: " + node.superClassName.lexeme);
        }
        if (!node.fields.isEmpty()) {
            println("Fields:");
            increaseIndent();
            for (VarDecl varDecl : node.fields) {
                visitVarDecl(varDecl);
            }
            decreaseIndent();
        }
        if (!node.methods.isEmpty()) {
            println("Methods:");
            increaseIndent();
            for (MethodDecl methodDecl : node.methods) {
                visitMethodDecl(methodDecl);
            }
            decreaseIndent();
        }
        decreaseIndent();
    }

    private void visitVarDecl(VarDecl node) {
        println("VarDecl: " + node.varName.lexeme + " : " + node.type);
    }

    private void visitMethodDecl(MethodDecl node) {
        println("MethodDecl: " + node.methodName.lexeme);
        increaseIndent();
        println("ReturnType: " + node.returnType);
        if (!node.parameters.isEmpty()) {
            println("Parameters:");
            increaseIndent();
            for (VarDecl param : node.parameters) {
                visitVarDecl(param);
            }
            decreaseIndent();
        }
        if (!node.locals.isEmpty()) {
            println("LocalVariables:");
            increaseIndent();
            for (VarDecl varDecl : node.locals) {
                visitVarDecl(varDecl);
            }
            decreaseIndent();
        }
        if (!node.statements.isEmpty()) {
            println("Body:");
            increaseIndent();
            for (Statement stmt : node.statements) {
                visitStatement(stmt);
            }
            decreaseIndent();
        }
        println("Return:");
        increaseIndent();
        visitExpression(node.returnExpression);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitStatement(Statement node) {
        if (node instanceof PrintStatement) {
            visitPrintStatement((PrintStatement) node);
        } else if (node instanceof AssignmentStatement) {
            visitAssignmentStatement((AssignmentStatement) node);
        } else if (node instanceof ArrayAssignmentStatement) {
            visitArrayAssignmentStatement((ArrayAssignmentStatement) node);
        } else if (node instanceof IfStatement) {
            visitIfStatement((IfStatement) node);
        } else if (node instanceof WhileStatement) {
            visitWhileStatement((WhileStatement) node);
        } else if (node instanceof BlockStatement) {
            visitBlockStatement((BlockStatement) node);
        } else if (node instanceof ReturnStatement) {
            visitReturnStatement((ReturnStatement) node);
        } else {
            println("Unknown Statement: " + node.getClass().getSimpleName());
        }
    }

    private void visitPrintStatement(PrintStatement node) {
        println("PrintStatement");
        increaseIndent();
        visitExpression(node.expression);
        decreaseIndent();
    }

    private void visitAssignmentStatement(AssignmentStatement node) {
        println("AssignmentStatement: " + node.varName.lexeme);
        increaseIndent();
        visitExpression(node.value);
        decreaseIndent();
    }

    private void visitArrayAssignmentStatement(ArrayAssignmentStatement node) {
        println("ArrayAssignmentStatement: " + node.varName.lexeme);
        increaseIndent();
        println("Index:");
        increaseIndent();
        visitExpression(node.index);
        decreaseIndent();
        println("Value:");
        increaseIndent();
        visitExpression(node.value);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitIfStatement(IfStatement node) {
        println("IfStatement");
        increaseIndent();
        println("Condition:");
        increaseIndent();
        visitExpression(node.condition);
        decreaseIndent();
        println("ThenBranch:");
        increaseIndent();
        visitStatement(node.thenStatement);
        decreaseIndent();
        if (node.elseStatement != null) {
            println("ElseBranch:");
            increaseIndent();
            visitStatement(node.elseStatement);
            decreaseIndent();
        }
        decreaseIndent();
    }

    private void visitWhileStatement(WhileStatement node) {
        println("WhileStatement");
        increaseIndent();
        println("Condition:");
        increaseIndent();
        visitExpression(node.condition);
        decreaseIndent();
        println("Body:");
        increaseIndent();
        visitStatement(node.body);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitBlockStatement(BlockStatement node) {
        println("BlockStatement");
        increaseIndent();
        for (Statement stmt : node.statements) {
            visitStatement(stmt);
        }
        decreaseIndent();
    }

    private void visitReturnStatement(ReturnStatement node) {
        println("ReturnStatement");
        increaseIndent();
        visitExpression(node.expression);
        decreaseIndent();
    }

    private void visitExpression(Expression node) {
        if (node instanceof IntegerLiteral) {
            visitIntegerLiteral((IntegerLiteral) node);
        } else if (node instanceof BooleanLiteral) {
            visitBooleanLiteral((BooleanLiteral) node);
        } else if (node instanceof Identifier) {
            visitIdentifier((Identifier) node);
        } else if (node instanceof ArrayLength) {
            visitArrayLength((ArrayLength) node);
        } else if (node instanceof ArrayAccess) {
            visitArrayAccess((ArrayAccess) node);
        } else if (node instanceof BinaryOp) {
            visitBinaryOp((BinaryOp) node);
        } else if (node instanceof UnaryOp) {
            visitUnaryOp((UnaryOp) node);
        } else if (node instanceof MethodCall) {
            visitMethodCall((MethodCall) node);
        } else if (node instanceof NewArray) {
            visitNewArray((NewArray) node);
        } else if (node instanceof NewObject) {
            visitNewObject((NewObject) node);
        } else if (node instanceof This) {
            visitThis((This) node);
        } else if (node instanceof ParenthesizedExpression) {
            visitParenthesizedExpression((ParenthesizedExpression) node);
        } else {
            println("Unknown Expression: " + node.getClass().getSimpleName());
        }
    }

    private void visitIntegerLiteral(IntegerLiteral node) {
        println("IntegerLiteral: " + node.value);
    }

    private void visitBooleanLiteral(BooleanLiteral node) {
        println("BooleanLiteral: " + node.value);
    }

    private void visitIdentifier(Identifier node) {
        println("Identifier: " + node.name.lexeme);
    }

    private void visitArrayLength(ArrayLength node) {
        println("ArrayLength");
        increaseIndent();
        visitExpression(node.array);
        decreaseIndent();
    }

    private void visitArrayAccess(ArrayAccess node) {
        println("ArrayAccess");
        increaseIndent();
        println("Array:");
        increaseIndent();
        visitExpression(node.array);
        decreaseIndent();
        println("Index:");
        increaseIndent();
        visitExpression(node.index);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitBinaryOp(BinaryOp node) {
        println("BinaryOp: " + node.operator);
        increaseIndent();
        println("Left:");
        increaseIndent();
        visitExpression(node.left);
        decreaseIndent();
        println("Right:");
        increaseIndent();
        visitExpression(node.right);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitUnaryOp(UnaryOp node) {
        println("UnaryOp: " + node.operator);
        increaseIndent();
        visitExpression(node.operand);
        decreaseIndent();
    }

    private void visitMethodCall(MethodCall node) {
        println("MethodCall: " + node.methodName.lexeme);
        increaseIndent();
        println("Object:");
        increaseIndent();
        visitExpression(node.object);
        decreaseIndent();
        if (!node.arguments.isEmpty()) {
            println("Arguments:");
            increaseIndent();
            for (Expression arg : node.arguments) {
                visitExpression(arg);
            }
            decreaseIndent();
        }
        decreaseIndent();
    }

    private void visitNewArray(NewArray node) {
        println("NewArray");
        increaseIndent();
        println("Size:");
        increaseIndent();
        visitExpression(node.sizeExpression);
        decreaseIndent();
        decreaseIndent();
    }

    private void visitNewObject(NewObject node) {
        println("NewObject: " + node.className.lexeme);
    }

    private void visitThis(This node) {
        println("This");
    }

    private void visitParenthesizedExpression(ParenthesizedExpression node) {
        println("ParenthesizedExpression");
        increaseIndent();
        visitExpression(node.expression);
        decreaseIndent();
    }

    private void println(String text) {
        output.append(indent).append(text).append("\n");
    }

    private void increaseIndent() {
        indent += "  ";
    }

    private void decreaseIndent() {
        if (indent.length() >= 2) {
            indent = indent.substring(0, indent.length() - 2);
        }
    }
}

