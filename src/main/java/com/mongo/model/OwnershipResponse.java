package com.mongo.model;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnershipResponse {
    private String id;
    private String status;
    private String programName;
}
