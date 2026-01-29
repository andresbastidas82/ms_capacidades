package com.pragma.ms_capacidades.domain.spi;

import com.pragma.ms_capacidades.infrastructure.input.rest.dto.TechnologyResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyClientPort {

    Mono<Boolean> existAllByIds(List<Long> ids);
    Flux<TechnologyResponse> getTechnologiesByIds(List<Long> ids);

}
