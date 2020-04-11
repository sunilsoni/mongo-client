package com.mongo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Service;

@Setter
@Getter
public class OwnershipRequest {
    private String id;
    private String status;
    private String programName;
}
