package com.tofusoftware.libs.functions;

import java.util.function.Function;

/**
 * Holds a function with the probability it should run with
 * This is used with the Chaos class to describe each function's probability
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the functions
 * @see ChaosRunner
 */
public class ChaosFunction<T,R> extends FunctionBase<Function<T,R>> {

    /**
     * Creates a new ChaosFunction from a function and it's probability
     * Will throw a NullPointer exception if the is null or an IllegalArgumentException if the probability
     *  is Infinite, NaN, or <= 0
     * 
     * @param func The function that will be called
     * @param prob The probability of the function
     * @throws IllegalArgumentException When prob parameter is <= 0, NaN, or Infinite
     * @throws NullPointerException When func parameter is null
     */
    public ChaosFunction(Function<T,R> func, double prob) throws IllegalArgumentException, NullPointerException {
        super(func, prob);
    }

    /**
     * Runs the internal function using the provided input
     * @param input The input to pass to the internal function
     * @return output of stored function
     */
    public R run(T input) {
        return getFunction().apply(input);
    }
}
