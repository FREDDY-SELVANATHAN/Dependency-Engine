package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;

public class DependencyAnalyzer {

    public List<String> analyze(String formula) {

        CharStream input = CharStreams.fromString(formula);

        ExpressionLexer lexer = new ExpressionLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ExpressionParser parser = new ExpressionParser(tokens);

        ParseTree tree = parser.expression();

        DependencyListener listener = new DependencyListener();

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(listener, tree);

        return listener.getDependencies();
    }
}