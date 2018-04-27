package com.tofusoftware.libs.functions;

/**
 * This class serves as the basis for the various ChaosFunction types
 * 
 * @author Matt T.
 */
public abstract class FunctionBase<F> {
    /**
     * Probability for the Consumer function being called
     */
    private double probability;

    /**
     * The function to call
     */
    private final F function;

    /**
     * Creates a new FunctionBase by setting the probability
     * Throws an error if the param prob is NaN, Ininite, or <= 0
     * 
     * @param prob The probability to user
     * @throws IllegalArgumentException Thrown if the param prob is NaN, Ininite, or <= 0
     * @throws NullPointerException When func parameter is null
     */
    public FunctionBase(F func, double prob) throws IllegalArgumentException, NullPointerException {
        if (Double.isNaN(prob) || Double.isInfinite(prob)) {
            throw new IllegalArgumentException("Probability cannot be Infinity or NaN!");
        }
        if (prob <= 0) {
            throw new IllegalArgumentException("Probability must be greater than 0!");
        }

        if (func == null) {
            throw new NullPointerException("Need to specify a function!");
        }
        
        function = func;
        probability = prob;
    }

    /**
     * Returns the probability for the chaos function
     * @return The probability for the chaos function
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Returns the stored function
     * @return the stored function
     */
    protected F getFunction() {
        return function;
    }
}
