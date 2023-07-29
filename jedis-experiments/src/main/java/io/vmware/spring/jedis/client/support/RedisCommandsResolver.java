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
package io.vmware.spring.jedis.client.support;

import org.springframework.lang.NonNull;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.KeyCommands;
import redis.clients.jedis.commands.StringCommands;

/**
 * Redis Commands Resolver used to resolve Jedis command interfaces given a required {@link Jedis} instance.
 *
 * @author John Blum
 * @see redis.clients.jedis.Jedis
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class RedisCommandsResolver {

	public static @NonNull KeyCommands resolveKeyCommands(@NonNull Jedis jedis) {
		return RedisAssertions.requireObject(jedis, "Jedis object is required");
	}

	public static @NonNull StringCommands resolveStringCommands(@NonNull Jedis jedis) {
		return RedisAssertions.requireObject(jedis, "Jedis object is required");
	}
}
