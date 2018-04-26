import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tofusoftware.libs.ChaosFunction;
import com.tofusoftware.libs.ChaosRunner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosRunner {
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void testInit() {
        ChaosRunner<Integer> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> {}, 0.5),
                new ChaosFunction<>(x -> {}, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.x = true, 0.5)
        );

        TestTarget t = new TestTarget();

        chaos.run(t);
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
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
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
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
    public void testAddChaosFunction() {
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
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
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
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
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosRunner<TestTarget>(
                new ChaosFunction<>(x -> x.y += 1, 0),
                new ChaosFunction<>(x -> x.z += 1, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>( x -> {} );
        chaos.add(x -> {}, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>( x -> {} );
        chaos.add(x -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>( x -> {} );
        chaos.add(x -> {}, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Function cannot be null!");
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>( x -> {} );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>( x -> {} );
        chaos.add(null);
    }


    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosRunner.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(ChaosRunner.DisableGlobalChaos());
        assertFalse(ChaosRunner.DisableGlobalChaos());

        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
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
        ChaosRunner<TestTarget> chaos1 = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
        );

        ChaosRunner<TestTarget> chaos2 = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
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
    public void testEnableLocalChaosFunctions() {
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
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
    public void testForceRunWithChaosFunctions() {
        ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
                new ChaosFunction<>(x -> x.y += 1, 0.75),
                new ChaosFunction<>(x -> x.z += 1, 0.25)
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
}
