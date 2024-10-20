package taz.amin.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import taz.amin.microservices.core.product.persistence.ProductEntity;
import taz.amin.microservices.core.product.persistence.ProductRepository;
import taz.amin.util.http.ServiceUtil;

import taz.amin.api.core.product.Product;
import taz.amin.api.core.product.ProductService;
import taz.amin.api.exceptions.InvalidInputException;
import taz.amin.api.exceptions.NotFoundException;

import java.util.logging.Level;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get product info for id={}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(LOG.getName(), Level.FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        if (product.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + product.getProductId());
        }
        ProductEntity productEntity = mapper.apiToEntity(product);
        return repository.save(productEntity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key, Product Id: " + product.getProductId()))
                .map(e -> mapper.entityToApi(e));
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .log(LOG.getName(), Level.FINE)
                .map(e -> repository.delete(e))
                .flatMap(e -> e);

    }

    private Product setServiceAddress(Product e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
