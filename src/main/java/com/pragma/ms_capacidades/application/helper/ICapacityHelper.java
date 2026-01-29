package com.pragma.ms_capacidades.application.helper;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.dto.PageResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapacityHelper {

    Mono<CapacityResponse> createCapacity(CapacityRequest capacityRequest);

    Mono<PageResponse<CapacityResponse>> getCapacities(int page, int size, String sortBy, String direction);

    Flux<CapacityResponse> getCapacitiesByIds(List<Long> ids);
}
