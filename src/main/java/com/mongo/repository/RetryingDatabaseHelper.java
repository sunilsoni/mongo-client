package com.mongo.repository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.UniformRandomBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RetryingDatabaseHelper extends DatabaseHelper {
    @Autowired
    public RetryingDatabaseHelper(MenuItemsRepository menuItemsRepository,
                                  OwnershipRepository ownershipRepository) {

        super(menuItemsRepository, ownershipRepository);

        log.info("DatabaseHelper initialised ");
    }

    protected RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        UniformRandomBackOffPolicy backOffPolicy = new UniformRandomBackOffPolicy();
        backOffPolicy.setMinBackOffPeriod(50L);
        backOffPolicy.setMaxBackOffPeriod(150L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
        retryPolicy.setTimeout(TimeUnit.MINUTES.toMillis(5));
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}

