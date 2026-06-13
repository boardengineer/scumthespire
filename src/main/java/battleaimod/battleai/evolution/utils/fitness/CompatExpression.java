package battleaimod.battleai.evolution.utils.fitness;

import battleaimod.battleai.evolution.utils.ValueFunctionManager;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatExpression extends AbstractFitness {
    public Expression e;
    public String jeneticsE;

    public double fitness;

    public CompatExpression(String jeneticsE) {
        String normalized = normalizeJeneticsToExp4j(jeneticsE);
        Set<String> variables = extractVariables(normalized);

        Expression expr = new ExpressionBuilder(normalized)
                .variables(variables)
                .build();

        this.e = expr;
        this.jeneticsE = jeneticsE;
    }

    private Set<String> extractVariables(String expr) {
        Set<String> vars = new HashSet<>();

        // Matches variable names (simple heuristic)
        Pattern pattern = Pattern.compile("\\b[A-Z][A-Z_]*\\b"); //ALL_CAPS_WITH_UNDERSCORES
        Matcher matcher = pattern.matcher(expr);

        while (matcher.find()) {
            String token = matcher.group();

            if (!isFunction(token) && !isConstant(token)) {
                vars.add(token);
            }
        }

        return vars;
    }

    private static boolean isFunction(String token) {
        return Arrays.asList(
                "sin", "cos", "tan",
                "log", "sqrt", "abs", "exp"
        ).contains(token);
    }

    private static boolean isConstant(String token) {
        return token.equals("pi") || token.equals("e");
    }

    @Override
    public double evaluate() {

        for (ValueFunctionManager.Variables v : ValueFunctionManager.Variables.values()) {
            e.setVariable(v.name(), ValueFunctionManager.getVariableValue(v));
        }

        double result = e.evaluate();

        return Double.isFinite(result) ? result : Double.NEGATIVE_INFINITY;
    }

    private static String normalizeJeneticsToExp4j(String expr) {
        String out = expr;

        // Constants
        out = out.replace("PI", "pi");
        out = out.replace("π", "pi");

        // pow(a,b) -> (a^b)
        out = replacePow(out);

        return out;
    }

    private static String replacePow(String expr) {
        Pattern pattern = Pattern.compile("pow\\(([^,]+),([^)]+)\\)");
        Matcher matcher = pattern.matcher(expr);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String base = matcher.group(1).trim();
            String exp = matcher.group(2).trim();
            String replacement = "(" + base + "^" + exp + ")";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    @Override
    public String toString() {
        return jeneticsE;
    }
}
