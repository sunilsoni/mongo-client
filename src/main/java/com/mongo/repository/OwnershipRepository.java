package com.mongo.repository;

import com.mongo.entity.Ownership;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnershipRepository extends MongoRepository<Ownership, String> {

    List<Ownership> findAllByStatus(String status);

    List<Ownership> findFirstByRecordBeginDateAndRecordEndDate(String recordBeginDate, String recordEndDate);
}
