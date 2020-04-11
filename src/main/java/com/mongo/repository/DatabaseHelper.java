package com.mongo.repository;

import com.mongo.entity.MenuItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Primary
@Component
public class DatabaseHelper {

    private static final String RECONCILIATION_NAME = "digital";
    private static final Boolean RULE_SUPPRESSED_ITEMS_FILTERED = true;
    private static final Boolean DIGITAL_SUPPRESSED_ITEMS_FILTERED = true;

    private final Map<Class, MongoRepository> repositories;
    private final MenuItemsRepository menuItemsRepository;
    private final OwnershipRepository ownershipRepository;
    private final RetryTemplate retryTemplate;

    @Autowired
    public DatabaseHelper(MenuItemsRepository menuItemsRepository,
                          OwnershipRepository ownershipRepository) {
        repositories = new HashMap<>();
        repositories.put(MenuItemsRepository.class, menuItemsRepository);
        repositories.put(OwnershipRepository.class, ownershipRepository);

        this.menuItemsRepository = menuItemsRepository;
        this.ownershipRepository = ownershipRepository;

        this.retryTemplate = createRetryTemplate();

        log.info("DatabaseHelper initialised ");
    }

    public <R, T> R read(String id, Class<T> klass, Function<T, R> callback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        return doRead(id, callback, repository);
    }

    public <T> void read(String id, Class<T> klass, Consumer<T> callback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        doRead(id, (obj) -> {
            callback.accept(obj);
            return null;
        }, repository);
    }

    public <R, T> R update(String id, Class<T> klass, Function<T, R> callback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        return doUpdate(id, callback, null, repository);
    }

    public <T> void update(String id, Class<T> klass, Consumer<T> callback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        doUpdate(id, (obj) -> {
            callback.accept(obj);
            return null;
        }, null, repository);
    }

    public <R, T> R update(String id, Class<T> klass, Function<T, R> callback, Supplier<T> creationCallback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        return doUpdate(id, callback, creationCallback, repository);
    }

    public <T> void update(String id, Class<T> klass, Consumer<T> callback, Supplier<T> creationCallback) {
        MongoRepository<T, String> repository = lookupRepository(klass);
        doUpdate(id, (obj) -> {
            callback.accept(obj);
            return null;
        }, creationCallback, repository);
    }

    public void readAllMenuItems(String runId, Consumer<List<MenuItems>> callback) {
        Page<MenuItems> items = menuItemsRepository.findAllById(runId, PageRequest.of(0, 100000));
        callback.accept(items.getContent());
    }


    public <R> R readAllMenuItems(String runId, Function<List<MenuItems>, R> callback) {
        Page<MenuItems> items = menuItemsRepository.findAllById(runId, PageRequest.of(0, 100000));
        return callback.apply(items.getContent());
    }


    private <T, R> R doUpdate(String id, Function<T, R> callback, Supplier<T> creationCallback, MongoRepository<T, String> repository) {
        if (retryTemplate != null) {
            return retryTemplate.execute((ctx) -> doUpdateWithoutRetry(id, callback, creationCallback, repository));
        } else {
            return doUpdateWithoutRetry(id, callback, creationCallback, repository);
        }
    }

    private <T, R> R doUpdateWithoutRetry(String id, Function<T, R> callback, Supplier<T> creationCallback, MongoRepository<T, String> repository) {
        T object = repository.findById(id).orElse(null);

        if (object == null && creationCallback != null) {
            object = creationCallback.get();
        }

        R result = callback.apply(object);

        if (object != null) {
            repository.save(object);
        }

        return result;
    }

    private <T, R> R doRead(String id, Function<T, R> callback, MongoRepository<T, String> repository) {
        T object = repository.findById(id).orElse(null);

        return callback.apply(object);
    }

    protected RetryTemplate createRetryTemplate() {
        return null;
    }

    private <T> MongoRepository<T, String> lookupRepository(Class<T> klass) {
        MongoRepository<T, String> repository = repositories.get(klass);

        if (repository != null) {
            return repository;
        }

        throw new UnsupportedOperationException("Repository for " + klass.getCanonicalName() + " is not implemented.");
    }
}

