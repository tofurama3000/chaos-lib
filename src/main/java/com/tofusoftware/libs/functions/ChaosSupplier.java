package com.tofusoftware.libs.functions;

import java.util.function.Supplier;

/**
 * Holds a supplier function with the probability it should run with
 * This is used with the Chaos class to describe each function's probability
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the Supplier functions
 * @see ChaosRunner
 */
public class ChaosSupplier<T> extends FunctionBase<Supplier<T>> {

    /**
     * Creates a new ChaosFunction from a Supplier function and it's probability
     * Will throw a NullPointer exception if the Supplier is null or an IllegalArgumentException if the probability
     *  is Infinite, NaN, or <= 0
     * 
     * @param func The Supplier function that will be called
     * @param prob The probability of the Supplier function
     * @throws IllegalArgumentException When prob parameter is <= 0, NaN, or Infinite
     * @throws NullPointerException When func parameter is null
     */
    public ChaosSupplier(Supplier<T> func, double prob) throws IllegalArgumentException, NullPointerException {
        super(func, prob);
    }

    /**
     * Runs the internal function using the provided input
     * @return output of stored function
     */
    public T run() {
        return getFunction().get();
    }
}
