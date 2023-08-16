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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.commands.KeyCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * Java {@link Iterator} for iterating over the results of a Reds {@literal SCAN}.
 *
 * @author John Blum
 * @see java.util.Iterator
 * @see java.util.List
 * @see java.util.function.Function
 * @see redis.clients.jedis.params.ScanParams
 * @see redis.clients.jedis.resps.ScanResult
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class ScanIterator implements Iterator<List<String>> {

	protected static final int DEFAULT_COUNT = 1;

	public static final String END_OF_CURSOR = "0";

	public static ScanIterator from(@NonNull Function<RedisCallback<List<String>>, List<String>> redisCommandFunction,
		@NonNull String pattern) {

		return from(redisCommandFunction, pattern, DEFAULT_COUNT);
	}

	public static ScanIterator from(@NonNull Function<RedisCallback<List<String>>, List<String>> redisCommandFunction,
		@NonNull String pattern, int count) {

		return new ScanIterator(redisCommandFunction, pattern, count);
	}

	private final Function<RedisCallback<List<String>>, List<String>> redisCommandFunction;

	private final ScanParams scanParams;

	@Setter(AccessLevel.PRIVATE)
	private String cursor;

	protected ScanIterator(@NonNull Function<RedisCallback<List<String>>, List<String>> redisCommandFunction,
		@NonNull String pattern, int count) {

		Assert.notNull(redisCommandFunction,
			"Function used to execute SCAN command inside Redis is required");

		this.redisCommandFunction = redisCommandFunction;
		this.scanParams = newScanParams(pattern, count);
	}

	private @NonNull ScanParams newScanParams(@NonNull String pattern, int count) {

		Assert.hasText(pattern, () ->
			String.format("Pattern [%s] used to match keys in the SCAN is required", pattern));

		ScanParams scanParams = new ScanParams();

		scanParams.count(Math.max(DEFAULT_COUNT, count));
		scanParams.match(pattern);

		return scanParams;
	}

	@Override
	public boolean hasNext() {
		return !END_OF_CURSOR.equals(getCursor());
	}

	@Override
	public List<String> next() {

		Assert.isTrue(hasNext(), "SCAN is at the end of the cursor");

		return getRedisCommandFunction().apply(jedis -> {

			KeyCommands keyCommands = RedisCommandsResolver.resolveKeyCommands(jedis);

			ScanResult<String> scanResult = keyCommands.scan(resolveCursor(), getScanParams());

			setCursor(scanResult.getCursor());

			return scanResult.getResult();
		});
	}

	private String resolveCursor() {
		String cursor = getCursor();
		return StringUtils.hasText(cursor) ? cursor : END_OF_CURSOR;
	}
}
