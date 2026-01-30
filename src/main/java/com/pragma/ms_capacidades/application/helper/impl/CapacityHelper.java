package com.pragma.ms_capacidades.application.helper.impl;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.dto.PageResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import com.pragma.ms_capacidades.application.mapper.ICapacityRequestMapper;
import com.pragma.ms_capacidades.domain.api.ICapacityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Override
    public Mono<PageResponse<CapacityResponse>> getCapacities(int page, int size, String sortBy, String direction) {
        return capacityServicePort.count()
                .flatMap(total ->
                    capacityServicePort.getCapacities(page, size, sortBy, direction)
                        .map(capacityRequestMapper::toCapacityResponse)
                        .collectList()
                        .map(content -> PageResponse.<CapacityResponse>builder()
                                .page(page)
                                .size(size)
                                .content(content)
                                .totalElements(total)
                                .build()
                        )
                );
    }

    @Override
    public Flux<CapacityResponse> getCapacitiesByIds(List<Long> ids) {
        return capacityServicePort.getCapacitiesByIds(ids)
                .map(capacityRequestMapper::toCapacityResponse);
    }

    @Override
    public Mono<Boolean> deleteCapacities(List<Long> ids) {
        return capacityServicePort.deleteCapacities(ids);
    }


}
