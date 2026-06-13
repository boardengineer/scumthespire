package battleaimod.battleai.evolution.utils.fitness;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ExpressionManager {

    // --- Public API ---

    public static List<CompatExpression> readJeneticsFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        List<CompatExpression> expressions = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            expressions.add(new CompatExpression(line.trim()));
        }

        return expressions;
    }

    public static void writeExpressionsToFile(List<CompatExpression> expressions, String path) throws IOException {
        List<String> lines = new ArrayList<>();

        for (CompatExpression expr : expressions) {
            lines.add(expr.jeneticsE);
        }

        Files.write(Paths.get(path), lines);
    }


}