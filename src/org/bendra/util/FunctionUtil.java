package org.bendra.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class FunctionUtil {

	public static <T, R> Function<T, R> memoize(Function<T, R> fn) {
		Map<T, R> map = new ConcurrentHashMap<T, R>();
		return (t) -> map.computeIfAbsent(t, fn);
	}

	public static <T> Predicate<T> memoizePred(Predicate<T> pred) {
		Function<T, Boolean> memo = memoize((t) -> pred.test(t));
		return (t) -> memo.apply(t);
	}
	
	public static <T, U, R> BiFunction<T, U, R> memoizeBifn(BiFunction<T, U, R> bFn) {
		@SuppressWarnings("unchecked")
		Function<List<? extends Object>, R> twoArgsListMemoFn = memoize(list -> bFn.apply(
				(T) list.get(0), (U) list.get(1)));

		return (t, u) -> {
			List<? extends Object> list = Arrays.asList(t, u);
			return twoArgsListMemoFn.apply(list);
		};
	}

}
