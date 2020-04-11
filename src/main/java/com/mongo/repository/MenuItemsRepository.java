package com.mongo.repository;

import com.mongo.entity.MenuItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemsRepository extends MongoRepository<MenuItems, String> {

    Page<MenuItems> findAllById(String id, Pageable pageable);
}
