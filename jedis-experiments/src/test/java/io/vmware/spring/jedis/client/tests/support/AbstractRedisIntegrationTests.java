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
package io.vmware.spring.jedis.client.tests.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.vmware.spring.jedis.client.support.RedisCallback;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Abstract base class for running Redis Integration Tests.
 *
 * @author John Blum
 * @see org.testcontainers.containers.GenericContainer
 * @see org.testcontainers.junit.jupiter.Container
 * @see org.testcontainers.junit.jupiter.Testcontainers
 * @see org.testcontainers.utility.DockerImageName
 * @since 0.1.0
 */
@Getter
@Testcontainers
public abstract class AbstractRedisIntegrationTests {

	protected static final int REDIS_PORT = 6379;

	protected static final String DEFAULT_REDIS_DOCKER_IMAGE_VERSION = "7.2-rc2-alpine";

	protected static final DockerImageName REDIS_DOCKER_IMAGE =
		DockerImageName.parse(String.format("redis:%s",
			System.getProperty("redis.version", DEFAULT_REDIS_DOCKER_IMAGE_VERSION)));

	@Container
	@SuppressWarnings("all")
	protected static final GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_DOCKER_IMAGE)
		.withExposedPorts(REDIS_PORT);

	@BeforeAll
	public static void assertRedisContainerIsRunning() {

		assertThat(redisContainer).isNotNull();
		assertThat(redisContainer.isRunning()).isTrue();
	}

	protected abstract JedisPool getJedisPool();

	protected @Nullable <T> T runInRedis(@NonNull RedisCallback<T> callback) {

		try (Jedis jedis = getJedisPool().getResource()) {
			return callback.doInRedis(jedis);
		}
	}
}
