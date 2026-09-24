package org.example;

import java.util.List;

public class DependencyAnalyzerTest {

    public static void main(String[] args) {

        String formula =
                "SubTotal * Customer.DiscountPct / 100";

        DependencyListener listener = new DependencyListener();

        org.antlr.v4.runtime.CharStream input =
                org.antlr.v4.runtime.CharStreams.fromString(formula);

        ExpressionLexer lexer =
                new ExpressionLexer(input);

        org.antlr.v4.runtime.CommonTokenStream tokens =
                new org.antlr.v4.runtime.CommonTokenStream(lexer);

        ExpressionParser parser =
                new ExpressionParser(tokens);

        org.antlr.v4.runtime.tree.ParseTree tree =
                parser.expression();

        org.antlr.v4.runtime.tree.ParseTreeWalker walker =
                new org.antlr.v4.runtime.tree.ParseTreeWalker();

        walker.walk(listener, tree);

        System.out.println("Formula: " + formula);
        System.out.println("Dependencies: "
                + listener.getDependencies());

        System.out.println("Positions: "
                + listener.getDependencyPositions());
    }
}