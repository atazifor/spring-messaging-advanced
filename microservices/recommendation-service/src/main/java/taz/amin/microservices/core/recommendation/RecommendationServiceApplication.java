package taz.amin.microservices.core.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("taz.amin")
public class RecommendationServiceApplication {
	private static Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(RecommendationServiceApplication.class, args);
		String mongoDbHost = context.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = context.getEnvironment().getProperty("spring.data.mongodb.port");
		LOG.info("Connected to MongoDb: " + mongoDbHost + ":" + mongoDbPort);
	}

}
