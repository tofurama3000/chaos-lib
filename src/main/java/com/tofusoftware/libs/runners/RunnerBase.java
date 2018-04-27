package com.tofusoftware.libs.runners;

import com.tofusoftware.libs.functions.FunctionBase;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RunnerBase<F, FB extends FunctionBase<F>> {

    @SafeVarargs
    public RunnerBase(FB... functions) throws IllegalArgumentException {
        this(Arrays.stream(functions)
        .collect(Collectors.toList()));
    }

    public RunnerBase(List<FB> functions) throws IllegalArgumentException {
        range = 0;
        this.functions = functions.stream().filter(f -> f != null).collect(Collectors.toList());

        if (this.functions.size() == 0) {
            throw new IllegalArgumentException("Must provide at least one valid function!");
        }

        // Since there are two variadic function constructors and no default constructor, we don't need to
        // check if size is 0 since the compiler cannot tell which constructor to use

        for (FB function : this.functions) {
            range += function.getProbability();
        }
        
        if (Double.isInfinite(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is Infinity!");
        }
    }

    /** Local State */

    /**
     * List of chaos functions that are in this Chaos runner
     */
    private List<FB> functions;

    /**
     * Quick cache of probability range (used for internal calculations)
     */
    private double range;

    /**
     * Whether or not chaos is enabled locally (disabling globally will override local settings)
     */
    private boolean runWithChaos = true;

    /** GLOBAL STATE **/

    /**
     * Determines global chaos state
     */
    private static AtomicBoolean runWithChaosGlobal = new AtomicBoolean(true);

    /**
     * Atomically disables global chaos and returns old global chaos setting
     * @return The old value for running with chaos
     */
    public static boolean DisableGlobalChaos() {
        return runWithChaosGlobal.getAndSet(false);
    }

    /**
     * Atomically enables global chaos and returns old global chaos setting
     * @return The old value for running with chaos
     */
    public static boolean EnableGlobalChaos() {
        return runWithChaosGlobal.getAndSet(true);
    }

    /**
     * Returns whether or not global chaos is enabled
     * @return Whether or not chaos is currently enabled
     */
    public static boolean IsGlobalChaosEnabled() {
        return runWithChaosGlobal.get();
    }

    /**
     * Disables chaos for just this runner and returns the old local setting
     * @return Old local setting for running with chaos
     */
    public boolean disableChaos() {
        final boolean returnVal = runWithChaos;
        runWithChaos = false;
        return returnVal;
    }

    /**
     * Disables chaos for just this runner and returns the old local setting
     * @return Old local setting for running with chaos
     */
    public boolean enableChaos() {
        final boolean returnVal = runWithChaos;
        runWithChaos = true;
        return returnVal;
    }

    /**
     * Returns whether or not a call to run(T input) will run with chaos enabled
     * @return Whether or not chaos is enabled
     */
    public boolean willRunWithChaos() {
        return IsGlobalChaosEnabled() && runWithChaos;
    }

    /**
     * Returns the probability range for the chaos runner
     * 
     * @return The probability range for the chaos runner
     */
    public double getRange() {
        return range;
    }

    /**
     * Returns the number of functions that have been registered for the chaos runner
     * 
     * @return The number of registered functions for the chaos runner
     */
    public int numFunctions() {
        return functions.size();
    }

    /**
     * Returns the first registered function (the non-chaos function)
     * 
     * @return Non-chaos function
     */
    protected FB getNonChaosFunction() {
        return functions.get(0);
    }

    /**
     * Returns the iterator for the list of functions
     * 
     * @return Function iterator
     */
    protected FB getRandomFunction() {
        double chaos = Math.random() * getRange();
        for (FB function : this.functions) {
            if (chaos < function.getProbability()) {
                return function;
            }
            chaos -= function.getProbability();
        }
        return this.functions.get(0);
    }

    /**
     * Adds a new function to the chaos runner
     * This function will not be the non-chaos function
     * Will throw if is null, probability is invalid, or the probability makes the
     *  chaos function's range invalid
     * 
     * @param The function to run (cannot be null)
     * @param prob The probability that the function will run (cannot be infinite, 0, or NaN)
     */
    public abstract void add(F consumer, double prob);

    /**
     * Adds a new function to the chaos runner
     * This function will not be the non-chaos function
     * Will throw if the chaos function is null (NullPointerException), or the probability makes the
     *  chaos function's range invalid (IllegalArgumentException)
     * 
     * @param cf The ChaosFunction to add to the registered function list
     * @throws IllegalArgumentException When chaos runner's range would become invalid
     * @throws NullPointerException When cf parameter is null
     */
    public void add(FB cf) throws IllegalArgumentException, NullPointerException {
        if (cf == null) {
            throw new NullPointerException("Chaos Function cannot be null!");
        }
        double range = this.range + cf.getProbability();
        if (Double.isInfinite(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is Infinity!");
        }
        this.functions.add(cf);
        this.range = range;
    }
}
