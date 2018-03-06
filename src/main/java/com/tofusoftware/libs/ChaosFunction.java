package com.tofusoftware.libs;

import java.util.function.Consumer;

public class ChaosFunction<T> {
    private double probability;
    private Consumer<T> function;

    public ChaosFunction(Consumer<T> func, double prob) throws IllegalArgumentException {
        if (func == null) {
            throw new IllegalArgumentException("Need to specify a function");
        }
        probability = Math.abs(prob);
        function = func;
    }

    public double getProbability() {
        return probability;
    }

    public void run(T input) {
        function.accept(input);
    }
}
