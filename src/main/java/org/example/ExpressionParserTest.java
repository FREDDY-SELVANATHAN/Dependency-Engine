package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class ExpressionParserTest {

    public static void main(String[] args) {

        String formula = "(Qty + Bonus) * Rate";

        CharStream input = CharStreams.fromString(formula);

        ExpressionLexer lexer = new ExpressionLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ExpressionParser parser = new ExpressionParser(tokens);

        ParseTree tree = parser.expression();

        DependencyListener listener = new DependencyListener();

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(listener, tree);

        System.out.println("Formula: " + formula);
        System.out.println("Dependencies: " + listener.getDependencies());
    }
}