package com.pragma.ms_capacidades.infrastructure.input.rest;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.dto.PageResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public Mono<ResponseEntity<PageResponse<CapacityResponse>>> listCapacities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return capacityHelper.getCapacities(page, size, sortBy, direction).map(ResponseEntity::ok);
    }
}
