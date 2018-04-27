import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import com.tofusoftware.libs.functions.ChaosConsumer;
import com.tofusoftware.libs.runners.ChaosRunnerConsumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosRunnerConsumer {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosRunnerConsumer<Integer> chaos = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> {}, 0.5),
                new ChaosConsumer<>(x -> {}, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.x = true, 0.5)
        );

        TestTarget t = new TestTarget();

        chaos.run(t);
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.y += 1, 0.75),
                new ChaosConsumer<>(x -> x.z += 1, 0.25)
        );

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testMultipleFunctionsWithInferredProbability() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
            x -> x.y += 1,
            x -> x.z += 1
        );

        TestTarget t = new TestTarget();
        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(chaos.getRange(), 1.0, 0.0001);
        assertEquals(1.0, ratio, 1 * 0.10); // make sure the ratio is 1:1 with 10% error
    }

    @Test
    public void testAddChaosFunctionConsumer() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
            new ChaosConsumer<>(x -> x.y += 1, 0.75)
        );
        chaos.add(new ChaosConsumer<>(x -> x.z += 1, 0.25));

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testAddFunction() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
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
    public void canHandleNullChaosFunctionConsumers() {
        ChaosRunnerConsumer<Integer> runner = new ChaosRunnerConsumer<>(
            null,
            new ChaosConsumer<>(x -> x += 1, 0.1),
            new ChaosConsumer<>(x -> x += 1, 0.1)
        );

        assertEquals(2, runner.numFunctions());
        runner = new ChaosRunnerConsumer<>(
            new ChaosConsumer<>(x -> x += 1, 0.1),
            null,
            new ChaosConsumer<>(x -> x += 1, 0.1)
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void canHandleNullConsumerFunctions() {
        ChaosRunnerConsumer<Integer> runner = new ChaosRunnerConsumer<>(
            null,
            x -> x += 1,
            x -> x += 2
        );

        assertEquals(2, runner.numFunctions());

        runner = new ChaosRunnerConsumer<>(
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

        new ChaosRunnerConsumer<Integer>(
            new ChaosConsumer<>(x -> x += 1, Double.MAX_VALUE),
            new ChaosConsumer<>(x -> x += 2, Double.MAX_VALUE)
        );
    }

    @Test
    public void addChaosFunctionConsumerInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunnerConsumer<Integer> runner = new ChaosRunnerConsumer<>(
            new ChaosConsumer<>(x -> x += 1, Double.MAX_VALUE)
        );

        runner.add(new ChaosConsumer<>(x -> x += 2, Double.MAX_VALUE));
    }

    @Test
    public void addConsumerFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunnerConsumer<Integer> runner = new ChaosRunnerConsumer<>(
            new ChaosConsumer<>(x -> x += 1, Double.MAX_VALUE)
        );

        runner.add(x -> x += 2, Double.MAX_VALUE);
    }

    @Test
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosRunnerConsumer<TestTarget>(
                new ChaosConsumer<>(x -> x.y += 1, 0),
                new ChaosConsumer<>(x -> x.z += 1, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>( x -> {} );
        chaos.add(x -> {}, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>( x -> {} );
        chaos.add(x -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>( x -> {} );
        chaos.add(x -> {}, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>( x -> {} );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosFunctionConsumerCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>( x -> {} );
        chaos.add(null);
    }


    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosRunnerConsumer.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(ChaosRunnerConsumer.DisableGlobalChaos());
        assertFalse(ChaosRunnerConsumer.DisableGlobalChaos());

        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
            x -> x.y += 1,
            x -> x.z += 1
        );

        assertFalse(chaos.willRunWithChaos());

        // Check that chos is disabled
        assertFalse(ChaosRunnerConsumer.IsGlobalChaosEnabled());

        TestTarget t = new TestTarget();
        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            chaos.run(t);
        }
        assertEquals(t.y, Integer.valueOf(numIterations));

        // Check that it returns the last value
        assertFalse(ChaosRunnerConsumer.EnableGlobalChaos());
        assertTrue(ChaosRunnerConsumer.EnableGlobalChaos());
        assertTrue(chaos.willRunWithChaos());

        // Check that chaos is enabled
        assertTrue(ChaosRunnerConsumer.IsGlobalChaosEnabled());
    }


    @Test
    public void testDisableLocalChaosFunctionConsumers() {
        ChaosRunnerConsumer<TestTarget> chaos1 = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.y += 1, 0.75),
                new ChaosConsumer<>(x -> x.z += 1, 0.25)
        );

        ChaosRunnerConsumer<TestTarget> chaos2 = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.y += 1, 0.75),
                new ChaosConsumer<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos2.disableChaos());

        TestTarget t1 = new TestTarget();
        TestTarget t2 = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos1.run(t1);
            chaos2.run(t2);
        }
        double ratio = ((double)t1.y) / ((double)t1.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
        assertEquals(0.0, t2.z, 0.01);
        assertEquals(1000000.0, t2.y, 0.01);
    }

    @Test
    public void testEnableLocalChaosFunctionConsumers() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.y += 1, 0.75),
                new ChaosConsumer<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());
        assertFalse(chaos.enableChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.run(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }

    @Test
    public void testForceRunWithChaosFunctionConsumers() {
        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
                new ChaosConsumer<>(x -> x.y += 1, 0.75),
                new ChaosConsumer<>(x -> x.z += 1, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());

        TestTarget t = new TestTarget();

        for (int i = 0; i < 1000000; ++i) {
            chaos.runForceChaos(t);
        }
        double ratio = ((double)t.y) / ((double)t.z);

        assertEquals(3.0, ratio, 3 * 0.10); // make sure the ratio is 3:1 with 10% error
    }


    @Test
    public void testExceptionThrowing() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error!");

        ChaosRunnerConsumer<TestTarget> chaos = new ChaosRunnerConsumer<>(
            new ChaosConsumer<>(new Consumer<TestTarget>(){
                @Override
                public void accept(TestTarget t) throws RuntimeException {
                    throw new RuntimeException("Error!");
                }
            }, 1.0)
        );

        TestTarget t = new TestTarget();

        chaos.run(t);
    }
}
