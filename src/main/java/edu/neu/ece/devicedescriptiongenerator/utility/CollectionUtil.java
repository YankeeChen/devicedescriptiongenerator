package edu.neu.ece.devicedescriptiongenerator.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * Collection utility class that contains methods on collection operations.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public final class CollectionUtil {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private CollectionUtil() {
	}

	/**
	 * Get a random element with T type from a list
	 * 
	 * @param typeList
	 *            A list contains elements of T type
	 * @param random
	 *            Used to generate a stream of pseudorandom numbers
	 * @return A random element from typeList of T type
	 */
	public static <T> T getARandomElementFromList(List<T> typeList, Random random) {
		int index = random.nextInt(typeList.size());
		T t = (T) typeList.get(index);
		return t;
	}

	/**
	 * Get a random element with T type from a set
	 * 
	 * @param typeSet
	 *            A set of type T
	 * @param random
	 *            Used to generate a stream of pseudorandom numbers
	 * @return A T type object
	 */
	public static <T> T getARandomElementFromSet(Set<T> typeSet, Random random) {
		int index = random.nextInt(typeSet.size());
		@SuppressWarnings("unchecked")
		T t = (T) typeSet.toArray()[index];
		return t;
	}

	/**
	 * Generate all subset of a set of entries
	 * 
	 * @param set
	 *            A set
	 * @return A list containing all subset of set
	 * @deprecated
	 */
	public <T1, T2> List<Set<Entry<T1, T2>>> getAllSubSetsOfASetOfEntries(Set<Entry<T1, T2>> set) {
		int eleNumber = set.size();
		if (eleNumber == 0)
			return new ArrayList<>();
		int subsetNumber = (int) Math.pow(2, eleNumber);
		List<Set<Entry<T1, T2>>> results = new ArrayList<>(subsetNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + "; eleNumber
		// is: " + eleNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		for (int i = 0; i < subsetNumber; i++) {
			results.add(new HashSet<Entry<T1, T2>>());
		}
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		// @SuppressWarnings("unchecked")
		// Set<Entry<T1, Set<T2>>>[] resultsInArray = results.toArray(new
		// HashSet[subsetNumber]);
		@SuppressWarnings("unchecked")
		Entry<T1, T2>[] setInArray = set.toArray(new Entry[eleNumber]);
		for (int i = 0; i < eleNumber; i++)
			for (int j = 0; j < subsetNumber; j++)
				if ((j >> i & 1) == 1) {
					// System.out.println(resultsInArray[j].toString());
					results.get(j).add(setInArray[i]);
				}

		// @SuppressWarnings("unchecked")
		// Set<Set<Entry<T1, Set<T2>>>> f = new HashSet<>((Collection<? extends
		// Set<Entry<T1, Set<T2>>>>) Arrays.asList(results));
		return results;

	}

	/**
	 * Generate all subset of a set
	 * 
	 * @param set
	 *            A set
	 * @return A list containing all subset of set
	 */
	public static <T> ArrayList<Set<T>> getAllSubSetsOfASet(Set<T> set) {
		int eleNumber = set.size();
		if (eleNumber == 0)
			return new ArrayList<>();
		int subsetNumber = (int) Math.pow(2, eleNumber);
		ArrayList<Set<T>> results = new ArrayList<>(subsetNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + "; eleNumber
		// is: " + eleNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		for (int i = 0; i < subsetNumber; i++) {
			results.add(new HashSet<T>());
		}
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		// @SuppressWarnings("unchecked")
		// Set<Entry<T1, Set<T2>>>[] resultsInArray = results.toArray(new
		// HashSet[subsetNumber]);
		@SuppressWarnings("unchecked")
		T[] setInArray = (T[]) set.toArray(new Object[eleNumber]);
		for (int i = 0; i < eleNumber; i++)
			for (int j = 0; j < subsetNumber; j++)
				if ((j >> i & 1) == 1) {
					// System.out.println(resultsInArray[j].toString());
					results.get(j).add(setInArray[i]);
				}

		// @SuppressWarnings("unchecked")
		// Set<Set<Entry<T1, Set<T2>>>> f = new HashSet<>((Collection<? extends
		// Set<Entry<T1, Set<T2>>>>) Arrays.asList(results));
		return results;

	}
}
