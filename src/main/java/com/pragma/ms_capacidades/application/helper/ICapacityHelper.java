package com.pragma.ms_capacidades.application.helper;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import reactor.core.publisher.Mono;

public interface ICapacityHelper {

    Mono<CapacityResponse> createCapacity(CapacityRequest capacityRequest);
}
