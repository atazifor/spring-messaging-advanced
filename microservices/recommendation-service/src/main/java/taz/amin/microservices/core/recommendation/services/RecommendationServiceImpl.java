package taz.amin.microservices.core.recommendation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taz.amin.api.core.recommendation.Recommendation;
import taz.amin.api.core.recommendation.RecommendationService;
import taz.amin.api.exceptions.InvalidInputException;
import taz.amin.microservices.core.recommendation.persistence.RecommendationEntity;
import taz.amin.microservices.core.recommendation.persistence.RecommendationRepository;
import taz.amin.util.http.ServiceUtil;

import java.util.logging.Level;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;


    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get recommendations for product with id={}", productId);
        Flux<Recommendation> recommendations = repository.findByProductId(productId)
                .log(LOG.getName(), Level.FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));

        return recommendations;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        RecommendationEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .onErrorMap(DuplicateKeyException.class,
                        dpe -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .log(LOG.getName(), Level.FINE)
                .map(e -> mapper.entityToApi(e));
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
