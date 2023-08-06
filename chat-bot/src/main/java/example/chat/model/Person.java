/*
 *  Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package example.chat.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.cp.elements.lang.ImmutableIdentifiable;
import org.cp.elements.lang.Nameable;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.Renderable;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;
import org.cp.elements.util.ComparatorResultBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Abstract Data Type (ADT) modeling a person.
 *
 * @author John Blum
 * @see java.lang.Comparable
 * @see java.io.Serializable
 * @see java.time.LocalDateTime
 * @see org.cp.elements.lang.ImmutableIdentifiable
 * @see org.cp.elements.lang.Nameable
 * @see org.cp.elements.lang.Renderable
 * @since 0.1.0
 */
@Getter
@RequiredArgsConstructor(staticName = "newPerson")
@SuppressWarnings("unused")
public class Person implements Comparable<Person>, ImmutableIdentifiable<String>, Nameable<String>, Renderable,
		Serializable {

	@Serial
	private static final long serialVersionUID = 4122518401562642688L;

	@Setter(AccessLevel.PROTECTED)
	private String id;

	@Setter(AccessLevel.PROTECTED)
	private Gender gender;

	@Setter(AccessLevel.PROTECTED)
	private LocalDateTime birthDate;

	@NotNull
	private final String firstName;

	@NotNull
	private final String lastName;

	public @NotNull String getName() {
		return String.format("%1$s %2$s", getFirstName(), getLastName());
	}

	public @NotNull Person as(@Nullable Gender gender) {
		setGender(gender);
		return this;
	}

	public @NotNull Person born(@Nullable LocalDateTime birthDate) {
		setBirthDate(birthDate);
		return this;
	}

	public @NotNull Person identifiedBy(@Nullable String id) {
		setId(id);
		return this;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int compareTo(Person that) {

		return ComparatorResultBuilder.<Comparable>create()
			.doCompare(this.getLastName(), that.getLastName())
			.doCompare(this.getFirstName(), that.getFirstName())
			.doCompare(this.getBirthDate(), that.getBirthDate())
			.build();
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Person that)) {
			return false;
		}

		return ObjectUtils.equals(this.getLastName(), that.getLastName())
			&& ObjectUtils.equals(this.getFirstName(), that.getFirstName())
			&& ObjectUtils.equalsIgnoreNull(this.getBirthDate(), that.getBirthDate());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirstName(), getLastName(), getBirthDate());
	}

	@Override
	public String toString() {
		return getName();
	}
}
