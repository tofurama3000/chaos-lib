package com.tofusoftware.libs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class holds a list of functions with probabilities of each function running
 * It's use is for chaos testing (i.e. what happens when something goes wrong part of the time, such as timeouts, errors, etc)
 * When creating a chaos tester, the first function passed in will always be the non-chaos function as well.
 * The non-chaos function will be ran whenever chaos is disabled or runNoChaos is called
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the consumer functions
 */
public class Chaos <T> {
    /** GLOBAL STATE **/


    /**
     * Determines global chaos state
     */
    private static AtomicBoolean runWithChaos = new AtomicBoolean(true);

    /**
     * Atomically disables global chaos and returns old global chaos setting
     * @return The old value for running with chaos
     */
    public static boolean DisableGlobalChaos() {
        return runWithChaos.getAndSet(false);
    }

    /**
     * Atomically enables global chaos and returns old global chaos setting
     * @return The old value for running with chaos
     */
    public static boolean EnableGlobalChaos() {
        return runWithChaos.getAndSet(true);
    }

    /**
     * Returns whether or not global chaos is enabled
     * @return Whether or not chaos is currently enabled
     */
    public static boolean IsGlobalChaosEnabled() {
        return runWithChaos.get();
    }

    /** LOCAL STATE **/

    /**
     * List of chaos functions that are in this Chaos runner
     */
    private List<ChaosFunction<T>> functions;

    /**
     * Quick cache of probability range (used for internal calculations)
     */
    private double range;

    /**
     * Creates a new chaos runner from Chaos functions
     * It ignores all occurences of null
     * @param functions A variable list of ChaosFunction that could be called in run; first function will run when not in chaos mode
     * @see ChaosFunction
     * @throws IllegalArgumentException When no chaos functions are provided or total range becomes infinity or NaN
     */
    @SafeVarargs
    public Chaos(ChaosFunction<T>... functions) throws IllegalArgumentException {
        range = 0;
        this.functions = Arrays.stream(functions)
            .filter(f -> f != null)
            .collect(Collectors.toList());
        if (this.functions.size() == 0) {
            throw new IllegalArgumentException("Need to specify at least one function");
        }

        for (ChaosFunction<T> function : functions) {
            range += function.getProbability();
        }
        
        if (Double.isInfinite(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is Infinity!");
        }
        if (Double.isNaN(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is NaN!");
        }
    }

    /**
     * Creates a new chaos runner from a list of consumer functions
     * It ignores all null consumers passed in
     * Consumers are all given equal probability
     * Range will be approximately 1.0 (could be off due to rounding errors)
     * 
     * @param consumers A list of functions that consume type T
     * @throws IllegalArgumentException Thrown when no consumers are provided
     */
    @SafeVarargs
    public Chaos(Consumer<T>... consumers) throws IllegalArgumentException {
        if (consumers.length == 0) {
            throw new IllegalArgumentException("Need to specify at least one function");
        }
        int numConsumers = consumers.length;
        double probabilityPerFunction = 1.0 / numConsumers;
        range = probabilityPerFunction * numConsumers;
        this.functions = Arrays.stream(consumers)
            .filter(c -> c != null)
            .map(consumer -> new ChaosFunction<>(consumer, probabilityPerFunction))
            .collect(Collectors.toList());
    }

    /**
     * Picks an available function and runs it
     * It first checks to see if global and local chaos are enabled,
     * if so it picks at random based on the probability of each function
     * Otherwise, it runs the first registered function
     * 
     * @param input The input to pass to the function being ran
     */
    public void run(T input) {
        if (runWithChaos.get() == false) {
            runNoChaos(input);
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
        functions.get(0).run(input);
    }

    /**
     * Runs the first registered function
     * Same as calling run(T input) with chaos disabled
     * 
     * @param input The input to pass to the function being ran
     */
    public void runNoChaos(T input) {
        functions.get(0).run(input);
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
     * Adds a new function to the chaos runner
     * This function will not be the non-chaos function
     * Will throw if consumer is null, probability is invalid, or the probability makes the
     *  chaos function's range invalid
     * Returns reference to this for function chaining
     * 
     * @param consumer The consumer function to run (cannot be null)
     * @param prob The probability that the function will run (cannot be infinite, 0, or NaN)
     * @throws IllegalArgumentException When probability paramter is invalid or would make the range invalid
     * @throws NullPointerException When the consumer parameter is null
     * @return Returns this for function chaining
     */
    public Chaos<T> add(Consumer<T> consumer, double prob) throws IllegalArgumentException, NullPointerException {

        if (Double.isInfinite(prob) || Double.isNaN(prob)) {
            throw new IllegalArgumentException("Probability cannot be Infinity or NaN!");
        }
        if (prob <= 0) {
            throw new IllegalArgumentException("Probability must be greater than 0!");
        }
        if (consumer == null) {
            throw new NullPointerException("Function cannot be null!");
        }
        double range = this.range + prob;
        if (Double.isInfinite(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is Infinity!");
        }
        if (Double.isNaN(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is NaN!");
        }

        this.functions.add(new ChaosFunction<>(consumer, prob));
        this.range = range;
        return this;
    }

    /**
     * Adds a new function to the chaos runner
     * This function will not be the non-chaos function
     * Will throw if the chaos function is null (NullPointerException), or the probability makes the
     *  chaos function's range invalid (IllegalArgumentException)
     * Returns this for function chaining
     * 
     * @param cf The ChaosFunction to add to the registered function list
     * @throws IllegalArgumentException When chaos runner's range would become invalid
     * @throws NullPointerException When cf parameter is null
     * @return Returns this for function chaining
     */
    public Chaos<T> add(ChaosFunction<T> cf) throws IllegalArgumentException, NullPointerException {
        if (cf == null) {
            throw new NullPointerException("Chaos Function cannot be null!");
        }
        double range = this.range + cf.getProbability();
        if (Double.isInfinite(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is Infinity!");
        }
        if (Double.isNaN(range)) {
            throw new IllegalArgumentException("Bad probabilities! Total range is NaN!");
        }
        this.functions.add(cf);
        this.range = range;
        return this;
    }
}
