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
package io.vmware.spring.data.redis.connection;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.cp.elements.lang.NumberUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.redis.connection.RedisClusterNode;

import lombok.Getter;

/**
 * JMH Benchmarks for Spring Data Redis {@link RedisClusterNode.SlotRange}.
 *
 * @author John Blum
 * @see org.openjdk.jmh.annotations.Benchmark
 * @see org.springframework.data.redis.connection.RedisClusterNode.SlotRange
 * @since 0.1.0
 */
@Fork(value = 1)
@Measurement(iterations = 3, time=1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 1)
public class RedisClusterNodeSlotRangeBenchmarks {

	public static void main(String[] args) throws IOException {
		org.openjdk.jmh.Main.main(args);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@SuppressWarnings("unused")
	public void measureSlotRangeContainsSlot(Blackhole blackhole, ExecutionPlan executionPlan) {

		for (int slot = 0; slot < executionPlan.getIterations(); slot++) {
			executionPlan.containsSlot(slot);
		}
	}

	@Getter
	@State(Scope.Benchmark)
	@SuppressWarnings("unused")
	public static class ExecutionPlan {

		@Param({ "2000", "3000", "5000", "10000" })
		private int iterations;

		private RedisClusterNode.SlotRange slotRange;

		@Setup(Level.Iteration)
		public void setup() {

			Set<Integer> slots = new HashSet<>(getIterations());

			for (int slot = 0, iterations = getIterations(); slot < iterations; slot++) {
				if (NumberUtils.isPrime(slot)) {
					slots.add(slot);
				}
			}

			this.slotRange = new RedisClusterNode.SlotRange(slots);
		}

		boolean containsSlot(int slot) {
			return getSlotRange().contains(slot);
		}
	}
}
