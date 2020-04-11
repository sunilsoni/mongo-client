package com.mongo.controller;

import com.mongo.model.OwnershipRequest;
import com.mongo.model.OwnershipResponse;
import com.mongo.service.McpService;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @PostMapping("/ownership")
    @ResponseBody
    public ResponseEntity<OwnershipResponse> updateOwnershipDetails(@RequestBody OwnershipRequest ownershipRequest) {
        mcpService.updateOwnershipDetailsById(ownershipRequest);
        return ResponseEntity.ok(OwnershipResponse.builder().build());
    }
}
