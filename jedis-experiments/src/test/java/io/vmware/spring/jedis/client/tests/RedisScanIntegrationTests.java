package io.vmware.spring.jedis.client.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.Getter;
import redis.clients.jedis.Jedis;

/**
 * Integration Tests for the Redis {@literal SCAN} command.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.testcontainers.containers.GenericContainer
 * @see org.testcontainers.junit.jupiter.Container
 * @see org.testcontainers.junit.jupiter.Testcontainers
 * @see <a href="https://redis.io/commands/scan/">Redis SCAN command</a>
 * @since 0.1.0
 */
@Getter
@SpringBootTest
@Testcontainers
@SuppressWarnings("unused")
public class RedisScanIntegrationTests {

  private static final int REDIS_PORT = 6379;

  private static final DockerImageName REDIS_DOCKER_IMAGE = DockerImageName.parse("redis:");

  @Container
  static GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_DOCKER_IMAGE)
    .withExposedPorts(REDIS_PORT);

  @BeforeAll
  public static void assertRedisContainerIsRunning() {

    assertThat(redisContainer).isNotNull();
    assertThat(redisContainer.isRunning()).isTrue();
  }

  @Autowired
  private Jedis jedis;

  @Test
  public void scanIsCorrect() {

    getJedis().sc
  }

  @TestConfiguration
  static class RedisTestConfiguration {

    @Bean
    Jedis jedisClient() {
      return new Jedis(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
    }
  }
}
