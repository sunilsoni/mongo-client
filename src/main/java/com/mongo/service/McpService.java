package com.mongo.service;

import com.mongo.entity.Ownership;
import com.mongo.exception.DataNotFoundException;
import com.mongo.model.OwnershipRequest;
import com.mongo.repository.DatabaseHelper;
import com.mongo.repository.MenuItemsRepository;
import com.mongo.repository.OwnershipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class McpService {

    private final MenuItemsRepository menuItemsRepository;
    private final OwnershipRepository ownershipRepository;
    private final DatabaseHelper databaseHelper;

    @Autowired
    public McpService(MenuItemsRepository menuItemsRepository, OwnershipRepository ownershipRepository, DatabaseHelper databaseHelper) {
        this.menuItemsRepository = menuItemsRepository;
        this.ownershipRepository = ownershipRepository;
        this.databaseHelper = databaseHelper;
    }


    public void updateOwnershipDetailsById(OwnershipRequest request) {

        Optional<Ownership> optionalOwnership = ownershipRepository.findById(request.getId());

        if (!optionalOwnership.isPresent()) {
            throw new DataNotFoundException("Ownership Details not found for Id " + request.getId());
        }

        Ownership ownership = optionalOwnership.get();
        databaseHelper.update(ownership.getId(), Ownership.class, o -> {
            String status = o.getStatus();
            log.info("Old Status was: {} ",status);
            o.setStatus(request.getStatus());
        });
    }
}
