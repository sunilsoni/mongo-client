package com.mongo.repository.factory;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.query.MongoQueryMethod;
import org.springframework.data.mongodb.repository.query.StringBasedMongoQuery;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

public class TypeAwareMongoRepositoryFactory extends MongoRepositoryFactory {

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final MongoOperations operations;

    public TypeAwareMongoRepositoryFactory(MongoOperations mongoOperations) {
        super(mongoOperations);
        this.operations = mongoOperations;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new MongoQueryLookupStrategy(operations, evaluationContextProvider,
                operations.getConverter().getMappingContext()));
    }

    private static class MongoQueryLookupStrategy implements QueryLookupStrategy {

        private final MongoOperations operations;
        private final QueryMethodEvaluationContextProvider evaluationContextProvider;
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;

        public MongoQueryLookupStrategy(MongoOperations operations, QueryMethodEvaluationContextProvider evaluationContextProvider,
                                        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext) {

            this.operations = operations;
            this.evaluationContextProvider = evaluationContextProvider;
            this.mappingContext = mappingContext;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                            NamedQueries namedQueries) {

            MongoQueryMethod queryMethod = new MongoQueryMethod(method, metadata, factory, mappingContext);
            String namedQueryName = queryMethod.getNamedQueryName();
            SpelExpressionParser expressionParser = new SpelExpressionParser();
            if (namedQueries.hasQuery(namedQueryName)) {
                String namedQuery = namedQueries.getQuery(namedQueryName);
                return new StringBasedMongoQuery(namedQuery, queryMethod, operations, EXPRESSION_PARSER, evaluationContextProvider);
            } else if (queryMethod.hasAnnotatedQuery()) {
                return new StringBasedMongoQuery(queryMethod, operations, EXPRESSION_PARSER, evaluationContextProvider);
            } else {
                return new TypeAwarePartTreeMongoQuery(queryMethod, operations, expressionParser, evaluationContextProvider);
            }
        }
    }
}

