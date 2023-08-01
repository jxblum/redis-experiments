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
package io.vmware.spring.data.redis.client.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import com.redis.testcontainers.RedisContainer;

import org.junit.jupiter.api.BeforeAll;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for Spring Boot and Spring Data Redis Integration Tests using the Lettuce (or Jedis) driver
 * and Testcontainers to bootstrap and configure a Redis server.
 *
 * @author John Blum
 * @see com.redis.testcontainers.RedisContainer
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.autoconfigure.data.redis.RedisProperties
 * @see org.springframework.data.redis.connection.RedisStandaloneConfiguration
 * @see org.testcontainers.junit.jupiter.Container
 * @see org.testcontainers.junit.jupiter.Testcontainers
 * @see org.testcontainers.utility.DockerImageName
 * @since 0.1.0
 */
@Slf4j
@Testcontainers
@SuppressWarnings("unused")
public abstract class AbstractRedisIntegrationTests {

	protected static final boolean ENABLE_LOGGING =
		Boolean.getBoolean("redis-experiments.logging.system-out.enabled");

	protected static final int REDIS_PORT = 6379;

	protected static final String DEFAULT_REDIS_DOCKER_IMAGE_VERSION = "7.2-rc2-alpine";

	protected static final DockerImageName REDIS_DOCKER_IMAGE =
		DockerImageName.parse(String.format("redis:%s",
			System.getProperty("redis.version", DEFAULT_REDIS_DOCKER_IMAGE_VERSION)));

	@Container
	@SuppressWarnings("all")
	protected static final RedisContainer redisContainer = new RedisContainer(REDIS_DOCKER_IMAGE)
		.withExposedPorts(REDIS_PORT);

	@BeforeAll
	public static void assertRedisContainerIsRunning() {

		assertThat(redisContainer).isNotNull();
		assertThat(redisContainer.isRunning()).isTrue();
	}

	@SuppressWarnings("unused")
	protected static @NonNull RedisStandaloneConfiguration redisStandaloneConfiguration(
			@NonNull RedisProperties redisProperties) {

		return new RedisStandaloneConfiguration(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
	}

	protected static Logger getLogger() {
		return log;
	}

	protected static void log(String message, Object... arguments) {

		logToLogger(message, arguments);

		if (ENABLE_LOGGING) {
			logToSystemOut(message, arguments);
		}
	}

	private static void logToLogger(String message, Object... arguments) {

		Logger logger = getLogger();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(message, arguments), arguments);
		}
	}

	private static void logToSystemOut(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	protected Duration timed(@NonNull Runnable runner) {

		Instant beforeRunTime = Instant.now();

		runner.run();

		Instant afterRunTime = Instant.now();

		return Duration.between(beforeRunTime, afterRunTime);
	}

	@SuppressWarnings({ "rawtypes" })
	protected @NonNull SessionCallback<?> toSessionCallback(@NonNull Function<RedisOperations, ?> sessionFunction) {

		return new SessionCallback<>() {

			@Override
			public Object execute(@NonNull RedisOperations redisOperations) throws DataAccessException {
				return sessionFunction.apply(redisOperations);
			}
		};
	}
}
