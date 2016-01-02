package org.bendra.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
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
	

	public static class IntStreamReverse {
		public static Supplier<IntStream.Builder> supplier() {
			return () -> new IntStream.Builder() {
				LinkedList<Integer> data = new LinkedList<Integer>();
				int outputSize = 0;

				@Override
				public void accept(int t) {
					data.addLast(t);
					outputSize++;
				}

				@Override
				public IntStream build() {
					Iterator<Integer> outputIterator =
							data.descendingIterator();
					Spliterator<Integer> outputSpliterator =
							Spliterators.spliterator(outputIterator,
									outputSize, Spliterator.SIZED
											| Spliterator.ORDERED);
					// cannot run in parallel w/o losing order
					return StreamSupport.stream(outputSpliterator, false)
							.mapToInt(i -> i);
				}

			};
		}

		public static ObjIntConsumer<IntStream.Builder> accumulator() {
			return (r, e) -> r.accept(e);
		}

		public static BiConsumer<IntStream.Builder, IntStream.Builder>
				combiner() {
			return (t, u) -> t.build().forEach(u::accept);
		}

	}
	
	
	public static class StreamReverse {

		public static <T> Supplier<Stream.Builder<T>> supplier() {
			Builder<T> builder = new Builder<T>() {
				LinkedList<T> data = new LinkedList<T>();
				int outputSize = 0;

				@Override
				public void accept(T t) {
					data.addLast(t);
					outputSize++;
				}

				@Override
				public Stream<T> build() {
					Iterator<T> outputIterator = data.descendingIterator();
					Spliterator<T> outputSpliterator =
							Spliterators.spliterator(outputIterator,
									outputSize, Spliterator.SIZED
											| Spliterator.ORDERED);
					// cannot run in parallel w/o losing order
					return StreamSupport.stream(outputSpliterator, false);
				}

			};
			return () -> builder;
		}

		
		public static <T> Collector<T, Stream.Builder<T>, Stream.Builder<T>> collector(){
			return Collector.of(
					supplier(), 
					(t, u) -> t.accept(u), //accumulator
					(t, u) ->{             //combiner
						throw new IllegalArgumentException(
								"Cannot reverse a parallel Stream");
					});
		}

	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static <T> Stream<T> reductions(BinaryOperator<T> op, T init,
			Stream<T> input) {
		Objects.requireNonNull(input);
		Objects.requireNonNull(op);
		return reductions(op, Stream.concat(Stream.of(init), input));
	}

	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static <T> Stream<T>
			reductions(BinaryOperator<T> op, Stream<T> input) {
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
	public static IntStream reductions(IntBinaryOperator op, int init,
			IntStream input) {
		Objects.requireNonNull(input);
		Objects.requireNonNull(op);
		return reductions(op, IntStream.concat(IntStream.of(init), input));
	}

	
	public static class IntReductator implements IntUnaryOperator{
		private IntStreamRef accTot = new IntStreamRef(0);
		private BooleanStreamRef empty = new BooleanStreamRef(true);
		private IntBinaryOperator op;
		
		public IntReductator(IntBinaryOperator op){
			Objects.requireNonNull(op);
			
			this.op = op;
		}

		@Override
		public int applyAsInt(int operand) {
			if (empty.val) {
				accTot.val = operand;
				empty.val = false;
			} else {
				accTot.val = op.applyAsInt(operand, accTot.val);
			}
			return accTot.val;
		}
	};
	
	/**
	 * Produces the intermediate results from a "reduce" operation
	 */
	public static IntStream reductions(IntBinaryOperator op, IntStream input) {
		Objects.requireNonNull(input);

		return input.map(new IntReductator(op));
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
	 * Does not work
	 * @return
	 *
	public static <T> Collector<T, ?, Stream<T>> streamReverser() {
		return Collector.of(() -> {
			Builder<T> builder = Stream.builder();
			return builder.build();
		}, (t, u) -> Stream.concat(t, Stream.of(u)),
				(t, u) -> Stream.concat(u, t));
	}
	*/
	

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
	 * Returns a IntUnaryOperator which can be used to zip another stream with
	 * the stream it is applied to, using the operator provided. .
	 * 
	 * @param stream
	 * @param mapper
	 * @param nullValue
	 * @return
	 */
	public static IntUnaryOperator intZipMapper(IntBinaryOperator mapper,
			int nullValue, IntStream stream) {
		Objects.requireNonNull(mapper);
		OfInt spliterator = Objects.requireNonNull(stream).spliterator();

		PrimitiveIterator.OfInt iterator = Spliterators.iterator(spliterator);

		return i -> mapper.applyAsInt(i,
				iterator.hasNext() ? iterator.nextInt() : nullValue);
	}
	
	/**
	 * Return stream of results from recursive application of function to each
	 * element in the Stream except the last one.  Seed value is the initial
	 * value and will be returned as the first element in the resulting stream
	 * 
	 * @param op
	 * @param seed
	 * @return
	 */
	public static IntUnaryOperator intReductionsMapper(IntBinaryOperator op, int seed){
		Objects.requireNonNull(op);

		IntStreamRef accTot = new IntStreamRef(seed);

		return (i)->{
			int returnVal = accTot.val;
			accTot.val = op.applyAsInt(i, accTot.val);
			return returnVal;
		};
	}
	
	/**
	 * Return stream of results from recursive application of function to each
	 * element in the Stream.  First value in the stream serves as seed value.
	 * 
	 * @param op
	 * @param seed
	 * @return
	 */
	public static IntUnaryOperator intReductionsMapper(IntBinaryOperator op){
		Objects.requireNonNull(op);

		IntStreamRef accTot = new IntStreamRef(0);
		BooleanStreamRef empty = new BooleanStreamRef(true);

		return (i)->{
			accTot.val = empty.val? i : op.applyAsInt(i, accTot.val);
			empty.val = false;
			return accTot.val;
		};
	}

	/**
	 * Return stream of results from recursive application of function to each
	 * element in the Stream except the last one. Seed value is the initial
	 * value and will be returned as the first element in the resulting stream
	 * 
	 * @param op
	 * @param seed
	 * @return
	 */
	public static <T> UnaryOperator<T> reductionsMapper(BinaryOperator<T> op,
			T seed) {
		Objects.requireNonNull(op);

		StreamRef<T> accTotal = new StreamRef<T>(seed);

		return (t) -> {
			T returnVal = accTotal.val;
			accTotal.val = op.apply(t, accTotal.val);
			return returnVal;
		};
	}
	
	/**
	 * Return stream of results from recursive application of function to each
	 * element in the Stream except the last one.  First element in stream
	 * serves as seed value
	 * 
	 * @param op
	 * @param seed
	 * @return
	 */
	public static <T> UnaryOperator<T> reductionsMapper(BinaryOperator<T> op){
		Objects.requireNonNull(op);
		
		StreamRef<T> accTotal = new StreamRef<T>(null);
		BooleanStreamRef empty = new BooleanStreamRef(true);
		
		return (t)->{
			accTotal.val = empty.val ? t : op.apply(t, accTotal.val);
			empty.val = false;
			return accTotal.val;
		};
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
	public static IntStream zip(IntBinaryOperator zipper, IntStream stream1,
			IntStream stream2) {
		return zip(zipper, false, 0, 0, stream1, stream2);
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
	public static IntStream zip(IntBinaryOperator zipper, boolean exhaustAll,
			int exhaustedVal1, int exhaustedVal2, IntStream stream1,
			IntStream stream2) {
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
	 * Zipper type function to allow two streams to be zipped together through
	 * Stream.map() function
	 * 
	 * Note: function is statefull; will not work on parallel streams
	 * 
	 * @param zipStream
	 * @param zipper
	 * @param nullValue
	 * @return
	 */
	public static <T, R> Function<? super T, ? extends R> zipMapper(
			BiFunction<T, T, R> zipper, T nullValue,
			Stream<T> zipStream) {
		return zipMapper(zipStream, zipper, nullValue, zipStream);
	}

	
	/**
	 * Zipper type operator to allow two streams to be zipped together through
	 * Stream.map() function.  Target stream argument allows zipping together
	 * streams of different types, it is not actually muted or otherwise 
	 * operated on
	 * Note: function is statefull; will not work on parallel streams
	 * 
	 * @param zipStream
	 * @param zipper
	 * @param nullValue
	 * @param targetStream
	 * @return
	 */
	public static <T, U, R> Function<? super T, ? extends R> zipMapper(
			Stream<T> targetStream,
			BiFunction<T, U, R> zipper, U nullValue,
			Stream<? extends U> zipStream) {
		Objects.requireNonNull(zipper);
		Spliterator<? extends U> spliterator =
				Objects.requireNonNull(zipStream).spliterator();

		Iterator<? extends U> iterator = Spliterators.iterator(spliterator);
		
		return i ->
			zipper.apply(i, iterator.hasNext() ? iterator.next() : nullValue);
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
		// streams if exhaustall is true, otherwise the smaller; -1 if unknown
		long outputSize = -1;
		if((characteristics  & Spliterator.SIZED) != 0 ){
			long size1 = spliterator1.getExactSizeIfKnown();
			long size2 = spliterator2.getExactSizeIfKnown();
			outputSize = exhaustAll ? Math.max(size1, size2) : Math.min(size1, size2);	
		}

		Spliterator<R> outputSpliterator =
				Spliterators.spliterator(outputIterator, outputSize,
						characteristics);

		// can be run in parallel if both input streams can be run in parallel
		return StreamSupport.stream(outputSpliterator, stream1.isParallel()
				|| stream2.isParallel());
	}
}