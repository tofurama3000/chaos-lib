package com.tofusoftware.libs;

public class Chaos <T> {
    private ChaosFunction<T>[] functions;
    private double range;

    @SafeVarargs
    public Chaos(ChaosFunction<T>... functions) throws IllegalArgumentException {
        this.functions = functions;
        range = 0;
        for (ChaosFunction<T> function : functions) {
            range += function.getProbability();
        }
        if (functions.length == 0) {
            throw new IllegalArgumentException("Need to specify at least one function");
        }
        if (range == 0) {
            throw new IllegalArgumentException("Need to specify probabilities for functions");
        }
    }

    public void run(T input) {
        if (range == 0 || functions.length == 0) {
            return;
        }
        double chaos = Math.random() * range;
        for (ChaosFunction<T> function : functions) {
            if (chaos < function.getProbability()) {
                function.run(input);
                return;
            }
            chaos -= function.getProbability();
        }
        functions[0].run(input);
    }

    public void runNoChaos(T input) {
    }

    public double getRange() {
        return range;
    }

    public int numFunctions() {
        return functions.length;
    }
}
