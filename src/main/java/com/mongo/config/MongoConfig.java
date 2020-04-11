package com.mongo.config;


import com.mongo.repository.factory.TypeAwareMongoRepositoryFactory;
import com.mongo.repository.factory.TypeAwareMongoRepositoryFactoryBean;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ServerSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.annotation.PreDestroy;

@Slf4j
@Configuration
@EnableConfigurationProperties
@EnableMongoAuditing
@EnableMongoRepositories(
        basePackages = {"com.mongo.repository"}
        //,repositoryBaseClass = TypeAwareMongoRepositoryFactory.class,
        //repositoryFactoryBeanClass = TypeAwareMongoRepositoryFactoryBean.class
)

@PropertySource("classpath:/mongodb.properties")
@ConditionalOnProperty(name = "test.mongo.enabled", havingValue = "false", matchIfMissing = true)
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final int MONGO_MAX_POOL_SIZE = 4;
    private static final int MONGO_MIN_POOL_SIZE = 1;

    private final Environment environment;
    private final String serverAddress;
    private final Boolean sslEnabled;
    private final String databaseName;
    private final String username;
    private final String password;
    private final MongoClient mongoClient = null;

    @Autowired
    public MongoConfig(Environment environment,
                       @Value("${mongo.server.address}") String serverAddress,
                       @Value("${mongo.server.use-ssl}") Boolean serverUseSsl,
                       @Value("${mongo.database.name}") String databaseName,
                       @Value("${mongo.database.username}") String username,
                       @Value("${mongo.database.password}") String password) {
        this.environment = environment;
        this.serverAddress = serverAddress;
        this.sslEnabled = serverUseSsl;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    @PreDestroy
    public void cleanupMongoClient() {
        log.info("Shutting down and closing mongoClient connections!");
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    protected ConnectionString connectionString() {
        return new ConnectionString("mongodb://".concat(serverAddress));
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        //final ServerAddress address = new ServerAddress("127.0.0.1", 27017);
        final MongoCredential credential = MongoCredential
                .createCredential(username, getDatabaseName(), password.toCharArray());
        final MongoClientOptions options = new MongoClientOptions.Builder().build();
        final MongoClientSettings settings = MongoClientSettings.builder()
                .readPreference(ReadPreference.secondaryPreferred())
                .applyToConnectionPoolSettings(builder -> builder.maxSize(MONGO_MAX_POOL_SIZE).minSize(MONGO_MIN_POOL_SIZE))
                .applyToServerSettings(builder -> builder
                        .applySettings(ServerSettings
                                .builder()
                                .applyConnectionString(connectionString())
                                .build()))
                .credential(credential)
                .build();
        return MongoClients.create(settings);
    }

}

