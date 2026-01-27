package com.pragma.ms_capacidades.domain.api;

import com.pragma.ms_capacidades.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface ICapacityServicePort {
    Mono<Capacity> save(Capacity capacity);
}
