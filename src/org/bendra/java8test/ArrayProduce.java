package org.bendra.java8test;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.bendra.util.StreamUtil;
import org.bendra.util.StreamUtil.StreamReverse;

import static org.bendra.util.StreamUtil.*;
import static java.util.Arrays.stream;


/**
 * Problem:
 * accept an int array, return the an array of the same length, each element i
 * equaling the product of all elements in the input array except element at
 * index i Do it without using division operator
 * 
 * @author Owner
 *
 */
public class ArrayProduce {
	/**
	 * Imperative solution using classic (non-functional) java
	 * 
	 * @param input
	 * @return
	 */
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

		//now generate the output by multiplying the data
		for (int i = 0; i < output.length; i++) {
			output[i] = ascending[i] * descending[i];
		}
		return output;
	}

	/**
	 * Functional solution 1: using static methods
	 * 
	 * @param input
	 * @return
	 */
	public static int[] functionalSolution(int[] input) {
		return StreamUtil
				.zip((i, j) -> i * j,
						StreamUtil.reductions((i, j) -> i * j, 1,
								Arrays.stream(input)),
						StreamUtil.streamInReverse(
								StreamUtil.reductions((i, j) -> i * j, 1,
										StreamUtil.streamInReverse(input))
										.toArray()).skip(1)).toArray();
	}

	/**
	 * Functional solution 2: add some local variables to make it more sane
	 * 
	 * @param input
	 * @return
	 */
	public static int[] functionalSolution2(int[] input) {
		IntStream ascending = Arrays.stream(input);
		IntStream descending = StreamUtil.streamInReverse(input);

		IntStream reductions1 =
				StreamUtil.reductions((i, j) -> i * j, 1, ascending);
		IntStream reductions2 =
				StreamUtil.streamInReverse(StreamUtil.reductions(
						(i, j) -> i * j, 1, descending).toArray());

		return StreamUtil
				.zip((i, j) -> i * j, reductions1, reductions2.skip(1))
				.toArray();
	}
	
	
	/**
	 * Functional solution 3: zip and reductions mapper
	 * 
	 * @param input
	 * @return
	 */
	public static int[] functionalSolution3(int[] input) {
		IntStream ascending = Arrays.stream(input);
		IntStream descending = StreamUtil.streamInReverse(input);

		IntStream reductions2 =
				StreamUtil.streamInReverse(
						descending.map(StreamUtil.intReductionsMapper( (i, j) -> i * j, 1))
						.toArray());
		
		return ascending
				.map(StreamUtil.intReductionsMapper((i,j)->i*j, 1))
				.map( StreamUtil.intZipMapper((i, j)-> i *j, 0, reductions2.skip(1)))
				.toArray();		
	}
	
	/*
	 * What I'd really like to do:
	 * 
	 *
	 public static int[] functionalSolution(int[] input){
	    Arrays.stream(input)
	         .reductions(1, (i, j) -> i * j);
	         .zipWith(
	              Arrays.streamInReverse(input)
	                   .reductions(1, (i, j) -> i * j)
	                   .reverse()
	                   .skip(1)
	              , (i, j) -> i * j)
	         .toArray();
	 }	 
	 */
	
	/**
	 * Full method chaining, Zip and reductions mapper, IntStreamReverse
	 * 
	 * @param input
	 * @return
	 */
	public static int[] functionalChainedSolution(int[] input) {

		return stream(input)
				.map(intReductionsMapper((i, j) -> i * j, 1))
				.map(intZipMapper( (i, j) -> i * j, 0,
						streamInReverse(input)
								.map(intReductionsMapper((i, j) -> i
										* j, 1))
								.collect(IntStreamReverse.supplier(),
										IntStreamReverse.accumulator(),
										IntStreamReverse.combiner()).build()))
				.toArray();
	}

	/**
	 * Full method chaining, Zip and reductions mapper, IntStreamReverse
	 * 
	 * @param input
	 * @return
	 */
	public static Integer[] functionalChainedSolution(Integer[] input) {
		
		return stream(input)
				.map(reductionsMapper((i, j) -> i * j, 1))
				.map(zipMapper((i, j) -> i * j, 0,
						streamInReverse(input)
								.map(reductionsMapper((i, j) -> i * j, 1))
								.collect(StreamReverse.collector()).build()))
				.toArray(Integer[]::new);
	}
}