package com.pragma.ms_capacidades.application.helper;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.dto.PageResponse;
import reactor.core.publisher.Mono;

public interface ICapacityHelper {

    Mono<CapacityResponse> createCapacity(CapacityRequest capacityRequest);

    Mono<PageResponse<CapacityResponse>> getCapacities(int page, int size, String sortBy, String direction);
}
