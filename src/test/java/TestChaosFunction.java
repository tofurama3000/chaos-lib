import com.tofusoftware.libs.functions.ChaosFunction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TestChaosFunction {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        TestTarget target = new TestTarget();
        ChaosFunction<TestTarget, Boolean> func = new ChaosFunction<>(t -> t.x = true, 0.5);
        assertEquals(0.5, func.getProbability(), 0.001);
        func.run(target);
        assertEquals(true, target.x);
    }

    @Test
    public void testNoFunction() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");

        new ChaosFunction<>(null, 0.75);
    }

    @Test
    public void testZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosFunction<Integer, Integer>(x -> 0, 0.0);
    }

    @Test
    public void testNegativeIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosFunction<Integer,Integer>(x -> 0, -1.0);
    }

    @Test
    public void testInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosFunction<Integer, Integer>(x -> 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosFunction<Integer, Integer>(x -> 0, Double.NaN);
    }
}
