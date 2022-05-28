package org.venecuela.visitor;

import org.antlr.v4.runtime.tree.RuleNode;
import org.venecuela.visitor.exceptions.VariableAlreadyDeclaredException;
import org.venecuela.visitor.exceptions.WrongTypeException;

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
        Object value = visit(ctx.constant());
        if (value instanceof String) {
            value = ((String) value).substring(1, ((String) value).length() - 1);
        }
        return value;
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

        Object value;

        if (currentBlockSymbols.containsVariable(varName)) {
            throw new VariableAlreadyDeclaredException();
        }

        if (ctx.variableDeclaration() != null) {
            value = visit(ctx.variableDeclaration());
        } else {
            value = visit(ctx.expression());
        }

        switch (varType) {
            case "bolivar":
                int integerValue = this.getIntegerValue(value) - 1;
                this.currentBlockSymbols.putVariable(varType, varName, value);
                return integerValue;
            case "cuerda":
                String newString = this.getStringValue(value);
                if (newString.length() > 2) {
                    newString = newString.substring(0, newString.length() - 1);
                }
                this.currentBlockSymbols.putVariable(varType, varName, value);
                return newString;
            case "boo":
                value = this.getBooleanValue(value);
                this.currentBlockSymbols.putVariable(varType, varName, value);
                return value;
            default:
                return null;
        }
    }

    @Override
    public Object visitAssignment(VeneCuelaParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Variable variable = this.currentBlockSymbols.getVariable(varName);
        String varType = variable.getType();

        Object value;
        if (ctx.assignment() != null) {
            value = visit(ctx.assignment());
        } else {
            value = visit(ctx.expression());
        }

        switch (varType) {
            case "bolivar":
                int integerValue = this.getIntegerValue(value) - 1;
                this.currentBlockSymbols.addVariable(varName, value);
                return integerValue;
            case "cuerda":
                String newString = this.getStringValue(value);
                if (newString.length() > 2) {
                    newString = newString.substring(0, newString.length() - 1);
                }
                this.currentBlockSymbols.addVariable(varName, value);
                return newString;
            case "boo":
                value = this.getBooleanValue(value);
                this.currentBlockSymbols.addVariable(varName, value);
                return value;
            default:
                return null;
        }
    }

    @Override
    public Object visitIdentifierExpression(VeneCuelaParser.IdentifierExpressionContext ctx) {
        String varName = ctx.IDENTIFIER().getText();

        return this.currentBlockSymbols.getVariable(varName);
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
        Variable variable = this.currentBlockSymbols.getCurrentScopeVariable(varName);
        this.currentBlockSymbols.removeCurrentScopeVariable(varName);
        this.currentBlockSymbols.putGlobalVariable(variable.getType(), varName, variable.getValue());
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
                functionScope.putVariable(paramType, paramName, arguments.get(i));
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

    @Override
    public Object visitNumericAddOpExpression(VeneCuelaParser.NumericAddOpExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        String operator = ctx.numericAddOp().getText();

        return switch (operator) {
            case "+" -> expr1 + expr2;
            case "-" -> expr1 - expr2;
            default -> null;
        };
    }


    @Override
    public Object visitBooleanUnaryOpExpression(VeneCuelaParser.BooleanUnaryOpExpressionContext ctx) {
        return !(this.getBooleanValue(visit(ctx.expression())));
    }

    @Override
    public Object visitStringBinaryOpExpression(VeneCuelaParser.StringBinaryOpExpressionContext ctx) {
        String expr1 = this.getStringValue(visit(ctx.expression(0)));
        String expr2 = this.getStringValue(visit(ctx.expression(1)));

        return expr1 + expr2;
    }

    @Override
    public Object visitBooleanBinaryOpExpression(VeneCuelaParser.BooleanBinaryOpExpressionContext ctx) {
        boolean expr1 = this.getBooleanValue(visit(ctx.expression(0)));
        boolean expr2 = this.getBooleanValue(visit(ctx.expression(1)));

        String operator = ctx.booleanBinaryOp().getText();

        return switch (operator) {
            case "||" -> expr1 || expr2;
            case "&&" -> expr1 && expr2;
            default -> null;
        };
    }

    @Override
    public Object visitEqualExpression(VeneCuelaParser.EqualExpressionContext ctx) {
        int expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        int expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        return expr1 == expr2;
    }

    @Override
    public Object visitNumericMultiOpExpression(VeneCuelaParser.NumericMultiOpExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        String operator = ctx.numericMultiOp().getText();

        return switch (operator) {
            case "*" -> expr1 * expr2;
            case "/" -> expr1 / expr2;
            case "%" -> expr1 % expr2;
            default -> null;
        };
    }

    @Override
    public Object visitLessThanExpression(VeneCuelaParser.LessThanExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        return expr1 < expr2;
    }

    @Override
    public Object visitLessThanOrEqualExpression(VeneCuelaParser.LessThanOrEqualExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        return expr1 <= expr2;
    }

    @Override
    public Object visitMoreThanExpression(VeneCuelaParser.MoreThanExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        return expr1 > expr2;
    }

    @Override
    public Object visitMoreThanOrEqualExpression(VeneCuelaParser.MoreThanOrEqualExpressionContext ctx) {
        Integer expr1 = this.getIntegerValue(visit(ctx.expression(0)));
        Integer expr2 = this.getIntegerValue(visit(ctx.expression(1)));

        return expr1 >= expr2;
    }

    @Override
    public Object visitCycleStatement(VeneCuelaParser.CycleStatementContext ctx) {
        while (this.getBooleanValue(visit(ctx.expression()))) {
            visit(ctx.block());
        }

        return null;
    }


    /*******************************
     *           HELPERS           *
     *******************************/


    public boolean getBooleanValue(Object value) {
        boolean isTrue;

        // If argument is variable, get value from variable, otherwise get object itself
        Object currentValue;
        if (value instanceof Variable) {
            currentValue = ((Variable) value).getValue();
        } else {
            currentValue = value;
        }
        // Try casting value to boolean
        try {
            isTrue = (Boolean) currentValue;
        } catch (Exception exception) {
            throw new WrongTypeException();
        }

        return isTrue;
    }

    public Integer getIntegerValue(Object value) {
        Integer integer;

        // If argument is variable, get value from variable, otherwise get object itself
        Object currentValue;
        if (value instanceof Variable) {
            currentValue = ((Variable) value).getValue();
        } else {
            currentValue = value;
        }

        // Try casting value to integer
        try {
            integer = (Integer) currentValue;
        } catch (Exception exception) {
            throw new WrongTypeException();
        }

        return integer;
    }

    public String getStringValue(Object value) {
        String string;

        // If argument is variable, get value from variable, otherwise get object itself
        Object currentValue;
        if (value instanceof Variable) {
            currentValue = ((Variable) value).getValue();
        } else {
            currentValue = value;
        }

        // Try casting value to string
        try {
            string = (String) currentValue;
        } catch (Exception exception) {
            throw new WrongTypeException();
        }

        return string;
    }
}
