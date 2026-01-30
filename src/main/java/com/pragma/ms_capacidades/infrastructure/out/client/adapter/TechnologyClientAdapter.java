package com.pragma.ms_capacidades.infrastructure.out.client.adapter;

import com.pragma.ms_capacidades.infrastructure.input.rest.dto.TechnologyResponse;
import com.pragma.ms_capacidades.domain.spi.TechnologyClientPort;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechnologyClientAdapter implements TechnologyClientPort {

    private final WebClient webClient;

    public TechnologyClientAdapter(@Qualifier("technologyWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> existAllByIds(List<Long> ids) {
        return webClient.post()
                .uri("/technology/validate")
                .bodyValue(ids)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    @Override
    public Flux<TechnologyResponse> getTechnologiesByIds(List<Long> ids) {
        String idsParam = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/technology/byIds")
                                .queryParam("ids", idsParam)
                                .build()
                )
                .retrieve()
                .bodyToFlux(TechnologyResponse.class);
    }

    @Override
    public Mono<Boolean> deleteTechnolgies(List<Long> ids) {
        String idsParam = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return webClient.delete()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/technology")
                                .queryParam("ids", idsParam)
                                .build()
                )
                .retrieve()
                .bodyToMono(Boolean.class);
    }


}
