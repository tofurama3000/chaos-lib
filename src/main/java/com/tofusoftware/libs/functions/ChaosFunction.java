package com.tofusoftware.libs.functions;

import java.util.function.Consumer;

/**
 * Holds a consumer function with the probability it should run with
 * This is used with the Chaos class to describe each function's probability
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the consumer functions
 * @see ChaosRunner
 */
public class ChaosFunction<T> extends FunctionBase<Consumer<T>> {

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
        super(func, prob);
    }

    /**
     * Runs the internal function using the provided input
     * @param input The input to pass to the internal function
     */
    public void run(T input) {
        getFunction().accept(input);
    }
}
