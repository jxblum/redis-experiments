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
package io.vmware.spring.data.redis.pubsub.client.data;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.cp.elements.lang.ObjectUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link Service} used to automatically generate Redis keys and values with optional expiration.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Slf4j
@Service
@EnableScheduling
@SuppressWarnings("unused")
public class RedisKeyValueGenerator {

	protected static final AtomicInteger keyNumber = new AtomicInteger(0);

	protected static final Duration DEFAULT_EXPIRATION_TIMEOUT = Duration.ZERO;

	protected static final String KEY_PREFIX = "key";

	@Setter(AccessLevel.PROTECTED)
	private Duration expirationTimeout;

	@Getter(AccessLevel.PROTECTED)
	private final RedisTemplate<String, String> redisTemplate;

	public RedisKeyValueGenerator(@NonNull RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = ObjectUtils.requireObject(redisTemplate, "RedisTemplate is required");
	}

	protected @NonNull Duration getExpirationTimeout() {
		return ObjectUtils.returnFirstNonNullValue(this.expirationTimeout, DEFAULT_EXPIRATION_TIMEOUT);
	}

	@Scheduled(fixedRateString = "${example.redis.keyvalue.generation.rate:1000}")
	public void generateKeyValue() {

		String key = generateKey();
		String value = generateValue();

		Duration expirationTimeout = getExpirationTimeout();

		logInfo("KEY [{}] VALUE [{}] with EXPIRATION [{}]", key, value, expirationTimeout);

		getRedisTemplate().opsForValue().set(key, value, expirationTimeout);
	}

	protected @NonNull String generateKey() {
		return KEY_PREFIX.concat(String.valueOf(keyNumber.incrementAndGet()));
	}

	protected @NonNull String generateValue() {
		return UUID.randomUUID().toString();
	}

	protected Logger getLogger() {
		return log;
	}

	protected void logInfo(String message, Object... arguments) {

		Logger logger = getLogger();

		if (logger.isInfoEnabled()) {
			logger.info(message, arguments);
		}
	}

	public @NonNull RedisKeyValueGenerator withExpirationTimeout(Duration expirationTimeout) {
		setExpirationTimeout(expirationTimeout);
		return this;
	}
}
