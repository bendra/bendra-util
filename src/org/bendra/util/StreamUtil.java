package org.bendra.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Set of utility methods for Stream operations
 * 
 * @author Ben Drasin
 */
public class StreamUtil {

	/**
	 * This is a convenience class to allow a mutable (non-final) reference in
	 * an inner class/lambda
	 * 
	 * @param <T> the value
	 */
	public static class StreamRef<T> {
		public T val;

		public StreamRef(T aVal) {
			val = aVal;
		}
	}

	/**
	 * This is a convenience class to allow a mutable (non-final) boolean in
	 * an inner class/lambda
	 * 
	 * @param boolean the value
	 */	
	public static class BooleanStreamRef {
		public boolean val;

		public BooleanStreamRef(boolean aVal) {
			val = aVal;
		}
	}

	public static class IntStreamRef {
		public int val;

		public IntStreamRef(int aVal) {
			val = aVal;
		}
	}

	public static class LongStreamRef {
		public int val;

		public LongStreamRef(int aVal) {
			val = aVal;
		}
	}

	public static class DoubleStreamRef {
		public double val;

		public DoubleStreamRef(int aVal) {
			val = aVal;
		}
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static <T> Stream<T> reductions(Stream<T> input, T init,
			BinaryOperator<T> op) {
		return reductions(Stream.concat(Stream.of(init), input), op);
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static <T> Stream<T> reductions(Stream<T> input, BinaryOperator<T> op) {
		final StreamRef<T> accTot = new StreamRef<T>(null);
		return input.map(i -> {
			if (accTot.val == null) {
				accTot.val = i;
			} else {
				accTot.val = op.apply(i, accTot.val);
			}
			return accTot.val;
		});
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static IntStream reductions(IntStream input, int init,
			IntBinaryOperator op) {
		return reductions(IntStream.concat(IntStream.of(init), input), op);
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static IntStream reductions(IntStream input, IntBinaryOperator op) {
		final IntStreamRef accTot = new IntStreamRef(0);
		final BooleanStreamRef empty = new BooleanStreamRef(true);
		return input.map(i -> {
			if (empty.val) {
				accTot.val = i;
				empty.val = false;
			} else {
				accTot.val = op.applyAsInt(i, accTot.val);
			}
			return accTot.val;
		});
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	public static <T> Stream<T> streamInReverse(T[] input) {
		return IntStream.range(1, input.length + 1).mapToObj(
				i -> input[input.length - i]);
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	public static IntStream streamInReverse(int[] input) {
		return IntStream.range(1, input.length + 1).map(
				i -> input[input.length - i]);
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	public static LongStream streamInReverse(long[] input) {
		return IntStream.range(1, input.length + 1).mapToLong(
				i -> input[input.length - i]);
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	public static DoubleStream streamInReverse(double[] input) {
		return IntStream.range(1, input.length + 1).mapToDouble(
				i -> input[input.length - i]);
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	public static <T> Stream<T> streamInReverse(List<T> input) {
		if (input instanceof LinkedList<?>) {
			return streamInReverse((LinkedList<T>) input);
		}
		return IntStream.range(1, input.size() + 1).mapToObj(
				i -> input.get(input.size() - 1));
	}

	/**
	 * Stream elements in reverse-index order
	 * 
	 * @param input
	 * @return a stream of the elements in reverse
	 */
	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		final Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
				descendingIterator, Spliterator.ORDERED), false);
	}
}