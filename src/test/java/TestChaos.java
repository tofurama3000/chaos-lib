import java.lang.IllegalArgumentException;
import com.tofusoftware.libs.Chaos;
import com.tofusoftware.libs.ChaosFunction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class TestChaos {
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void testInit() {
        Chaos<Integer> chaos = new Chaos<>(
                new ChaosFunction<>(x -> {}, 0.5),
                new ChaosFunction<>(x -> {}, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        Chaos<TestTarget> chaos = new Chaos<>(
                new ChaosFunction<>(x -> x.x = true, 0.5)
        );

        TestTarget t = new TestTarget();

        chaos.run(t);
        assertEquals(true, t.x);
    }

    @Test
    public void testMultipleFunctions() {
        Chaos<TestTarget> chaos = new Chaos<>(
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
        Chaos<TestTarget> chaos = new Chaos<>(
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
        Chaos<TestTarget> chaos = new Chaos<>(
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
        Chaos<TestTarget> chaos = new Chaos<>(
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

        new Chaos<TestTarget>(
                new ChaosFunction<>(x -> x.y += 1, 0),
                new ChaosFunction<>(x -> x.z += 1, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        Chaos<TestTarget> chaos = new Chaos<>( x -> {} );
        chaos.add(x -> {}, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        Chaos<TestTarget> chaos = new Chaos<>( x -> {} );
        chaos.add(x -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        Chaos<TestTarget> chaos = new Chaos<>( x -> {} );
        chaos.add(x -> {}, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Function cannot be null!");
        Chaos<TestTarget> chaos = new Chaos<>( x -> {} );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        Chaos<TestTarget> chaos = new Chaos<>( x -> {} );
        chaos.add(null);
    }


    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(Chaos.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(Chaos.DisableGlobalChaos());
        assertFalse(Chaos.DisableGlobalChaos());

        Chaos<TestTarget> chaos = new Chaos<>(
            x -> x.y += 1,
            x -> x.z += 1
        );

        // Check that chos is disabled
        assertFalse(Chaos.IsGlobalChaosEnabled());

        TestTarget t = new TestTarget();
        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            chaos.run(t);
        }
        assertEquals(t.y, Integer.valueOf(numIterations));

        // Check that it returns the last value
        assertFalse(Chaos.EnableGlobalChaos());
        assertTrue(Chaos.EnableGlobalChaos());

        // Check that chaos is enabled
        assertTrue(Chaos.IsGlobalChaosEnabled());
    }
}
