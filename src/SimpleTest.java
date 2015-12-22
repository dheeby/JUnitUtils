import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class SimpleTest {
	@Test(timeout = 100)
	@ScoringWeight(0.1)
	public void test1() {
		assertTrue(true);
	}
	
	@Test(timeout = 100)
	@ScoringWeight(0.9)
	public void test2() {
		assertTrue(false);
	}
}
