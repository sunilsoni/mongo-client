package com.mongo.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "menu_items")
public class MenuItems {

    @Id
    @Field(value = "Id")
    private String id;

    @Field(value = "MCP_MainCat_Identifier")
    private String mcpMainCatIdentifier;

    @Field(value = "MCP_SubCat_Identifier")
    private String mcpSubCatIdentifier;

    @Field(value = "ProgramName")
    private String programName;

    @Field(value = "BusinessType")
    private String businessType;

    @Field(value = "MCP_Owner_SCD")
    private String mcpOwnerSCD;

    @Field(value = "SCO_Delegate")
    private String scoDelegate;

    @Field(value = "OtherStakeholders")
    private String otherStakeholders;

    @Field(value = "Status")
    private String status;

    @Field(value = "Record_Begin_Date")
    private Instant recordBeginDate;

    @Field(value = "Record_End_Date")
    private Instant recordEndDate;

    @Field(value = "SegmentsIncluded")
    private String segmentsIncluded;

}
