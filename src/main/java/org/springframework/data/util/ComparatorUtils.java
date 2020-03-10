package org.springframework.data.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static org.springframework.beans.BeanUtils.getPropertyDescriptor;

/**
 * Reflection based converter for {@link Sort} to make it applicable as Comparator.
 *
 * @author Martin Konstiak
 */

@UtilityClass
public class ComparatorUtils {

	public static <T> Comparator<T> comparatorOf(Sort sort) {
		return Comparator.nullsFirst(sort.get().map(ComparatorUtils::comparatorOf)
										 .map(Comparator::nullsFirst)
										 .reduce(Comparator::thenComparing)
										 .orElse(comparing(e -> 0)));
	}

	private static <T, U extends Comparable<? super U>> Comparator<T> comparatorOf(Sort.Order order) {
		Function<T, U> keyExtractor = keyExtractor(order.getProperty());
		Comparator<T> comparator = comparing(keyExtractor, nullSafeComparator());
		return order.isAscending() ? comparator : comparator.reversed();
	}

	private static <T, U> Function<T, U> keyExtractor(String propertiesPath) {
		return entity -> {
			Object innerValue = entity;
			for (String propertyName : parsePropertiesPath(propertiesPath)) {
				innerValue = getValue(innerValue, propertyName);

				if (innerValue == null) {
					return null;
				}
			}

			return (U) innerValue;
		};
	}

	private static List<String> parsePropertiesPath(String propertiesPath) {
		return ofNullable(StringUtils.split(propertiesPath, "."))
				.map(Arrays::asList)
				.orElse(Collections.emptyList());
	}

	private static <T extends Comparable<? super T>> Comparator<T> nullSafeComparator() {
		return Comparator.nullsFirst(Comparator.naturalOrder());
	}

	@Nullable
	private static Object getValue(Object entity, String propertyName) {
		return ofNullable(getPropertyDescriptor(entity.getClass(), propertyName))
				.map(descriptor -> getValue(descriptor, entity))
				.orElse(null);
	}

	private static <T> Object getValue(PropertyDescriptor descriptor, T entity) {
		try {
			return descriptor.getReadMethod().invoke(entity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException(String.format("Could not get value from specified property: %s", descriptor.getDisplayName()));
		}
	}
}
