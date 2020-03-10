package org.springframework.data.util;

import lombok.Builder;
import lombok.Data;
import org.junit.Test;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ComparatorUtils}.
 *
 * @author Martin Konstiak
 */
class ComparatorUtilsTest {

	@Test
	public void orderedBySimpleType() {
		List<Entity> list = Arrays.asList(
				Entity.builder().name("1").build(),
				Entity.builder().name("2").build()
		);

		Comparator<Entity> comparator = ComparatorUtils.comparatorOf(Sort.by(Sort.Direction.DESC, "name"));
		List<Entity> sorted = list.stream().sorted(comparator).collect(Collectors.toList());

		assertThat(sorted.get(0).getName()).isEqualTo("2");
		assertThat(sorted.get(1).getName()).isEqualTo("1");
	}

	@Test
	public void orderedByInnerField() {
		List<Entity> list = Arrays.asList(
				Entity.builder().subEntity(Entity.builder().name("A").build()).build(),
				Entity.builder().subEntity(Entity.builder().name("B").build()).build()
		);

		Comparator<Entity> comparator = ComparatorUtils.comparatorOf(Sort.by(Sort.Direction.DESC, "subEntity.subName"));
		List<Entity> sorted = list.stream().sorted(comparator).collect(Collectors.toList());

		assertThat(sorted.get(0).getSubEntity().getName()).isEqualTo("B");
		assertThat(sorted.get(1).getSubEntity().getName()).isEqualTo("A");
	}

	@Test
	public void orderedNaturally() {
		List<Entity> list = Arrays.asList(
				Entity.builder().name("2").build(),
				Entity.builder().name("1").build()
		);

		Comparator<Entity> comparator = ComparatorUtils.comparatorOf(Sort.unsorted());
		List<Entity> sorted = list.stream().sorted(comparator).collect(Collectors.toList());

		assertThat(sorted.get(0).getName()).isEqualTo("1");
		assertThat(sorted.get(1).getName()).isEqualTo("2");
	}

	@Test
	public void nullsFirstInNaturalOrder() {
		List<Entity> list = Arrays.asList(
				Entity.builder().subEntity(Entity.builder().name("B").build()).build(),
				Entity.builder().build(),
				Entity.builder().subEntity(Entity.builder().name("A").build()).build()
		);

		Comparator<Entity> comparator = ComparatorUtils.comparatorOf(Sort.by(Sort.Direction.ASC, "subEntity.name"));
		List<Entity> sorted = list.stream().sorted(comparator).collect(Collectors.toList());

		assertThat(sorted.get(0).getSubEntity()).isNull();
		assertThat(sorted.get(1).getSubEntity().getName()).isEqualTo("A");
		assertThat(sorted.get(2).getSubEntity().getName()).isEqualTo("B");
	}

	@Test
	public void nullsLastInDescendingOrder() {
		List<Entity> list = Arrays.asList(
				Entity.builder().build(),
				Entity.builder().subEntity(Entity.builder().name("A").build()).build(),
				Entity.builder().subEntity(Entity.builder().name("B").build()).build()
		);

		Comparator<Entity> comparator = ComparatorUtils.comparatorOf(Sort.by(Sort.Direction.DESC, "subEntity.name"));
		List<Entity> sorted = list.stream().sorted(comparator).collect(Collectors.toList());

		assertThat(sorted.get(0).getSubEntity().getName()).isEqualTo("B");
		assertThat(sorted.get(1).getSubEntity().getName()).isEqualTo("A");
		assertThat(sorted.get(2).getSubEntity()).isNull();
	}

	@Builder
	@Data
	public static class Entity {
		String name;
		Entity subEntity;
	}
}