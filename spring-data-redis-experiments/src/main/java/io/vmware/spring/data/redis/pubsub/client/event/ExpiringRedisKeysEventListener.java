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
package io.vmware.spring.data.redis.pubsub.client.event;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link Component} used to listen for keys stored in Redis that have expired.
 *
 * @author John Blum
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.data.redis.core.RedisKeyExpiredEvent
 * @see org.springframework.stereotype.Component
 * @since 0.1.0
 */
@Slf4j
@Component
@SuppressWarnings("unused")
public class ExpiringRedisKeysEventListener {

	protected Logger getLogger() {
		return log;
	}

	@EventListener
	public void handle(@NonNull RedisKeyExpiredEvent<?> event) {
		getLogger().warn("Key [{}] Expired", resolveExpiredKey(event));
	}

	private String resolveExpiredKey(@NonNull RedisKeyExpiredEvent<?> event) {
		return new String(event.getId());
	}
}
