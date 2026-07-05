package com.products.health;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.Response;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ReadinessCheck.call() combines two independent checks (Mongo, Redis) with a plain
 * &amp;&amp;, so the "up" result requires both to succeed and the "down" result can come
 * from either one failing. The existing REST-level health test only exercises the
 * happy path where both dependencies are reachable, leaving the down-branches (and the
 * catch-block lines behind them) uncovered. Rather than standing up a @QuarkusTest with
 * globally mocked CDI beans (which would risk destabilizing every other test that relies
 * on the real Mongo/Redis clients for the rest of the suite), this test builds a plain
 * ReadinessCheck instance and wires Mockito doubles directly into its fields via
 * reflection - fully isolated from the CDI container.
 */
class ReadinessCheckTest {

    private ReadinessCheck newReadinessCheck(MongoClient mongoClient, Redis redis) throws Exception {
        ReadinessCheck check = new ReadinessCheck();
        setField(check, "appName", "products-api");
        setField(check, "environment", "test");
        setField(check, "mongoClient", mongoClient);
        setField(check, "redis", redis);
        return check;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = ReadinessCheck.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void call_returnsUp_whenMongoAndRedisAreReachable() throws Exception {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoClient.getDatabase("admin")).thenReturn(database);

        Redis redis = mock(Redis.class);
        when(redis.send(any())).thenReturn(Uni.createFrom().item(mock(Response.class)));

        ReadinessCheck check = newReadinessCheck(mongoClient, redis);

        HealthCheckResponse response = check.call();

        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void call_returnsDown_whenMongoIsUnreachable() throws Exception {
        MongoClient mongoClient = mock(MongoClient.class);
        when(mongoClient.getDatabase("admin")).thenThrow(new RuntimeException("mongo unreachable"));

        Redis redis = mock(Redis.class);
        when(redis.send(any())).thenReturn(Uni.createFrom().item(mock(Response.class)));

        ReadinessCheck check = newReadinessCheck(mongoClient, redis);

        HealthCheckResponse response = check.call();

        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
        assertThat(response.getData()).hasValueSatisfying(data -> assertThat(data)
                .containsEntry("mongodb", "down"));
    }

    @Test
    void call_returnsDown_whenRedisIsUnreachable() throws Exception {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoClient.getDatabase("admin")).thenReturn(database);

        Redis redis = mock(Redis.class);
        when(redis.send(any())).thenReturn(Uni.createFrom().failure(new RuntimeException("redis unreachable")));

        ReadinessCheck check = newReadinessCheck(mongoClient, redis);

        HealthCheckResponse response = check.call();

        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
        assertThat(response.getData()).hasValueSatisfying(data -> assertThat(data)
                .containsEntry("redis", "down"));
    }
}
