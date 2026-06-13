package fitnessevolution;

import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FeatureBankLoader {
    private FeatureBankLoader() {}

    public static List<String> loadNames(Path path) throws IOException {
        return Files.readAllLines(path).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .collect(Collectors.toList());
    }

    public static ISeq<Var<Double>> loadVars(Path path) throws IOException {
        List<String> names = loadNames(path);
        List<Var<Double>> vars = new ArrayList<>(names.size());
        for (int i = 0; i < names.size(); i++) {
            vars.add(Var.of(names.get(i), i));
        }
        return ISeq.of(vars);
    }
}
