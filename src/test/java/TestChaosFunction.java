import com.tofusoftware.libs.ChaosFunction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TestChaosFunction {
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void testInit() {
        TestTarget target = new TestTarget();
        ChaosFunction<TestTarget> func = new ChaosFunction<TestTarget>(t -> t.x = true, 0.5);
        assertEquals(0.5, func.getProbability(), 0.001);
        func.run(target);
        assertEquals(true, target.x);
    }

    @Test
    public void testNoFunction() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Need to specify a function");

        ChaosFunction<Integer> func = new ChaosFunction<>(null, 0.75);
    }
}
