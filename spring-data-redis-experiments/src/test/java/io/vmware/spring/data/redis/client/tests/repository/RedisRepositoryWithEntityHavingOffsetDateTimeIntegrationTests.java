/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.vmware.spring.data.redis.client.tests.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Integration Tests for Spring Data Redis {@literal Repositories} creating and reading an entity with
 * a {@link OffsetDateTime} property.
 *
 * @author John Blum
 * @see java.time.OffsetDateTime
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.core.RedisTemplate
 * @since 0.1.0
 */
@Getter
@DataRedisTest
@SuppressWarnings("unused")
public class RedisRepositoryWithEntityHavingOffsetDateTimeIntegrationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void userRepositorySaveAndFindSuccessful() {

		OffsetDateTime lastAccessed = OffsetDateTime.now();

		User jonDoe = User.as("Jon Doe").lastAccessed(lastAccessed);

		getUserRepository().save(jonDoe);

		User loadedUser = getUserRepository().findById(jonDoe.getName()).orElse(null);

		assertThat(loadedUser).isNotNull();
		assertThat(loadedUser).isNotSameAs(jonDoe);
		assertThat(loadedUser.getName()).isEqualTo("Jon Doe");
		assertThat(loadedUser.getLastAccessed()).isEqualTo(lastAccessed);
	}

	@SpringBootConfiguration
	@AutoConfigurationPackage(basePackageClasses = UserRepository.class)
	static class RedisConfiguration {

	}

	@Getter
	@RedisHash("Users")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "as")
	static class User {

		@Id
		private final String name;

		private OffsetDateTime lastAccessed;

		public @NonNull User lastAccessed(@Nullable OffsetDateTime dateTime) {
			this.lastAccessed = dateTime;
			return this;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	interface UserRepository extends CrudRepository<User, String> { }

}
