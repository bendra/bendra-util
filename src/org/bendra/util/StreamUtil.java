package org.bendra.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.PrimitiveIterator;

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
	 * @param <T>
	 *            the value
	 */
	public static class StreamRef<T> {
		public T val;

		public StreamRef(T aVal) {
			val = aVal;
		}
	}

	/**
	 * This is a convenience class to allow a mutable (non-final) boolean in an
	 * inner class/lambda
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
		Objects.requireNonNull(input);
		Objects.requireNonNull(op);
		return reductions(Stream.concat(Stream.of(init), input), op);
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static <T> Stream<T>
			reductions(Stream<T> input, BinaryOperator<T> op) {
		Objects.requireNonNull(input);
		Objects.requireNonNull(op);
		StreamRef<T> accTot = new StreamRef<T>(null);
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
		Objects.requireNonNull(input);
		Objects.requireNonNull(op);
		return reductions(IntStream.concat(IntStream.of(init), input), op);
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static IntStream reductions(IntStream input, IntBinaryOperator op) {
		Objects.requireNonNull(input);
		IntStreamRef accTot = new IntStreamRef(0);
		BooleanStreamRef empty = new BooleanStreamRef(true);
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
		Objects.requireNonNull(input);
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
		Objects.requireNonNull(input);
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
		Objects.requireNonNull(input);
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
		Objects.requireNonNull(input);
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
		Objects.requireNonNull(input);
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
	public static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		Objects.requireNonNull(input);
		Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
				descendingIterator, Spliterator.ORDERED), false);
	}

	/**
	 * Combine two IntStreams using a zipper function. Will run until one of the
	 * input streams is exhausted.
	 * 
	 * @param stream1
	 * @param stream2
	 * @param zipper
	 * 
	 * @return IntStream
	 */
	public static IntStream zip(IntStream stream1, IntStream stream2,
			IntBinaryOperator zipper) {
		return zip(stream1, stream2, zipper, false, 0, 0);
	}

	/**
	 * Combine two IntStreams using a zipper function. If exhaustAll is false,
	 * will run until one stream is exhausted; if true will run until both input
	 * streams exhausted. If exhaustAll is true, will utilize
	 * exhaustedVal1/exhaustedVal2 for zipper if one of the input streams is
	 * exhausted but the other is not.
	 * 
	 * @param stream1
	 * @param stream2
	 * @param zipper
	 * @param exhaustAll
	 * @param exhaustedVal1
	 * @param exhaustedVal2
	 * @return
	 */
	public static IntStream zip(IntStream stream1, IntStream stream2,
			IntBinaryOperator zipper, boolean exhaustAll, int exhaustedVal1,
			int exhaustedVal2) {
		Objects.requireNonNull(zipper);
		Spliterator.OfInt spliterator1 =
				Objects.requireNonNull(stream1).spliterator();
		Spliterator.OfInt spliterator2 =
				Objects.requireNonNull(stream2).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics =
				spliterator1.characteristics() & spliterator2.characteristics()
						& ~(Spliterator.DISTINCT | Spliterator.SORTED);

		PrimitiveIterator.OfInt iterator1 = Spliterators.iterator(spliterator1);
		PrimitiveIterator.OfInt iterator2 = Spliterators.iterator(spliterator2);

		PrimitiveIterator.OfInt outputIterator = new PrimitiveIterator.OfInt() {
			@Override
			public boolean hasNext() {
				if (exhaustAll) {
					return iterator1.hasNext() || iterator2.hasNext();
				} else {
					return iterator1.hasNext() && iterator2.hasNext();
				}
			}

			@Override
			public Integer next() {
				return nextInt();
			}

			@Override
			public int nextInt() {
				if (exhaustAll) {
					return zipper.applyAsInt(
							iterator1.hasNext() ? iterator1.nextInt()
									: exhaustedVal1,
							iterator2.hasNext() ? iterator2.nextInt()
									: exhaustedVal2);
				} else {
					return zipper.applyAsInt(iterator1.nextInt(),
							iterator2.nextInt());
				}
			}
		};
		// Our output stream will be the size of the larger of the two input
		// streams
		long outputSize =
				((characteristics & Spliterator.SIZED) != 0) ? Math.max(
						spliterator1.getExactSizeIfKnown(),
						spliterator2.getExactSizeIfKnown()) : -1;

		Spliterator.OfInt outputSpliterator =
				Spliterators.spliterator(outputIterator, outputSize,
						characteristics);

		// can be run in parallel if both input streams can be run in parallel
		return StreamSupport.intStream(outputSpliterator, stream1.isParallel()
				&& stream2.isParallel());
	}

	/**
	 * Combine two streams using a zipper function. Will run until one of the
	 * input streams is exhausted.
	 * 
	 * @param stream1
	 * @param stream2
	 * @param zipper
	 * 
	 * @return
	 */
	public static <T, U, R> Stream<R> zip(Stream<? extends T> stream1,
			Stream<? extends U> stream2,
			BiFunction<? super T, ? super U, ? extends R> zipper) {
		return zip(stream1, stream2, zipper, false, null, null);
	}

	/**
	 * Combine two streams using a zipper function. If exhaustAll is false, will
	 * run until one stream is exhausted; if true will run until both input
	 * streams exhausted. If exhaustAll is true, will utilize
	 * exhaustedVal1/exhaustedVal2 for zipper if one of the input streams is
	 * exhausted but the other is not.
	 * 
	 * @param stream1
	 * @param stream2
	 * @param zipper
	 * @param exhaustAll
	 * @param exhaustedVal1
	 * @param exhaustedVal2
	 * @return
	 */
	public static <T, U, R> Stream<R> zip(Stream<? extends T> stream1,
			Stream<? extends U> stream2,
			BiFunction<? super T, ? super U, ? extends R> zipper,
			boolean exhaustAll, T exhaustedVal1, U exhaustedVal2) {
		Objects.requireNonNull(zipper);
		@SuppressWarnings("unchecked")
		Spliterator<T> spliterator1 =
				(Spliterator<T>) Objects.requireNonNull(stream1).spliterator();
		@SuppressWarnings("unchecked")
		Spliterator<U> spliterator2 =
				(Spliterator<U>) Objects.requireNonNull(stream2).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics =
				spliterator1.characteristics() & spliterator2.characteristics()
						& ~(Spliterator.DISTINCT | Spliterator.SORTED);

		Iterator<T> iterator1 = Spliterators.iterator(spliterator1);
		Iterator<U> iterator2 = Spliterators.iterator(spliterator2);
		Iterator<R> outputIterator = new Iterator<R>() {
			@Override
			public boolean hasNext() {
				if (exhaustAll) {
					return iterator1.hasNext() || iterator2.hasNext();
				} else {
					return iterator1.hasNext() && iterator2.hasNext();
				}
			}

			@Override
			public R next() {
				if (exhaustAll) {
					return zipper.apply(iterator1.hasNext() ? iterator1.next()
							: exhaustedVal1,
							iterator2.hasNext() ? iterator2.next()
									: exhaustedVal2);
				} else {
					return zipper.apply(iterator1.next(), iterator2.next());
				}
			}
		};

		// Our output stream will be the size of the larger of the two input
		// streams
		long outputSize =
				((characteristics & Spliterator.SIZED) != 0) ? Math.max(
						spliterator1.getExactSizeIfKnown(),
						spliterator2.getExactSizeIfKnown()) : -1;

		Spliterator<R> outputSpliterator =
				Spliterators.spliterator(outputIterator, outputSize,
						characteristics);

		// can be run in parallel if both input streams can be run in parallel
		return StreamSupport.stream(outputSpliterator, stream1.isParallel()
				|| stream2.isParallel());
	}
}