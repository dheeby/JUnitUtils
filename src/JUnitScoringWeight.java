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

/**
 * The JUnitScoringWeight class was made for use with the Vocareum cloud 
 * platform to allow scoring weights to be applied to JUnit test cases. It has 
 * been adapted from the ScoringWeight for JUnit tests which are used on 
 * Web-CAT (web-cat.org) for automated Java programming assignment grading.
 * 
 * @author Neil Allison (neiljallison@gmail.com)
 */
public class JUnitScoringWeight {

	/**
	 * Gets all of the methods from the specified Class which have been marked 
	 * with the Test annotation. This is used to filter out the methods from a 
	 * test class that are not unit tests such as setUp, tearDown, and any 
	 * other utility methods from the test class.
	 * 
	 * @param clazz the Class to filter the test methods from
	 * @return An array containing the Methods marked with the Test annotation
	 */
	private Method[] getTestMethods(final Class<?> clazz) {
		Method[] allMethods = clazz.getDeclaredMethods();

		ArrayList<Method> methodsList = new ArrayList<Method>();
		for (Method m : allMethods) {
			// Add methods which are marked as JUnit tests
			if (m.getAnnotation(Test.class) != null) {
				methodsList.add(m);
			}
		}

		return (Method[])methodsList.toArray(new Method[0]);
	}

	/**
	 * Runs a single method from a given class. This method was needed to 
	 * prevent the default JUnit4 class runner from executing all test methods
	 * at once by creating a custom class runner to execute the method given 
	 * as an argument. This method isn't necessarily specific to running 
	 * methods with the Test annotation so it should work with any method, but 
	 * for this class it will specifically run a JUnit test method. 
	 * 
	 * @param testClazz the Class containing the test method to execute
	 * @param methodName the name of the method to execute
	 * @return the Result from running the specified test method
	 * @throws InitializationError
	 */
	private Result runTest(final Class<?> testClazz, final String methodName) 
			throws InitializationError {
		/* 
		 * BlockJUnit4ClassRunner is now the default JUnit class runner which 
		 * allows you to create a custom Runner when only minor modifications
		 * from the default are needed. In this case, only one method is
		 * overridden, computeTestMethods(), to allow us to run a single unit 
		 * test at a time. 
		 */
		BlockJUnit4ClassRunner runner = new BlockJUnit4ClassRunner(testClazz) {
			@Override
			protected List<FrameworkMethod> computeTestMethods() {
				try {
					Method method = testClazz.getMethod(methodName);
					return Arrays.asList(new FrameworkMethod(method));
				} catch (Exception e) {
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

	/**
	 * Calculates the weighted score of running all the JUnit test cases in 
	 * the given test class that are marked with both the Test annotation and 
	 * ScoringWeight annotation. The weighted score is the ratio of test cases 
	 * passed to all test cases that were executed with their corresponding 
	 * scoring weights applied.
	 * 
	 * @param clazz the Class containing the JUnit tests to execute
	 * @return the weighted ratio of test cases passed to test cases executed, 
	 * 		else NaN if all the scoring weight values were 0
	 */
	public double calculateScoreWeight(final Class<?> clazz) {
		double scoreTotal = 0.0;
		double scoreMax = 0.0;

		Method[] testMethods = getTestMethods(clazz);
		for (Method m : testMethods) {
			try {
				/*
				 * Only test cases marked with the ScoringWeight annotation 
				 * are considered and as such, are the only unit tests that 
				 * are executed.
				 */
				if (m.getAnnotation(ScoringWeight.class) != null) {
					Result r = runTest(SimpleTest.class, m.getName());
					
					if (r.wasSuccessful()) {
						scoreTotal += m.getAnnotation(ScoringWeight.class).value();
					}
					
					scoreMax += m.getAnnotation(ScoringWeight.class).value();
				}
			} catch (InitializationError e) {
				e.printStackTrace();
			}
		}

		return scoreTotal / scoreMax;
	}

	public static void main(String[] args) {
		JUnitScoringWeight jusw = new JUnitScoringWeight();
		double scoreWeight = jusw.calculateScoreWeight(SimpleTest.class);
		System.out.printf("Percentage Score: %.3f%%%n", scoreWeight * 100);
	}
}
