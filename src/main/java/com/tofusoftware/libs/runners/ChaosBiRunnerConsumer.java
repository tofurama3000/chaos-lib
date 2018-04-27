package com.tofusoftware.libs.runners;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.tofusoftware.libs.functions.ChaosBiConsumer;

/**
 * This class holds a list of functions with probabilities of each function running
 * It's use is for chaos testing (i.e. what happens when something goes wrong part of the time, such as timeouts, errors, etc)
 * When creating a chaos tester, the first function passed in will always be the non-chaos function as well.
 * The non-chaos function will be ran whenever chaos is disabled or runNoChaos is called
 * 
 * @author Matt T.
 * @param <T> The input type accepted for the consumer functions
 */
public class ChaosBiRunnerConsumer <T,U> extends RunnerBase<BiConsumer<T,U>, ChaosBiConsumer<T,U>> {

    /**
     * Creates a new chaos runner from Chaos functions
     * It ignores all occurences of null
     * @param functions A variable list of ChaosFunction that could be called in run; first function will run when not in chaos mode
     * @see ChaosFunction
     * @throws IllegalArgumentException When no chaos functions are provided or total range becomes infinity or NaN
     */
    @SafeVarargs
    public ChaosBiRunnerConsumer(ChaosBiConsumer<T,U>... functions) throws IllegalArgumentException {
        super(functions);
    }

    /**
     * Creates a new chaos runner from a list of consumer functions
     * It ignores all null consumers passed in
     * Consumers are all given equal probability
     * Range will be approximately 1.0 (could be off due to rounding errors)
     * 
     * @param funcs A list of functions that consume type T
     * @throws IllegalArgumentException Thrown when no consumers are provided
     */
    @SafeVarargs
    public ChaosBiRunnerConsumer(BiConsumer<T,U>... funcs) throws IllegalArgumentException {
        super(Arrays.stream(funcs)
            .filter(c ->c != null)
            .map(consumer -> new ChaosBiConsumer<T,U>(
                consumer, 
                1.0 / funcs.length
            ))
            .collect(Collectors.toList())
        );
    }

    /**
     * Picks an available function and runs it
     * It first checks to see if global and local chaos are enabled,
     * if so it picks at random based on the probability of each function
     * Otherwise, it runs the first registered function
     * 
     * @param input The input to pass to the function being ran
     */
    public void run(T input1, U input2) {
        if (!willRunWithChaos()) {
            runNoChaos(input1, input2);
        } else {
            runForceChaos(input1, input2);
        }
    }

    /**
     * Runs the first registered function
     * Same as calling run(T input) with chaos disabled
     * 
     * @param input The input to pass to the function being ran
     */
    public void runNoChaos(T input1, U input2) {
        getNonChaosFunction().run(input1, input2);
    }

    /**
     * Randomly picks a function to run
     * Same as calling run(T input) with chaos enabled
     * 
     * @param input The input to pass to the function being ran
     */
    public void runForceChaos(T input1, U input2) {
        getRandomFunction().run(input1, input2);
    }

    /**
     * Adds a new function to the chaos runner
     * This function will not be the non-chaos function
     * Will throw if consumer is null, probability is invalid, or the probability makes the
     *  chaos function's range invalid
     * Returns reference to this for function chaining
     * 
     * @param func The consumer function to run (cannot be null)
     * @param prob The probability that the function will run (cannot be infinite, 0, or NaN)
     * @throws IllegalArgumentException When probability paramter is invalid or would make the range invalid
     * @throws NullPointerException When the consumer parameter is null
     * @return Returns this for function chaining
     */
    @Override
    public void add(BiConsumer<T,U> func, double prob) throws IllegalArgumentException, NullPointerException {
        add(new ChaosBiConsumer<T,U>(func, prob));
    }
}
