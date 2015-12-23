package org.bendra.java8test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bendra.util.FunctionUtil;
import org.bendra.util.StreamUtil;

public class ArrayProduce {
	// accept an int array, return the an array of
	// the same length, each element i equaling the
	// product of all elements in the input array
	// except element at index i
	// Do it without using division operator
	public static int[] simpleSolution(int[] input) {
		if (input.length == 0)
			return new int[] {};
		int productSoFar = 1;
		int[] output = new int[input.length];
		int[] ascending = new int[input.length];
		int[] descending = new int[input.length];
		// ascending partial products
		int index = 0;
		do {
			ascending[index] = productSoFar;
			productSoFar = productSoFar * input[index];
			index++;
		} while (index < input.length);

		// descending partial products
		index = input.length - 1;
		productSoFar = 1;
		do {
			descending[index] = productSoFar;
			productSoFar = productSoFar * input[index];
			index--;
		} while (index >= 0);

		for (int i = 0; i < output.length; i++) {
			output[i] = ascending[i] * descending[i];
		}
		return output;
	}

	public static int[] functionalSolution(int[] input) {
		return StreamUtil
				.zip(StreamUtil.reductions(Arrays.stream(input), 1, (i, j) -> i
						* j),
						StreamUtil.streamInReverse(
								StreamUtil.reductions(
										StreamUtil.streamInReverse(input), 1,
										(i, j) -> i * j).toArray()).skip(1),
						(i, j) -> i * j).toArray();
	}
}
