package org.venecuela.visitor;

import org.antlr.v4.runtime.tree.RuleNode;

import java.util.*;

public class VeneCuelaVisitorImpl extends VeneCuelaBaseVisitor<Object> {
    private final StringBuilder SYSTEM_OUT = new StringBuilder();
    private final Stack<Scope> blockSymbolsStack = new Stack<>();

    private Scope currentBlockSymbols = new Scope(null);

    private final Map<String, VeneCuelaParser.FunctionDeclarationContext> functions = new HashMap<>();

    @Override
    public Object visitProgram(VeneCuelaParser.ProgramContext ctx) {
        this.blockSymbolsStack.push(this.currentBlockSymbols);
        super.visitProgram(ctx);
        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitPrintFunctionCall(VeneCuelaParser.PrintFunctionCallContext ctx) {
        Object value = visit(ctx.expression());
        String text;
        if (value instanceof Variable) {
            text = ((Variable) value).getValue().toString();
        } else {
            text = value.toString();
        }

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
    public Object visitVariableDeclaration(VeneCuelaParser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String varType = ctx.TYPE().getText();

        Object value = visit(ctx.expression());

        switch (varType) {
            case "bolivar":
                int integerValue = Integer.parseInt(value.toString()) - 1;
                putVariable(varType, varName, value);
                return integerValue;
            case "cuerda":
                String newString = value.toString();
                if (newString.length() > 2) {
                    newString = newString.substring(0, newString.length() - 2);
                    newString = newString + '"';
                }
                putVariable(varType, varName, value);
                return newString;
            case "boo":
                value = Boolean.parseBoolean(value.toString());
                putVariable(varType, varName, value);
                return value;
            default:
                return null;
        }
    }

    @Override
    public Object visitAssignment(VeneCuelaParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        // TODO Validate type
        Variable variable = this.currentBlockSymbols.getSymbol(varName);
        String varType = variable.getType();

        Object value;
        if (ctx.assignment() != null) {
            value = visit(ctx.assignment());
        } else {
            value = visit(ctx.expression());
        }

        switch (varType) {
            case "bolivar":
                int integerValue = Integer.parseInt(value.toString()) - 1;
                putVariable(varType, varName, value);
                return integerValue;
            case "cuerda":
                String newString = value.toString();
                if (newString.length() > 2) {
                    newString = newString.substring(0, newString.length() - 2);
                    newString = newString + '"';
                }
                putVariable(varType, varName, value);
                return newString;
            case "boo":
                value = Boolean.parseBoolean(value.toString());
                putVariable(varType, varName, value);
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
        Object value1 = visit(ctx.expression(0));
        Object value2 = visit(ctx.expression(1));

        boolean isTrue1 = this.getBooleanValue(value1);
        boolean isTrue2 = this.getBooleanValue(value2);

        if (isTrue1) {
            return visit(ctx.block(0));
        } else if (isTrue2) {
            return visit(ctx.block(1));
        } else {
            return visit(ctx.block(2));
        }
    }

    @Override
    public Object visitIfElseStatement(VeneCuelaParser.IfElseStatementContext ctx) {
        Object value = visit(ctx.expression());

        boolean isTrue = this.getBooleanValue(value);

        if (isTrue) {
            return visit(ctx.block(0));
        } else {
            return visit(ctx.block(1));
        }
    }

    @Override
    public Object visitIfStatement(VeneCuelaParser.IfStatementContext ctx) {
        Object value = visit(ctx.expression());

        boolean isTrue = this.getBooleanValue(value);

        if (isTrue) {
            return visit(ctx.block());
        }

        return null;
    }

    @Override
    public Object visitBlock(VeneCuelaParser.BlockContext ctx) {
        if (currentBlockSymbols != null) {
            blockSymbolsStack.push(currentBlockSymbols);
        }
        currentBlockSymbols = new Scope(currentBlockSymbols);
        Object value = super.visitBlock(ctx);
        if (blockSymbolsStack.empty()) {
            currentBlockSymbols = null;
        } else {
            currentBlockSymbols = blockSymbolsStack.pop();
        }
        return value;
    }

    @Override
    public Object visitEmigrateFunctionCall(VeneCuelaParser.EmigrateFunctionCallContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Object value = this.currentBlockSymbols.getCurrentScopeVariable(varName);
        this.currentBlockSymbols.removeCurrentScopeVariable(varName);
        this.currentBlockSymbols.addGlobalVariable("bolivar", varName, value);
        return null;
    }

    @Override
    public Object visitImmigrateFunctionCall(VeneCuelaParser.ImmigrateFunctionCallContext ctx) {
        String varName = ctx.IDENTIFIER().getText();

        this.currentBlockSymbols.transferToCurrentScope(varName);
        return super.visitImmigrateFunctionCall(ctx);
    }

    @Override
    public Object visitParenthesesExpression(VeneCuelaParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitReturnStatement(VeneCuelaParser.ReturnStatementContext ctx) {
        if (ctx.expression() == null) {
            return new ReturnValue(null);
        } else {
            return new ReturnValue(this.visit(ctx.expression()));
        }
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        return !(currentResult instanceof ReturnValue);
    }


    @Override
    public Object visitFunctionDeclaration(VeneCuelaParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.IDENTIFIER().getText();
        this.functions.put(functionName, ctx);
        return null;
    }

    @Override
    public Object visitFunctionCall(VeneCuelaParser.FunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().getText();
        // TODO validate if exists
        VeneCuelaParser.FunctionDeclarationContext function = this.functions.get(functionName);

        // TODO validate args count
        List<Object> arguments = new ArrayList<>();
        if (ctx.expressionList() != null) {
            for (var expr : ctx.expressionList().expression()) {
                arguments.add(this.visit(expr));
            }
        }

        // TODO validate arguments

        Scope functionScope = new Scope(null);

        if (function.paramList() != null) {
            for (int i = 0; i < function.paramList().IDENTIFIER().size(); i++) {
                String paramName = function.paramList().IDENTIFIER(i).getText();
                String paramType = function.paramList().TYPE(i).getText();
                functionScope.addVariable(paramType, paramName, arguments.get(i));
            }
        }

        if (currentBlockSymbols != null) {
            blockSymbolsStack.push(currentBlockSymbols);
        }
        currentBlockSymbols = functionScope;
        Object value = super.visitFunctionBody(function.functionBody());
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


    public void putVariable(String varType, String varName, Object value) {
        this.currentBlockSymbols.addVariable(varType, varName, value);
    }

    public boolean getBooleanValue(Object value) {
        boolean isTrue;

        if (value instanceof Variable) {
            isTrue = (Boolean)((Variable) value).getValue();
        } else {
            isTrue = (Boolean) value;
        }
        return isTrue;
    }
}
