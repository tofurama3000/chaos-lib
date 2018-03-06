import java.lang.IllegalArgumentException;
import com.tofusoftware.libs.Chaos;
import com.tofusoftware.libs.ChaosFunction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

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
    public void testNoFunction() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Need to specify at least one function");
        Chaos<TestTarget> chaos = new Chaos<>();
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
    public void testNoRangeFunctions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Need to specify probabilities for functions");

        Chaos<TestTarget> chaos = new Chaos<>(
                new ChaosFunction<>(x -> x.y += 1, 0),
                new ChaosFunction<>(x -> x.z += 1, 0)
        );
    }
}
