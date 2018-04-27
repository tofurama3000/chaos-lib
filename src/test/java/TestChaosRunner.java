import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import com.tofusoftware.libs.functions.ChaosFunction;
import com.tofusoftware.libs.runners.ChaosRunner;
import com.tofusoftware.libs.runners.RunnerBase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosRunner {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosRunner<Integer, Integer> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> 0, 0.5),
                new ChaosFunction<>(x -> 0, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosRunner<TestTarget, Boolean> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.x = true, 0.5)
        );

        TestTarget t = new TestTarget();

        assertTrue(chaos.run(t));
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            assertTrue(chaos.run(t) > 0);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testMultipleFunctionsWithInferredProbability() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
            x -> x.y += 1,
            x -> x.z += 1
        );

        TestTarget t = new TestTarget();
        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run(t);
            assertTrue(val > 0);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(chaos.getRange(), 1.0, 0.0001);
        assertEquals(1.0, ratio, 1 * 0.10); // make sure the ratio is 1:1 with 10% error
    }

    @Test
    public void testAddChaosFunction() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
            new ChaosFunction<>(x -> x.y += 1, 0.75)
        );
        chaos.add(new ChaosFunction<>(x -> x.z += 1, 0.25));

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testAddFunction() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
            x -> x.y += 1
        );
        chaos.add(x -> x.z += 1, 1.0);

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(1.0, ratio, 1 * 0.10); // make sure the ratio is 1:1 with 10% error
    }

    @Test
    public void canHandleNullChaosFunctions() {
        ChaosRunner<Integer, Integer> runner = new ChaosRunner<>(
            null,
            new ChaosFunction<>(x -> x += 1, 0.1),
            new ChaosFunction<>(x -> x += 1, 0.1)
        );

        assertEquals(2, runner.numFunctions());
        runner = new ChaosRunner<>(
            new ChaosFunction<>(x -> x += 1, 0.1),
            null,
            new ChaosFunction<>(x -> x += 1, 0.1)
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void canHandleNullConsumerFunctions() {
        ChaosRunner<Integer, Integer> runner = new ChaosRunner<>(
            null,
            x -> x += 1,
            x -> x += 2
        );

        assertEquals(2, runner.numFunctions());

        runner = new ChaosRunner<>(
            x -> x += 1,
            null,
            x -> x += 2
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void infiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        new ChaosRunner<Integer, Integer>(
            new ChaosFunction<>(x -> x += 1, Double.MAX_VALUE),
            new ChaosFunction<>(x -> x += 2, Double.MAX_VALUE)
        );
    }

    @Test
    public void addChaosFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunner<Integer, Integer> runner = new ChaosRunner<>(
            new ChaosFunction<>(x -> x += 1, Double.MAX_VALUE)
        );

        runner.add(new ChaosFunction<>(x -> x += 2, Double.MAX_VALUE));
    }

    @Test
    public void addConsumerFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunner<Integer, Integer> runner = new ChaosRunner<>(
            new ChaosFunction<>(x -> x += 1, Double.MAX_VALUE)
        );

        runner.add(x -> x += 2, Double.MAX_VALUE);
    }

    @Test
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosRunner<TestTarget, Integer>(
                new ChaosFunction<>(x -> x.y += 1, 0),
                new ChaosFunction<>(x -> x.z += 1, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>( x -> 0 );
        chaos.add(x -> 0, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>( x -> 0 );
        chaos.add(x -> 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>( x -> 0 );
        chaos.add(x -> 0, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>( x -> 0 );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>( x -> 0 );
        chaos.add(null);
    }

    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosRunner.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(RunnerBase.DisableGlobalChaos());
        assertFalse(ChaosRunner.DisableGlobalChaos());

        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
            x -> x.y += 1,
            x -> x.z += 1
        );

        assertFalse(chaos.willRunWithChaos());

        // Check that chos is disabled
        assertFalse(ChaosRunner.IsGlobalChaosEnabled());

        TestTarget t = new TestTarget();
        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            chaos.run(t);
        }
        assertEquals(t.y, Integer.valueOf(numIterations));

        // Check that it returns the last value
        assertFalse(ChaosRunner.EnableGlobalChaos());
        assertTrue(ChaosRunner.EnableGlobalChaos());
        assertTrue(chaos.willRunWithChaos());

        // Check that chaos is enabled
        assertTrue(ChaosRunner.IsGlobalChaosEnabled());
    }


    @Test
    public void testDisableLocalChaosFunctions() {
        ChaosRunner<TestTarget, Integer> chaos1 = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        ChaosRunner<TestTarget, Integer> chaos2 = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos2.disableChaos());

        TestTarget t1 = new TestTarget();
        TestTarget t2 = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos1.run(t1);
            Integer val2 = chaos2.run(t2);
            assertEquals(Integer.valueOf(i + 1), val2);
        }
        double ratio = ((double)t1.y) / ((double)t1.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
        assertEquals(0.0, t2.z, 0.01);
        assertEquals(1000000.0, t2.y, 0.01);
    }

    @Test
    public void testEnableLocalChaosFunctions() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());
        assertFalse(chaos.enableChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run(t);
            assertTrue(val > 0);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testForceRunWithChaosFunctions() {
        ChaosRunner<TestTarget, Integer> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            assertTrue(chaos.runForceChaos(t) > 0);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testExceptionThrowing() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error!");

        ChaosRunner<TestTarget, Double> chaos = new ChaosRunner<>(
            new ChaosFunction<>(new Function<TestTarget, Double>(){
                @Override
                public Double apply(TestTarget t) throws RuntimeException {
                    throw new RuntimeException("Error!");
                }
            }, 1.0)
        );

        TestTarget t = new TestTarget();

        chaos.run(t);
    }
}
