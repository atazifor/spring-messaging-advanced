package taz.amin.microservices.core.recommendation;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public abstract class MongoDbTestBase {
     static MongoDBContainer database = new MongoDBContainer("mongo:6.0.4")
            .withStartupTimeout(Duration.ofSeconds(300))
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry)  {
        registry.add("spring.data.mongodb.host", database::getHost);
        registry.add("spring.data.mongodb.port", () -> database.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () ->  "test");
    }


}
