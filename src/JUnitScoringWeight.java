import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class JUnitScoringWeight {
	
	private Method[] getTestMethods(Class<?> clazz) {
		Method[] allMethods = clazz.getDeclaredMethods();
		
		ArrayList<Method> methodsList = new ArrayList<Method>();
		for (Method m : allMethods) {
			if (m.getAnnotation(Test.class) != null) {
				methodsList.add(m);
			}
		}
		
		return (Method[])methodsList.toArray(new Method[0]);
	}
	
	private Result runTest(final Class<?> testClazz, final String methodName) throws InitializationError {
		BlockJUnit4ClassRunner runner = new BlockJUnit4ClassRunner(testClazz) {
			@Override
			protected List<FrameworkMethod> computeTestMethods() {
				try {
					Method method = testClazz.getMethod(methodName);
					return Arrays.asList(new FrameworkMethod(method));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		};
		Result res = new Result();
		RunNotifier rn = new RunNotifier();
		rn.addListener(res.createListener());
		runner.run(rn);
		return res;
	}
	
	public double calculateScore(Class<?> clazz) {
		double scoreWeight = 0.0;
		
		Method[] testMethods = getTestMethods(clazz);
		
		for (Method m : testMethods) {
			try {
				Result r = runTest(SimpleTest.class, m.getName());
				
				if (r.wasSuccessful()) {
					scoreWeight += m.getAnnotation(ScoringWeight.class).value();
				}
			} catch (InitializationError e) {
				e.printStackTrace();
			}
		}
		
		return scoreWeight;
	}
	
	public static void main(String[] args) {
		JUnitScoringWeight jusw = new JUnitScoringWeight();
		double scoreWeight = jusw.calculateScore(SimpleTest.class);
		System.out.printf("Total Score: %.6f / %.6f%n", scoreWeight, 1.0);
	}
}
