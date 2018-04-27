package com.tofusoftware.libs.functions;

import java.util.function.BiFunction;

/**
 * Holds a bi-function with the probability it should run with
 * This is used with the Chaos class to describe each function's probability
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the bi-functions
 * @see ChaosRunner
 */
public class ChaosBiFunction<T,U,R> extends FunctionBase<BiFunction<T,U,R>> {

    /**
     * Creates a new ChaosBiFunction from a bi-function and it's probability
     * Will throw a NullPointer exception if the bi-is null or an IllegalArgumentException if the probability
     *  is Infinite, NaN, or <= 0
     * 
     * @param func The bi-function that will be called
     * @param prob The probability of the bi-function
     * @throws IllegalArgumentException When prob parameter is <= 0, NaN, or Infinite
     * @throws NullPointerException When func parameter is null
     */
    public ChaosBiFunction(BiFunction<T,U,R> func, double prob) throws IllegalArgumentException, NullPointerException {
        super(func, prob);
    }

    /**
     * Runs the internal function using the provided input
     * @param input The input to pass to the internal function
     * @return output of stored function
     */
    public R run(T input1, U input2) {
        return getFunction().apply(input1, input2);
    }
}
