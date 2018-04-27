import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.BiConsumer;

import com.tofusoftware.libs.functions.ChaosBiConsumer;
import com.tofusoftware.libs.runners.ChaosBiRunnerConsumer;
import com.tofusoftware.libs.runners.RunnerBase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosBiRunnerConsumer {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosBiRunnerConsumer<Integer, Integer> chaos = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, y) -> {}, 0.5),
                new ChaosBiConsumer<>((x, y) -> {}, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosBiRunnerConsumer<TestTarget, Boolean> chaos = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, b) -> x.x = b, 0.5)
        );

        TestTarget t = new TestTarget();

        chaos.run(t, true);
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25)
        );

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testMultipleFunctionsWithInferredProbability() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
            (x, i) -> x.y += i,
            (x, i) -> x.z += i
        );

        TestTarget t = new TestTarget();
        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(chaos.getRange(), 1.0, 0.0001);
        assertEquals(1.0, ratio, 1 * 0.10); // make sure the ratio is 1:1 with 10% error
    }

    @Test
    public void testAddChaosBiConsumer() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
            new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75)
        );
        chaos.add(new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25));

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testAddFunction() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
            (x, i) -> x.y += i
        );
        chaos.add((x, i) -> x.z += i, 1.0);

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(1.0, ratio, 1 * 0.10); // make sure the ratio is 1:1 with 10% error
    }

    @Test
    public void canHandleNullChaosBiConsumers() {
        ChaosBiRunnerConsumer<Integer, Integer> runner = new ChaosBiRunnerConsumer<>(
            null,
            new ChaosBiConsumer<>((x, i) -> x += i, 0.1),
            new ChaosBiConsumer<>((x, i) -> x += i, 0.1)
        );

        assertEquals(2, runner.numFunctions());
        runner = new ChaosBiRunnerConsumer<>(
            new ChaosBiConsumer<>((x, i) -> x += i, 0.1),
            null,
            new ChaosBiConsumer<>((x, i) -> x += i, 0.1)
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void canHandleNullConsumerFunctions() {
        ChaosBiRunnerConsumer<Integer, Integer> runner = new ChaosBiRunnerConsumer<>(
            null,
            (x, i) -> x += i,
            (x, i) -> x += i
        );

        assertEquals(2, runner.numFunctions());

        runner = new ChaosBiRunnerConsumer<>(
            (x, i) -> x += i,
            null,
            (x, i) -> x += i
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void infiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        new ChaosBiRunnerConsumer<Integer, Integer>(
            new ChaosBiConsumer<>((x, i) -> x += i, Double.MAX_VALUE),
            new ChaosBiConsumer<>((x, i) -> x += i + 1, Double.MAX_VALUE)
        );
    }

    @Test
    public void addChaosBiConsumerInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosBiRunnerConsumer<Integer, Integer> runner = new ChaosBiRunnerConsumer<>(
            new ChaosBiConsumer<>((x, i) -> x += i, Double.MAX_VALUE)
        );

        runner.add(new ChaosBiConsumer<>((x, i) -> x += i + 1, Double.MAX_VALUE));
    }

    @Test
    public void addConsumerFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosBiRunnerConsumer<Integer, Integer> runner = new ChaosBiRunnerConsumer<>(
            new ChaosBiConsumer<>((x, i) -> x += i, Double.MAX_VALUE)
        );

        runner.add((x, i) -> x += i + 1, Double.MAX_VALUE);
    }

    @Test
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosBiRunnerConsumer<TestTarget, Integer>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>( (x, y) -> {} );
        chaos.add((x, y) -> {}, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>( (x, y) -> {} );
        chaos.add((x, y) -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>( (x, y) -> {} );
        chaos.add((x, y) -> {}, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>( (x, y) -> {} );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosBiConsumerCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>( (x, y) -> {} );
        chaos.add(null);
    }

    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosBiRunnerConsumer.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(RunnerBase.DisableGlobalChaos());
        assertFalse(ChaosBiRunnerConsumer.DisableGlobalChaos());

        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
            (x, i) -> x.y += i,
            (x, i) -> x.z += i
        );

        assertFalse(chaos.willRunWithChaos());

        // Check that chos is disabled
        assertFalse(ChaosBiRunnerConsumer.IsGlobalChaosEnabled());

        TestTarget t = new TestTarget();
        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            chaos.run(t, 1);
        }
        assertEquals(t.y, Integer.valueOf(numIterations));

        // Check that it returns the last value
        assertFalse(ChaosBiRunnerConsumer.EnableGlobalChaos());
        assertTrue(ChaosBiRunnerConsumer.EnableGlobalChaos());
        assertTrue(chaos.willRunWithChaos());

        // Check that chaos is enabled
        assertTrue(ChaosBiRunnerConsumer.IsGlobalChaosEnabled());
    }


    @Test
    public void testDisableLocalChaosBiConsumers() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos1 = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25)
        );

        ChaosBiRunnerConsumer<TestTarget, Integer> chaos2 = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25)
        );

        assertTrue(chaos2.disableChaos());

        TestTarget t1 = new TestTarget();
        TestTarget t2 = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos1.run(t1, 2);
            chaos2.run(t2, 1);
        }
        double ratio = ((double)t1.y) / ((double)t1.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
        assertEquals(0.0, t2.z, 0.01);
        assertEquals(1000000.0, t2.y, 0.01);
    }

    @Test
    public void testEnableLocalChaosBiConsumers() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());
        assertFalse(chaos.enableChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testForceRunWithChaosBiConsumers() {
        ChaosBiRunnerConsumer<TestTarget, Integer> chaos = new ChaosBiRunnerConsumer<>(
                new ChaosBiConsumer<>((x, i) -> x.y += i, 0.75),
                new ChaosBiConsumer<>((x, i) -> x.z += i, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.runForceChaos(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testExceptionThrowing() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error!");

        ChaosBiRunnerConsumer<TestTarget, Double> chaos = new ChaosBiRunnerConsumer<>(
            new ChaosBiConsumer<>(new BiConsumer<TestTarget, Double>(){
                @Override
                public void accept(TestTarget t, Double d) throws RuntimeException {
                    throw new RuntimeException("Error!");
                }
            }, 1.0)
        );

        TestTarget t = new TestTarget();

        chaos.run(t, 2.0);
    }
}
