import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import com.tofusoftware.libs.functions.ChaosBiFunction;
import com.tofusoftware.libs.runners.ChaosBiRunner;
import com.tofusoftware.libs.runners.RunnerBase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosBiRunner {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosBiRunner<Integer, Integer, Integer> chaos = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, y) -> 0, 0.5),
                new ChaosBiFunction<>((x, y) -> 0, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosBiRunner<TestTarget, Boolean, Boolean> chaos = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, b) -> x.x = b, 0.5)
        );

        TestTarget t = new TestTarget();

        assertTrue(chaos.run(t, true));
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0.75),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0.25)
        );

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            Integer res = chaos.run(t, 2);
            assertTrue(res > 0);
            assertTrue(res % 2 == 0);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testMultipleFunctionsWithInferredProbability() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
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
    public void testAddChaosBiFunction() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
            new ChaosBiFunction<>((x, i) -> x.y += i, 0.75)
        );
        chaos.add(new ChaosBiFunction<>((x, i) -> x.z += i, 0.25));

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t, 2);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testAddFunction() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
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
    public void canHandleNullChaosBiFunctions() {
        ChaosBiRunner<Integer, Integer, Integer> runner = new ChaosBiRunner<>(
            null,
            new ChaosBiFunction<>((x, i) -> x += i, 0.1),
            new ChaosBiFunction<>((x, i) -> x += i, 0.1)
        );

        assertEquals(2, runner.numFunctions());
        runner = new ChaosBiRunner<>(
            new ChaosBiFunction<>((x, i) -> x += i, 0.1),
            null,
            new ChaosBiFunction<>((x, i) -> x += i, 0.1)
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void canHandleNullConsumerFunctions() {
        ChaosBiRunner<Integer, Integer, Integer> runner = new ChaosBiRunner<>(
            null,
            (x, i) -> x += i,
            (x, i) -> x += i
        );

        assertEquals(2, runner.numFunctions());

        runner = new ChaosBiRunner<>(
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

        new ChaosBiRunner<Integer, Integer, Integer>(
            new ChaosBiFunction<>((x, i) -> x += i, Double.MAX_VALUE),
            new ChaosBiFunction<>((x, i) -> x += i + 1, Double.MAX_VALUE)
        );
    }

    @Test
    public void addChaosBiFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosBiRunner<Integer, Integer, Integer> runner = new ChaosBiRunner<>(
            new ChaosBiFunction<>((x, i) -> x += i, Double.MAX_VALUE)
        );

        runner.add(new ChaosBiFunction<>((x, i) -> x += i + 1, Double.MAX_VALUE));
    }

    @Test
    public void addConsumerFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosBiRunner<Integer, Integer, Integer> runner = new ChaosBiRunner<>(
            new ChaosBiFunction<>((x, i) -> x += i, Double.MAX_VALUE)
        );

        runner.add((x, i) -> x += i + 1, Double.MAX_VALUE);
    }

    @Test
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosBiRunner<TestTarget, Integer, Integer>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>( (x, y) -> 0 );
        chaos.add((x, y) -> 0, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>( (x, y) -> 0 );
        chaos.add((x, y) -> 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>( (x, y) -> 0 );
        chaos.add((x, y) -> 0, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>( (x, y) -> 0 );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosBiFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>( (x, y) -> 0 );
        chaos.add(null);
    }

    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosBiRunner.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(RunnerBase.DisableGlobalChaos());
        assertFalse(ChaosBiRunner.DisableGlobalChaos());

        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
            (x, i) -> x.y += i,
            (x, i) -> x.z += i
        );

        assertFalse(chaos.willRunWithChaos());

        // Check that chos is disabled
        assertFalse(ChaosBiRunner.IsGlobalChaosEnabled());

        TestTarget t = new TestTarget();
        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            chaos.run(t, 1);
        }
        assertEquals(t.y, Integer.valueOf(numIterations));

        // Check that it returns the last value
        assertFalse(ChaosBiRunner.EnableGlobalChaos());
        assertTrue(ChaosBiRunner.EnableGlobalChaos());
        assertTrue(chaos.willRunWithChaos());

        // Check that chaos is enabled
        assertTrue(ChaosBiRunner.IsGlobalChaosEnabled());
    }


    @Test
    public void testDisableLocalChaosBiFunctions() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos1 = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0.75),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0.25)
        );

        ChaosBiRunner<TestTarget, Integer, Integer> chaos2 = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0.75),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0.25)
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
    public void testEnableLocalChaosBiFunctions() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0.75),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0.25)
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
    public void testForceRunWithChaosBiFunctions() {
        ChaosBiRunner<TestTarget, Integer, Integer> chaos = new ChaosBiRunner<>(
                new ChaosBiFunction<>((x, i) -> x.y += i, 0.75),
                new ChaosBiFunction<>((x, i) -> x.z += i, 0.25)
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

        ChaosBiRunner<TestTarget, Double, Double> chaos = new ChaosBiRunner<>(
            new ChaosBiFunction<>(new BiFunction<TestTarget, Double, Double>(){
                @Override
                public Double apply(TestTarget t, Double d) throws RuntimeException {
                    throw new RuntimeException("Error!");
                }
            }, 1.0)
        );

        TestTarget t = new TestTarget();

        chaos.run(t, 2.0);
    }
}
