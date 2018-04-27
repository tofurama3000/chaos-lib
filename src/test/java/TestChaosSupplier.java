import com.tofusoftware.libs.functions.ChaosSupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TestChaosSupplier {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        ChaosSupplier<Integer> func = new ChaosSupplier<>(() -> 5, 0.5);
        assertEquals(0.5, func.getProbability(), 0.001);
        assertEquals(Integer.valueOf(5), func.run());
    }

    @Test
    public void testNoFunction() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");

        new ChaosSupplier<>(null, 0.75);
    }

    @Test
    public void testZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosSupplier<Integer>(() -> 0, 0.0);
    }

    @Test
    public void testNegativeIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosSupplier<Integer>(() -> 0, -1.0);
    }

    @Test
    public void testInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosSupplier<Integer>(() -> 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosSupplier<Integer>(() -> 0, Double.NaN);
    }
}
