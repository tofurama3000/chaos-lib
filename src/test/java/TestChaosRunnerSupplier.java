import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import com.tofusoftware.libs.functions.ChaosSupplier;
import com.tofusoftware.libs.runners.ChaosRunnerSupplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestChaosRunnerSupplier {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 0, 0.5),
                new ChaosSupplier<>(() -> 0, 0.75)
        );

        assertEquals(1.25, chaos.getRange(), 0.0001);
        assertEquals(2, chaos.numFunctions());
    }

    @Test
    public void testSingleFunction() {
        ChaosRunnerSupplier<Boolean> chaos = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> true, 0.5)
        );

        assertTrue(chaos.run());
    }

    @Test
    public void testMultipleFunctions() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 1, 0.75),
                new ChaosSupplier<>(() -> 0, 0.25)
        );

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }

    @Test
    public void testMultipleFunctionsWithInferredProbability() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
            () -> 1,
            () -> 0
        );

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }

    @Test
    public void testAddChaosFunctionSupplier() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
            new ChaosSupplier<>(() -> 1, 0.75)
        );
        chaos.add(new ChaosSupplier<>(() -> 0, 0.25));

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }

    @Test
    public void testAddFunction() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
            () -> 1
        );
        chaos.add(() -> 0, 1.0);

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }

    @Test
    public void canHandleNullChaosFunctionSuppliers() {
        ChaosRunnerSupplier<Integer> runner = new ChaosRunnerSupplier<>(
            null,
            new ChaosSupplier<>(() -> 2, 0.1),
            new ChaosSupplier<>(() -> 2, 0.1)
        );

        assertEquals(2, runner.numFunctions());
        runner = new ChaosRunnerSupplier<>(
            new ChaosSupplier<>(() -> 2, 0.1),
            null,
            new ChaosSupplier<>(() -> 2, 0.1)
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void canHandleNullSupplierFunctions() {
        ChaosRunnerSupplier<Integer> runner = new ChaosRunnerSupplier<>(
            null,
            () -> 2,
            () -> 3
        );

        assertEquals(2, runner.numFunctions());

        runner = new ChaosRunnerSupplier<>(
            () -> 2,
            null,
            () -> 3
        );

        assertEquals(2, runner.numFunctions());
    }

    @Test
    public void infiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        new ChaosRunnerSupplier<Integer>(
            new ChaosSupplier<>(() -> 2, Double.MAX_VALUE),
            new ChaosSupplier<>(() -> 3, Double.MAX_VALUE)
        );
    }

    @Test
    public void addChaosFunctionSupplierInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunnerSupplier<Integer> runner = new ChaosRunnerSupplier<>(
            new ChaosSupplier<>(() -> 2, Double.MAX_VALUE)
        );

        runner.add(new ChaosSupplier<>(() -> 3, Double.MAX_VALUE));
    }

    @Test
    public void addSupplierFunctionInfiniteRangeException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bad probabilities! Total range is Infinity!");

        ChaosRunnerSupplier<Integer> runner = new ChaosRunnerSupplier<>(
            new ChaosSupplier<>(() -> 2, Double.MAX_VALUE)
        );

        runner.add(() -> 3, Double.MAX_VALUE);
    }

    @Test
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");

        new ChaosRunnerSupplier<Integer>(
                new ChaosSupplier<>(() -> 1, 0),
                new ChaosSupplier<>(() -> 0, 0)
        );
    }

    @Test
    public void testAddFunctionZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>( () -> 0 );
        chaos.add(() -> 0, 0);
    }

    @Test
    public void testAddFunctionInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>( () -> 0 );
        chaos.add(() -> 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAddFunctionNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>( () -> 0 );
        chaos.add(() -> 0, Double.NaN);
    }

    @Test
    public void testAddFunctionCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>( () -> 0 );
        chaos.add(null, 1);
    }

    @Test
    public void testAddChaosFunctionSupplierCannotBeNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Chaos Function cannot be null!");
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>( () -> 0 );
        chaos.add(null);
    }


    @Test
    public void testChaosToggle() {
        // Check that chaos is enabled
        assertTrue(ChaosRunnerSupplier.IsGlobalChaosEnabled());

        // Check to make sure that it returns the last value
        assertTrue(ChaosRunnerSupplier.DisableGlobalChaos());
        assertFalse(ChaosRunnerSupplier.DisableGlobalChaos());

        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
            () -> 1,
            () -> 0
        );

        assertFalse(chaos.willRunWithChaos());

        // Check that chos is disabled
        assertFalse(ChaosRunnerSupplier.IsGlobalChaosEnabled());

        int numIterations = 1000000;
        for (int i = 0; i < numIterations; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1);
        }

        // Check that it returns the last value
        assertFalse(ChaosRunnerSupplier.EnableGlobalChaos());
        assertTrue(ChaosRunnerSupplier.EnableGlobalChaos());
        assertTrue(chaos.willRunWithChaos());

        // Check that chaos is enabled
        assertTrue(ChaosRunnerSupplier.IsGlobalChaosEnabled());
    }


    @Test
    public void testDisableLocalChaosFunctionSuppliers() {
        ChaosRunnerSupplier<Integer> chaos1 = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 1, 0.75),
                new ChaosSupplier<>(() -> 0, 0.25)
        );

        ChaosRunnerSupplier<Integer> chaos2 = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 1, 0.75),
                new ChaosSupplier<>(() -> 0, 0.25)
        );

        assertTrue(chaos2.disableChaos());

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos1.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
            val = chaos2.run();
            assert(val.intValue() == 1);
        }
    }

    @Test
    public void testEnableLocalChaosFunctionSuppliers() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 1, 0.75),
                new ChaosSupplier<>(() -> 0, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());
        assertFalse(chaos.enableChaos());

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.run();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }

    @Test
    public void testForceRunWithChaosFunctionSuppliers() {
        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
                new ChaosSupplier<>(() -> 1, 0.75),
                new ChaosSupplier<>(() -> 0, 0.25)
        );

        assertTrue(chaos.willRunWithChaos());
        assertTrue(chaos.disableChaos());
        assertFalse(chaos.willRunWithChaos());

        for (int i = 0; i < 1000000; ++i) {
            Integer val = chaos.runForceChaos();
            assert(val.intValue() == 1 || val.intValue() == 0);
        }
    }


    @Test
    public void testExceptionThrowing() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error!");

        ChaosRunnerSupplier<Integer> chaos = new ChaosRunnerSupplier<>(
            new ChaosSupplier<>(new Supplier<Integer>(){
                @Override
                public Integer get() throws RuntimeException {
                    throw new RuntimeException("Error!");
                }
            }, 1.0)
        );

        chaos.run();
    }
}
