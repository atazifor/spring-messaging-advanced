package taz.amin.microservices.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import taz.amin.api.core.recommendation.Recommendation;
import taz.amin.api.event.Event;
import taz.amin.api.exceptions.InvalidInputException;
import taz.amin.microservices.core.recommendation.persistence.RecommendationRepository;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase{
	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Recommendation>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getRecommendationsByProductId() {

		int productId = 1;

		sendCreateRecommendationEvent(productId, 1);
		sendCreateRecommendationEvent(productId, 2);
		sendCreateRecommendationEvent(productId, 3);

		assertEquals(3, (long)repository.findByProductId(productId).count().block());

		getAndVerifyRecommendation("?productId="+productId, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);

		assertEquals(1, (long)repository.count().block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateRecommendationEvent(productId, recommendationId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", thrown.getMessage());

		assertEquals(1, (long)repository.count().block());
	}

	@Test
	void deleteRecommendation() {
		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);
		assertEquals(1, (long)repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);

		assertEquals(0, (long)repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
	}

	@Test
	void getRecommendationsInvalid() {
		int productId = -1;
		getAndVerifyRecommendation("?productId="+productId, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productId);
	}


	private WebTestClient.BodyContentSpec getAndVerifyRecommendation(String productIdQuery, HttpStatus expectedHttpStatus) {
		return client.get()
				.uri("/recommendation"+productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedHttpStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateRecommendationEvent(int productId, int recommendationId) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
		Event<Integer, Recommendation> event = new Event(Event.Type.CREATE, productId, recommendation);
		messageProcessor.accept(event);
	}

	private void sendDeleteRecommendationEvent(int productId) {
		Event<Integer, Recommendation> event = new Event(Event.Type.DELETE, productId, null);
		messageProcessor.accept(event);
	}

}
