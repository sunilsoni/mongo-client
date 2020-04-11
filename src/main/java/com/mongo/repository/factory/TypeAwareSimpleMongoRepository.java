package com.mongo.repository.factory;

import org.bson.Document;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.io.Serializable;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class TypeAwareSimpleMongoRepository<T, ID extends Serializable> extends SimpleMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;
    private final Document classCriteriaDocument;
    private final Criteria classCriteria;

    public TypeAwareSimpleMongoRepository(MongoEntityInformation<T, ID> metadata,
                                          MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;

        String klassString = entityInformation.getJavaType().getCanonicalName();

        if (entityInformation.getJavaType().isAnnotationPresent(TypeAlias.class)) {
            klassString = entityInformation.getJavaType().getAnnotation(TypeAlias.class).value();
        }

        classCriteria = where("_class").is(klassString);
        classCriteriaDocument = classCriteria.getCriteriaObject();
    }

    @Override
    public long count() {
        return mongoOperations.getCollection(
                entityInformation.getCollectionName()).count(classCriteriaDocument);
    }

    @Override
    public List<T> findAll() {
        return mongoOperations.find(new Query().addCriteria(classCriteria),
                entityInformation.getJavaType(),
                entityInformation.getCollectionName());
    }
}

