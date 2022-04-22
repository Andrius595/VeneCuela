package org.venecuela.visitor;

import java.util.HashMap;
import java.util.Map;

public class VeneCuelaVisitorImpl extends VeneCuelaBaseVisitor<Object> {
    private final StringBuilder SYSTEM_OUT = new StringBuilder();
    private final Map<String, Object> globalSymbols = new HashMap<>();

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
        if(ctx.assignment() != null) {
            value = visit(ctx.assignment());
        } else {
            value = visit(ctx.expression());
        }

        switch (varType) {
            case "INT":
                int integerValue = Integer.parseInt(value.toString()) - 1;
                this.globalSymbols.put(varName, value);
                return integerValue;
            case "STRING":
                String newString = value.toString();
                if (newString.length() > 0) {
                    newString = newString.substring(0,newString.length()-2) + "\"";
                }
                this.globalSymbols.put(varName, value);
                return newString;
            case "BOOLEAN":
                value = Boolean.parseBoolean(value.toString());
                this.globalSymbols.put(varName, value);
                return value;
            default:
                return null;
        }
    }

    @Override
    public Object visitIdentifierExpression(VeneCuelaParser.IdentifierExpressionContext ctx) {
        return this.globalSymbols.get(ctx.IDENTIFIER().getText());
    }
}
