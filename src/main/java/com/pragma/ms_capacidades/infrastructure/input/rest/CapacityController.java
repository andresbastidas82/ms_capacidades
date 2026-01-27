package com.pragma.ms_capacidades.infrastructure.input.rest;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/capacity")
public class CapacityController {

    private final ICapacityHelper capacityHelper;

    public CapacityController(ICapacityHelper capacityHelper) {
        this.capacityHelper = capacityHelper;
    }

    @PostMapping
    public Mono<ResponseEntity<CapacityResponse>> createCapacity(@Valid @RequestBody CapacityRequest capacityRequest) {
        return capacityHelper.createCapacity(capacityRequest).map(ResponseEntity::ok);
    }
}
