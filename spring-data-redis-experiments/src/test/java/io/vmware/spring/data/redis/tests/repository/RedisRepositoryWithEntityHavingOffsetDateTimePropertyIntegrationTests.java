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
package io.vmware.spring.data.redis.tests.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;
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
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.repository.configuration.EnableRedisRepositories
 * @see org.springframework.test.context.ContextConfiguration
 * @see io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests
 * @see io.vmware.spring.data.redis.tests.repository.RedisRepositoryWithEntityHavingOffsetDateTimePropertyIntegrationTests.RedisTestConfiguration
 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2677">Jsr310Converters does not contain converters for OffsetTime and OffsetDateTime</a>
 * @since 0.1.0
 */
@Getter
@DataRedisTest
@ContextConfiguration(classes = RedisRepositoryWithEntityHavingOffsetDateTimePropertyIntegrationTests
	.RedisTestConfiguration.class)
@SuppressWarnings("unused")
public class RedisRepositoryWithEntityHavingOffsetDateTimePropertyIntegrationTests
		extends AbstractRedisIntegrationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void userRepositorySaveAndFindSuccessful() {

		OffsetDateTime lastAccessed = OffsetDateTime.now();
		OffsetTime timeToLive = OffsetTime.now().plusMinutes(5);

		User jonDoe = User.as("Jon Doe").lastAccessed(lastAccessed).timeToLive(timeToLive);

		getUserRepository().save(jonDoe);

		User loadedUser = getUserRepository().findById(jonDoe.getName()).orElse(null);

		assertThat(loadedUser).isNotNull();
		assertThat(loadedUser).isNotSameAs(jonDoe);
		assertThat(loadedUser.getName()).isEqualTo("Jon Doe");
		assertThat(loadedUser.getLastAccessed()).isEqualTo(lastAccessed);
		assertThat(loadedUser.getTimeToLive()).isEqualTo(timeToLive);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableRedisRepositories(considerNestedRepositories = true)
	static class RedisTestConfiguration {

		@Bean
		RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			return redisStandaloneConfiguration(redisProperties);
		}

		@Bean
		BeanPostProcessor redisCustomConversionsBeanPostProcessor() {

			return new BeanPostProcessor() {

				@Override
				public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

					return bean instanceof RedisCustomConversions
						? new RedisCustomConversions(ApplicationConverters.list())
						: bean;
				}
			};
		}
	}

	static abstract class ApplicationConverters {

		static List<?> list() {
			return List.of(
				new BytesToOffsetDateTimeConverter(),
				new BytesToOffsetTimeConverter(),
				new OffsetDateTimeToBytesConverter(),
				new OffsetTimeToBytesConverter()
			);
		}

		static class StringBasedConverter {

			static final Charset CHARSET = StandardCharsets.UTF_8;

			byte[] fromString(String source) {
				return source.getBytes(CHARSET);
			}

			String toString(byte[] bytes) {
				return new String(bytes, CHARSET);
			}
		}

		static class BytesToOffsetTimeConverter extends StringBasedConverter implements Converter<byte[], OffsetTime> {

			@Override
			public OffsetTime convert(@NonNull byte[] source) {
				return OffsetTime.parse(toString(source));
			}
		}

		static class BytesToOffsetDateTimeConverter extends StringBasedConverter implements Converter<byte[], OffsetDateTime> {

			@Override
			public OffsetDateTime convert(@NonNull byte[] source) {
				return OffsetDateTime.parse(toString(source));
			}
		}

		static class OffsetDateTimeToBytesConverter extends StringBasedConverter implements Converter<OffsetDateTime, byte[]> {

			@Override
			public byte[] convert(OffsetDateTime source) {
				return fromString(source.toString());
			}
		}

		static class OffsetTimeToBytesConverter extends StringBasedConverter implements Converter<OffsetTime, byte[]> {

			@Override
			public byte[] convert(OffsetTime source) {
				return fromString(source.toString());
			}
		}
	}

	@Getter
	@RedisHash("Users")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "as")
	static class User {

		@Id
		private final String name;

		private OffsetDateTime lastAccessed;

		private OffsetTime timeToLive;

		public @NonNull User lastAccessed(@Nullable OffsetDateTime dateTime) {
			this.lastAccessed = dateTime;
			return this;
		}

		public @NonNull User timeToLive(@Nullable OffsetTime time) {
			this.timeToLive = time;
			return this;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	interface UserRepository extends CrudRepository<User, String> { }

}
