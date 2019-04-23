package edu.neu.ece.devicedescriptiongenerator.utility;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

/**
 * Math utility that contains math function implementations.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public final class MathUtil {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private MathUtil() {
	}

	/**
	 * Get a random integer confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random integer value.
	 * @param max
	 *            The bound (exclusive) of each random integer value.
	 * @param ran
	 *            Random object.
	 * @return A random integer with the given integer range.
	 * @throws Exception
	 */
	public static int getRandomIntegerInRange(int min, int max, Random ran) throws Exception {
		/*
		 * Not correct, which may cause overflow. return ran.nextInt((max - min) + 1) +
		 * min;
		 */
		// return ThreadLocalRandom.current().nextInt(min, max + 1);
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.ints(min, max).findFirst().getAsInt();
	}

	/**
	 * Get a random long confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random long value.
	 * @param max
	 *            The bound (exclusive) of each random long value.
	 * @param ran
	 *            Random object.
	 * @return A random long with the given long range.
	 * @throws Exception
	 */
	public static long getRandomLongInRange(long min, long max, Random ran) throws Exception {
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.longs(min, max).findFirst().getAsLong();
	}

	/**
	 * Get a random double confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random double value.
	 * @param max
	 *            The bound (exclusive) of each random double value.
	 * @param ran
	 *            Random object.
	 * @return A random double with the given double range.
	 * @throws Exception
	 */
	public static double getRandomDoubleInRange(double min, double max, Random ran) throws Exception {
		// return ran.nextDouble()*((max - min) + 1) + min;
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.doubles(min, max).findFirst().getAsDouble();
	}

	/**
	 * Get a random boolean value with equal true/false probability.
	 * 
	 * @param ran
	 *            Random object
	 * @return A random boolean value.
	 */
	public static boolean getRandomBoolean(Random ran) {
		if (ran.nextDouble() < 0.5)
			return true;
		else
			return false;
	}

	/**
	 * Get a random float confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random float value.
	 * @param max
	 *            The bound (exclusive) of each random float value.
	 * @param ran
	 *            Random object.
	 * @return A random float with the given float range.
	 * @throws Exception
	 */
	public static float getRandomFloatInRange(float min, float max, Random ran) throws Exception {
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.nextFloat() * ((max - min)) + min;
	}

	/**
	 * Get a random string whose length confirms to the given origin (inclusive) and
	 * bound (exclusive).
	 * 
	 * @param minStringLength
	 *            The minimum string length (inclusive).
	 * @param maxStringLength
	 *            The maximum string length (inclusive).
	 * @param ran
	 *            Random object.
	 * @return A random String with the given string length range.
	 */
	public static String getRandomString(int minStringLength, int maxStringLength, Random ran) {
		final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		int stringLength;
		try {
			stringLength = getRandomIntegerInRange(minStringLength, maxStringLength + 1, ran);
			// length of the random string.
			while (salt.length() < stringLength) {
				int index = (int) (ran.nextFloat() * SALTCHARS.length());
				salt.append(SALTCHARS.charAt(index));
			}
			String saltStr = salt.toString();
			return saltStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a random decimal confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random decimal value.
	 * @param max
	 *            The bound (exclusive) of each random decimal value.
	 * @param ran
	 *            Random object.
	 * @return A random decimal with the given decimal range.
	 */
	public static String getRandomDecimalInString(double min, double max, Random ran) {
		DecimalFormat df = new DecimalFormat("#.##");
		try {
			String result = df.format(getRandomDoubleInRange(min, max, ran));
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calculate standard deviation of normalized distribution of a collection of
	 * integers.
	 * 
	 * @param numbers
	 *            A collection of integers.
	 * @return Stand deviation of normalized distribution of a collection of
	 *         integers.
	 * @throws Exception
	 *             If the collection is null.
	 */
	public static double calculateStandardDeviationOfNormalizedDistribution(Collection<Integer> numbers)
			throws Exception {
		if (numbers == null)
			throw new Exception();
		if (numbers.isEmpty())
			return 0;
		double results = 0;
		int countSum = 0;
		for (Integer number : numbers)
			countSum += number.intValue();
		if (countSum == 0)
			return 0;
		double alpha = 1 / (double) numbers.size();
		double temp = 0;
		for (Integer number : numbers) {
			temp += Math.pow(number.intValue() / (double) countSum - alpha, 2);
		}
		results = Math.sqrt(temp / numbers.size());
		// System.out.println(results);
		return results;
	}

	/**
	 * Calculate space coverage metric of a collection of integers. The metric is
	 * calculated as the ratio of non-zero integers over the collection of integers.
	 * 
	 * @param numbers
	 *            A collection of integers.
	 * @return Space coverage metric.
	 * @throws Exception
	 *             If the collection is null.
	 */
	public static double calculateSpaceCoverage(Collection<Integer> numbers) throws Exception {
		if (numbers == null)
			throw new Exception();
		if (numbers.isEmpty())
			return 1;
		double results = 0;
		int count = 0;
		for (Integer number : numbers)
			if (number.intValue() != 0)
				count++;
		results = (double) count / numbers.size();
		// System.out.println(results);
		return results;
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * Random ran = new Random();
	 * 
	 * for (int count = 0; count < 200; count++) {
	 * //System.out.printf("A random integer is: " +
	 * getRandomIntegerInRange(Integer.MIN_VALUE, Integer.MAX_VALUE - 1, ran) +
	 * "\n"); //System.out.printf("A random double is: " +
	 * getRandomDoubleInRange(-100, 100, ran) + "\n");
	 * //System.out.printf("A random boolean is: " + getRandomBoolean(ran) + "\n");
	 * //System.out.printf("A random float is: " + getRandomFloatInRange(-100, 100,
	 * ran) + "\n"); //System.out.printf("A random string is: " + getRandomString(4,
	 * 10, ran) + "\n"); //System.out.printf("A random decimal is: " +
	 * getRandomDecimalInString(-100, 100, ran) + "\n"); }
	 * 
	 * String regex = "[ab]{4,6}c"; Generex generator = new Generex(regex); String
	 * result = generator.random(); System.out.println(result); //assert
	 * result.matches(regex); }
	 */
}
