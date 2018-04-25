package com.tofusoftware.libs;

import java.util.function.Consumer;

/**
 * Holds a consumer function with the probability it should run with
 * This is used with the Chaos class to describe each function's probability
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the consumer functions
 * @see ChaosRunner
 */
public class ChaosFunction<T> {
    /**
     * Probability for the Consumer function being called
     */
    private double probability;
    /**
     * The consumer function to call
     */
    private Consumer<T> function;

    /**
     * Creates a new ChaosFunction from a consumer function and it's probability
     * Will throw a NullPointer exception if the consumer is null or an IllegalArgumentException if the probability
     *  is Infinite, NaN, or <= 0
     * 
     * @param func The consumer function that will be called
     * @param prob The probability of the consumer function
     * @throws IllegalArgumentException When prob parameter is <= 0, NaN, or Infinite
     * @throws NullPointerException When func parameter is null
     */
    public ChaosFunction(Consumer<T> func, double prob) throws IllegalArgumentException, NullPointerException {
        if (func == null) {
            throw new NullPointerException("Need to specify a function");
        }
        if (Double.isNaN(prob) || Double.isInfinite(prob)) {
            throw new IllegalArgumentException("Probability cannot be Infinity or NaN!");
        }
        if (prob <= 0) {
            throw new IllegalArgumentException("Probability must be greater than 0!");
        }
        probability = prob;
        function = func;
    }

    /**
     * Returns the probability for the chaos function
     * @return The probability for the chaos function
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Runs the internal function using the provided input
     * @param input The input to pass to the internal function
     */
    public void run(T input) {
        function.accept(input);
    }
}
