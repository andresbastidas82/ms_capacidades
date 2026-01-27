package com.pragma.ms_capacidades.application.helper.impl;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import com.pragma.ms_capacidades.application.mapper.ICapacityRequestMapper;
import com.pragma.ms_capacidades.domain.api.ICapacityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CapacityHelper implements ICapacityHelper {

    private final ICapacityServicePort capacityServicePort;
    private final ICapacityRequestMapper capacityRequestMapper;

    @Override
    public Mono<CapacityResponse> createCapacity(CapacityRequest capacityRequest) {
        return Mono.just(capacityRequest)
                .map(capacityRequestMapper::toCapacityModel)
                .flatMap(capacityServicePort::save)
                .map(capacityRequestMapper::toCapacityResponse);
    }
}
