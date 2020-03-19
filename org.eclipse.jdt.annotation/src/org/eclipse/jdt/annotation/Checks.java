/*******************************************************************************
 * Copyright (c) 2016, 2019 Stephan Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.annotation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility functions intended for use with the null annotations defined in this package.
 *
 * <p>
 * For maximum generality, all type parameters of methods in this class are not constraint
 * to either non-null nor nullable. Users of these methods can freely choose these types.
 * </p>
 * <p>
 * Methods in this class come in three groups: Assertions, Requirements, as well as Queries, conversions and computations.
 * </p>
 *
 * <h2>Assertions</h2>
 * <p>
 * All methods in this group start with the prefix "assert", and work similar to the <code>assert</code> keyword.
 * </p>
 * <ul>
 * <li>One or more values are checked for <code>null</code>.</li>
 * <li>If any value is <code>null</code>, an exception is thrown.</li>
 * <li>The method has no other effect (except for potential side effects in an Iterator).</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 * <p>
 * All methods in this group start with the prefix "require". They encode domain knowledge of the developer
 * and make this knowledge visible to static null analysis.
 * </p>
 * <ul>
 * <li>One or more values are checked for <code>null</code>.</li>
 * <li>If any value is <code>null</code>, an exception is thrown.</li>
 * <li>Otherwise a guaranteed non-null value is returned.</li>
 * </ul>
 * <p>For Strings and Collections additional checks for empty may be included.</p>
 * <p>
 * Typical usage includes the case of interfacing with legacy API, that is not specified using
 * null annotations, but where corresponding guarantees are given informally:
 * </p>
 * <pre>
 * interface LegacyThing {
 * 	{@literal /}** @return the name of this thing, never &lt;code&gt;null&lt;/code&gt;. *{@literal /}
 * 	String getName();
 * }
 * ...
 * public void process(@NonNull LegacyThing t) {
 *	{@literal @}NonNull String name = Checks.requireNonNull(t.getName(), "LegacyThing is supposed to have a name");
 * 	...
 * }
 * </pre>
 *
 * <h2>Queries, conversions and computations</h2>
 * <p>
 * A commonality among methods in this group is the null-safety that can be verified by static null analysis,
 * hence in a fully analyzed program none of these methods should throw an exception.
 * </p>
 * <dl>
 * <dt>boolean queries</dt>
 * <dd>Methods {@link #isNull(Object)}, {@link #isAnyNull(Object...)}, {@link #containsNull(Iterable)} simply
 * answer whether a null value could be found in the argument(s).</dd>
 * <dt>conversions</dt>
 * <dd>Methods {@link #nonNullElse(Object, Object)}, {@link #nonNullElseGet(Object, Supplier)}, {@link #asNullable(Optional)}
 * and the {@link #unboxElse(Boolean, boolean)} family of methods provide null-safe conversions,
 * which can be checked by static analysis.</dd>
 * <dt>computations</dt>
 * <dd>Methods {@link #ifNonNull(Object, Consumer)}, {@link #applyIfNonNull(Object, Function)},
 * {@link #applyIfNonNullElse(Object, Function, Object)} and {@link #applyIfNonNullElseGet(Object, Function, Supplier)}
 * feed unsafe values into a given functional expression in a null-safe way.</dd>
 * </dl>
 *
 * @since 2.1
 */
public class Checks {

	/**
	 * Checks whether any of the provided values is <code>null</code>.
	 * @param values arbitrary values to be checked
	 * @throws NullPointerException if a <code>null</code> was found among values.
	 */
	@SafeVarargs
	public static <T> void assertNonNull(T @NonNull... values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null)
				throw new NullPointerException("Value in position "+i+" must not be null");  //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/**
	 * Checks whether any of the provided values is <code>null</code>.
	 * @param message explanatory message to be used when throwing {@link NullPointerException}.
	 * @param values arbitrary values to be checked
	 * @throws NullPointerException if a <code>null</code> was found among values.
	 */
	@SafeVarargs
	public static <T> void assertNonNullWithMessage(String message, T @NonNull... values) {
		for (T v : values)
			if (v == null)
				throw new NullPointerException(message);
	}

	/**
	 * Checks whether any element in the provided values is <code>null</code>.
	 * @param values an Iterable of arbitrary values to be checked
	 * @throws NullPointerException if a <code>null</code> was found among the elements in values.
	 */
	public static <T> void assertNonNullElements(@NonNull Iterable<T> values) {
		int count = 0;
		for (T v : values) {
			if (v == null)
				throw new NullPointerException("Value in position "+count+" must not be null"); //$NON-NLS-1$ //$NON-NLS-2$
			count++;
		}
	}

	/**
	 * Checks whether any of the provided values is <code>null</code>.
	 * @param values an iterable of arbitrary values to be checked
	 * @param message explanatory message to be used when throwing {@link NullPointerException}.
	 * @throws NullPointerException if a <code>null</code> was found among the elements in values.
	 */
	public static <T> void assertNonNullElements(@NonNull Iterable<T> values, String message) {
		for (T v : values)
			if (v == null)
				throw new NullPointerException(message);
	}

	/**
	 * Answer the given value as a non-null value.
	 * Throws {@link NullPointerException} if the given value was <code>null</code>.
	 *
	 * @param value an arbitrary value, maybe <code>null</code>.
	 * @return the original passed value, guaranteed not to be <code>null</code>.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>.
	 */
	public static <T> @NonNull T requireNonNull(@Nullable T value) {
		if (value == null)
			throw new NullPointerException();
		return value;
	}

	/**
	 * Answer the given value as a non-null value.
	 * Throws {@link NullPointerException} if the given value was <code>null</code>.
	 *
	 * @param value an arbitrary value, maybe <code>null</code>.
	 * @param message explanatory message to be used when throwing {@link NullPointerException}.
	 * @return the original passed value, guaranteed not to be <code>null</code>.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>.
	 */
	public static <T> @NonNull T requireNonNull(@Nullable T value, @NonNull String message) {
		if (value == null)
			throw new NullPointerException(message);
		return value;
	}

	/**
	 * Answer the given value, guaranteeing it to be neither <code>null</code> nor
	 * an empty string.
	 *
	 * @param value value to be checked
	 * @return the given value, guaranteed to be neither <code>null</code> nor
	 * and empty string.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>
	 * @throws IllegalArgumentException if the given value was an empty string.
	 */
	public static @NonNull String requireNonEmpty(@Nullable String value) {
		if (value == null)
			throw new NullPointerException();
		if (value.isEmpty())
			throw new IllegalArgumentException();
		return value;
	}

	/**
	 * Answer the given value, guaranteeing it to be neither <code>null</code> nor
	 * an empty string.
	 *
	 * @param value value to be checked
	 * @param message explanatory message to be used when throwing an exception.
	 * @return the given value, guaranteed to be neither <code>null</code> nor
	 * an empty string.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>
	 * @throws IllegalArgumentException if the given value was an empty string.
	 */
	public static @NonNull String requireNonEmpty(@Nullable String value, String message) {
		if (value == null)
			throw new NullPointerException(message);
		if (value.isEmpty())
			throw new IllegalArgumentException(message);
		return value;
	}

	/**
	 * Answer the given value, guaranteeing it to be neither <code>null</code> nor
	 * an empty collection. This method doesn't make any statement about whether
	 * or not elements in the collection can possibly be <code>null</code>.
	 *
	 * @param value value to be checked
	 * @return the given value, guaranteed to be neither <code>null</code> nor
	 * an empty collection.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>
	 * @throws IllegalArgumentException if the given value was an empty collection.
	 */
	public static <C extends Collection<?>> @NonNull C requireNonEmpty(@Nullable C value) {
		if (value == null)
			throw new NullPointerException();
		if (value.isEmpty())
			throw new IllegalArgumentException();
		return value;
	}

	/**
	 * Answer the given value, guaranteeing it to be neither <code>null</code> nor
	 * an empty collection. This method doesn't make any statement about whether
	 * or not elements in the collection can possibly be <code>null</code>.
	 *
	 * @param value value to be checked
	 * @param message explanatory message to be used when throwing an exception.
	 * @return the given value, guaranteed to be neither <code>null</code> nor
	 * an empty collection.
	 *
	 * @throws NullPointerException if the given value was <code>null</code>
	 * @throws IllegalArgumentException if the given value was an empty collection.
	 */
	public static <C extends Collection<?>> @NonNull C requireNonEmpty(@Nullable C value, String message) {
		if (value == null)
			throw new NullPointerException(message);
		if (value.isEmpty())
			throw new IllegalArgumentException(message);
		return value;
	}

	/**
	 * Answer whether the given value is <code>null</code>.
	 * Calling this method should express that a null check is performed
	 * which the compiler would normally deem unnecessary or redundant.
	 * By calling this method, these warnings will be avoided, and readers
	 * of that code will be informed that the check is redundant with respect
	 * to null analysis, and done only as a measure for extra safety.
	 * @param value an object which analysis at the call-site considers as non-null
	 * @return true if the argument is <code>null</code>, else false.
	 */
	@SuppressWarnings("null") // intentionally performing a redundant check
	public static boolean isNull(@NonNull Object value) { return value == null; }

	/**
	 * Answer whether any of the given values is <code>null</code>.
	 * Depending on the nullness of concrete types for <code>T</code>,
	 * this method may or may not be redundant from the point of view of
	 * static analysis.
	 * @param values arbitrary values to be checked
	 * @return true if the argument is <code>null</code>, else false.
	 */
	@SafeVarargs
	public static <T> boolean isAnyNull(T @NonNull... values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null)
				return true;
		}
		return false;
	}

	/**
	 * Answer whether an element in the provided values is <code>null</code>.
	 * Depending on the nullness of the given <code>Iterable</code>'s type argument,
	 * this method may or may not be redundant from the point of view of
	 * static analysis.
	 * @param values an iterable of arbitrary values
	 * @return true if the argument contains the value <code>null</code>, else false.
	 */
	public static boolean containsNull(@NonNull Iterable<?> values) {
		for (Object value : values) {
			if (value == null)
				return true;
		}
		return false;
	}

	/**
	 * Answer the value of an {@link Optional}, or <code>null</code> if it has no value.
	 *
	 * @param optional wrapper for an optional value
	 * @return the value or <code>null</code>.
	 */
	public static <T> @Nullable T asNullable(@NonNull Optional<T> optional) {
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	/**
	 * Answer the given value as a non-null value.
	 * If the value was actually <code>null</code> the alternative 'fallbackValue' is returned.
	 *
	 * @param value an arbitrary value, maybe <code>null</code>.
	 * @param fallbackValue value to be returned when the value is <code>null</code>.
	 *
	 * @return the original passed value, guaranteed not to be <code>null</code>, or the fallback value.
	 */
	public static <T> @NonNull T nonNullElse(@Nullable T value, @NonNull T fallbackValue) {
		if (value == null)
			return fallbackValue;
		return value;
	}

	/**
	 * Answer the given value as a non-null value.
	 * If the value was actually <code>null</code> an alternative is computed by
	 * invoking the given 'fallbackSupplier'.
	 *
	 * @param value an arbitrary value, maybe <code>null</code>.
	 * @param fallbackSupplier will compute the value to be returned when the 'value' is <code>null</code>.
	 *
	 * @return the original passed value, guaranteed not to be <code>null</code>,
	 * 	or a fallback value provided by the 'fallbackSupplier'.
	 */
	public static <T> @NonNull T nonNullElseGet(@Nullable T value, @NonNull Supplier<? extends @NonNull T> fallbackSupplier) {
		if (value == null)
			return fallbackSupplier.get();
		return value;
	}

	/**
	 * Invoke the given consumer if and only if the given value is not <code>null</code>.
	 * Otherwise do nothing.
	 * @param value the value to be checked for null and possibly passed into the consumer.
	 * @param consumer the consumer to invoke after checking the value for null.
	 */
	public static <T> void ifNonNull(@Nullable T value, @NonNull Consumer<@NonNull ? super T> consumer) {
		if (value != null)
			consumer.accept(value);
	}

	/**
	 * Apply the given function if and only if the given value is not <code>null</code>.
	 * @param value the value to be checked for <code>null</code> and possibly passed into the function.
	 * @param function the function to apply after checking the value for <code>null</code>.
	 * @return the result of applying 'function' with 'value',
	 * 	or <code>null</code> if 'value' was <code>null</code>
	 */
	public static <T, U> @Nullable U applyIfNonNull(@Nullable T value, @NonNull Function<@NonNull ? super T, ? extends U> function) {
		if (value != null)
			return function.apply(value);
		return null;
	}

	/**
	 * Apply the given function if and only if the given value is not <code>null</code>.
	 * @param value the value to be checked for <code>null</code> and possibly passed into the function.
	 * @param function the function to apply after checking the value for <code>null</code>.
	 * @param fallbackValue value to be returned when the 'value' is <code>null</code>.
	 * @return the result of applying 'function' with 'value',
	 * 	or the 'fallbackValue' if 'value' was <code>null</code>
	 */
	public static <T, U> U applyIfNonNullElse(@Nullable T value, @NonNull Function<@NonNull ? super T, ? extends U> function, U fallbackValue) {
		if (value != null)
			return function.apply(value);
		return fallbackValue;
	}

	/**
	 * Apply the given function if and only if the given value is not <code>null</code>.
	 * @param value the value to be checked for <code>null</code> and possibly passed into the function.
	 * @param function the function to apply after checking the value for <code>null</code>.
	 * @return the result of applying 'function' with 'value',
	 * 	or a value provided by invoking 'fallbackSupplier' if 'value' was <code>null</code>
	 */
	public static <T, U> U applyIfNonNullElseGet(@Nullable T value, @NonNull Function<@NonNull ? super T, ? extends U> function,
			@NonNull Supplier<? extends U> fallbackSupplier)
	{
		if (value != null)
			return function.apply(value);
		return fallbackSupplier.get();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed boolean corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static boolean unboxElse(@Nullable Boolean boxedValue, boolean fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.booleanValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed byte corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static byte unboxElse(@Nullable Byte boxedValue, byte fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.byteValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed char corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static char unboxElse(@Nullable Character boxedValue, char fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.charValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed int corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static int unboxElse(@Nullable Integer boxedValue, int fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.intValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed long corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static long unboxElse(@Nullable Long boxedValue, long fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.longValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed short corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static short unboxElse(@Nullable Short boxedValue, short fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.shortValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed float corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static float unboxElse(@Nullable Float boxedValue, float fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.floatValue();
	}

	/**
	 * Unbox the given 'boxedValue' if and only if it is not <code>null</code>.
	 * Otherwise the given 'fallbackValue' is returned.
	 * @param boxedValue value, can be <code>null</code>.
	 * @param fallbackValue the value to use if 'boxedValue' is <code>null</code>
	 * @return either the unboxed double corresponding to 'boxedValue' or
	 * 	'fallbackValue' if 'boxedValue' was <code>null</code>.
	 */
	public static double unboxElse(@Nullable Double boxedValue, double fallbackValue) {
		if (boxedValue == null)
			return fallbackValue;
		return boxedValue.doubleValue();
	}
}
