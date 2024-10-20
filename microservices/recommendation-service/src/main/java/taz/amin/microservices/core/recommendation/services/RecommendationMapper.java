package taz.amin.microservices.core.recommendation.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import taz.amin.api.core.recommendation.Recommendation;
import taz.amin.microservices.core.recommendation.persistence.RecommendationEntity;

import java.util.List;

@Mapper(componentModel = "spring") //make it a spring bean
public interface RecommendationMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true),
            @Mapping(target = "rate", source = "entity.rating")
    })
    Recommendation entityToApi(RecommendationEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "rating", source = "api.rate")
    })
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entityList);

    List<RecommendationEntity> apiListToEntityList(List<Recommendation> apiList);
}
