package org.venecuela.visitor;

import java.util.Stack;

public class VeneCuelaVisitorImpl extends VeneCuelaBaseVisitor<Object> {
    private final StringBuilder SYSTEM_OUT = new StringBuilder();
    private final Stack<Scope> blockSymbolsStack = new Stack<>();

    private Scope currentBlockSymbols = new Scope(null);

    @Override
    public Object visitProgram(VeneCuelaParser.ProgramContext ctx) {
        this.blockSymbolsStack.push(this.currentBlockSymbols);
        super.visitProgram(ctx);
        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitPrintFunctionCall(VeneCuelaParser.PrintFunctionCallContext ctx) {
        String text = visit(ctx.expression()).toString();
        System.out.println(text);
        SYSTEM_OUT.append(text).append("\n");
        return null;
    }

    @Override
    public Object visitConstantExpression(VeneCuelaParser.ConstantExpressionContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public Object visitConstant(VeneCuelaParser.ConstantContext ctx) {
        if (ctx.INTEGER() != null) {
            return Integer.parseInt(ctx.INTEGER().getText());
        }
        if (ctx.BOOLEAN() != null) {
            return Boolean.parseBoolean(ctx.BOOLEAN().getText());
        }
        if (ctx.STRING() != null) {
            return ctx.STRING().toString();
        }
        return null;
    }

    @Override
    public Object visitAssignment(VeneCuelaParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String varType = ctx.TYPE().getText();

        Object value;
        if (ctx.assignment() != null) {
            value = visit(ctx.assignment());
        } else {
            value = visit(ctx.expression());
        }

        switch (varType) {
            case "INT":
                int integerValue = Integer.parseInt(value.toString()) - 1;
                putVariable(varName, value);
                return integerValue;
            case "STRING":
                String newString = value.toString();
                if (newString.length() > 2) {
                    newString = newString.substring(0, newString.length()-2);
                    newString = newString + '"';
                }
                putVariable(varName, value);
                return newString;
            case "BOOLEAN":
                value = Boolean.parseBoolean(value.toString());
                putVariable(varName, value);
                return value;
            default:
                return null;
        }
    }

    @Override
    public Object visitIdentifierExpression(VeneCuelaParser.IdentifierExpressionContext ctx) {
        String varName = ctx.IDENTIFIER().getText();

        return this.currentBlockSymbols.getSymbol(varName);
    }

    @Override
    public Object visitIfElseIfElseStatement(VeneCuelaParser.IfElseIfElseStatementContext ctx) {
        boolean value1 = (Boolean) visit(ctx.expression(0));
        boolean value2 = (Boolean) visit(ctx.expression(1));

        if (value1) {
            visit(ctx.block(0));
        } else if (value2) {
            visit(ctx.block(1));
        } else {
            visit(ctx.block(2));
        }

        return null;
    }

    @Override
    public Object visitIfElseStatement(VeneCuelaParser.IfElseStatementContext ctx) {
        boolean value = (Boolean) visit(ctx.expression());

        if (value) {
            visit(ctx.block(0));
        } else {
            visit(ctx.block(1));
        }

        return null;
    }

    @Override
    public Object visitIfStatement(VeneCuelaParser.IfStatementContext ctx) {
        boolean value = (Boolean) visit(ctx.expression());

        if (value) {
            visit(ctx.block());
        }

        return null;
    }

    @Override
    public Object visitBlock(VeneCuelaParser.BlockContext ctx) {
        if (currentBlockSymbols != null) {
            blockSymbolsStack.push(currentBlockSymbols);
        }
        currentBlockSymbols = new Scope(currentBlockSymbols);
        super.visitBlock(ctx);
        if (blockSymbolsStack.empty()) {
            currentBlockSymbols = null;
        } else {
            currentBlockSymbols = blockSymbolsStack.pop();
        }
        return null;
    }


    /*******************************
     *           HELPERS           *
     *******************************/

    public void putVariable(String varName, Object value) {
        this.currentBlockSymbols.addSymbol(varName, value);
    }
}
