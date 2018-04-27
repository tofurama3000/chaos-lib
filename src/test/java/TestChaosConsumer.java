import com.tofusoftware.libs.functions.ChaosConsumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TestChaosConsumer {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInit() {
        TestTarget target = new TestTarget();
        ChaosConsumer<TestTarget> func = new ChaosConsumer<>(t -> t.x = true, 0.5);
        assertEquals(0.5, func.getProbability(), 0.001);
        func.run(target);
        assertEquals(true, target.x);
    }

    @Test
    public void testNoFunction() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Need to specify a function!");

        new ChaosConsumer<>(null, 0.75);
    }

    @Test
    public void testZeroIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosConsumer<Integer>(x -> {}, 0.0);
    }

    @Test
    public void testNegativeIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability must be greater than 0!");
        new ChaosConsumer<Integer>(x -> {}, -1.0);
    }

    @Test
    public void testInfinityIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosConsumer<Integer>(x -> {}, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNaNIsInvalidProbability() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probability cannot be Infinity or NaN!");
        new ChaosConsumer<Integer>(x -> {}, Double.NaN);
    }
}
