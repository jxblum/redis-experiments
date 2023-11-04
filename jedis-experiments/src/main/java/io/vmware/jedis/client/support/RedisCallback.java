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
package io.vmware.jedis.client.support;

import redis.clients.jedis.Jedis;

/**
 * Java {@link FunctionalInterface} and {@literal Strategy} interface used for executing commands
 * inside Redis using Jedis.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see redis.clients.jedis.Jedis
 * @since 0.1.0
 */
@FunctionalInterface
public interface RedisCallback<T> {

	T doInRedis(Jedis jedis);

}
