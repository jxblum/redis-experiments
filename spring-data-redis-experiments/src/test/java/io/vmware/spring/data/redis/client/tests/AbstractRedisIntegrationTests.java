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

import com.redis.testcontainers.RedisContainer;

import org.junit.jupiter.api.BeforeAll;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.lang.NonNull;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract base class for Spring Boot and Spring Data Redis Integration Tests using the Lettuce driver
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
@Testcontainers
public abstract class AbstractRedisIntegrationTests {

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

	protected static @NonNull RedisStandaloneConfiguration redisStandaloneConfiguration(
			@NonNull RedisProperties redisProperties) {

		return new RedisStandaloneConfiguration(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
	}
}
