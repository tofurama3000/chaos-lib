import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

import com.tofusoftware.libs.functions.ChaosBiConsumer;

public class TestChaosBiConsumer {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        TestTarget target = new TestTarget();
        ChaosBiConsumer<TestTarget, Boolean> func = new ChaosBiConsumer<>((t, b) -> t.x = b, 0.5);
        assertEquals(0.5, func.getProbability(), 0.001);
        func.run(target, true);
        assertEquals(true, target.x);
    }

    @Test
    public void testNoBiFunction() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");

        new ChaosBiConsumer<>(null, 0.75);
    }

    @Test
    public void testZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosBiConsumer<Integer, Integer>((x,y) -> {}, 0.0);
    }

    @Test
    public void testNegativeIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosBiConsumer<Integer, Integer>((x,y) -> {}, -1.0);
    }

    @Test
    public void testInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosBiConsumer<Integer, Integer>((x,y) -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosBiConsumer<Integer, Integer>((x,y) -> {}, Double.NaN);
    }
}
