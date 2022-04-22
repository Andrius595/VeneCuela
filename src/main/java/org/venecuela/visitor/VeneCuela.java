package org.venecuela.visitor;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class VeneCuela {
    public static void main(String[] args) {
        try {
            execute(CharStreams.fromFileName(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object execute(CharStream stream) {
        VeneCuelaLexer lexer = new VeneCuelaLexer(stream);
        VeneCuelaParser parser = new VeneCuelaParser(new CommonTokenStream(lexer));
        parser.setBuildParseTree(true);
        ParseTree tree = parser.program();

        VeneCuelaVisitorImpl visitor = new VeneCuelaVisitorImpl();
        return visitor.visit(tree);
    }
}
