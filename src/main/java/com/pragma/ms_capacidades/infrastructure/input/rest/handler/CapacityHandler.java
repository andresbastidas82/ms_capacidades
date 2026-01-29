package com.pragma.ms_capacidades.infrastructure.input.rest.handler;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CapacityHandler {

    private final ICapacityHelper capacityHelper;

    public Mono<ServerResponse> createCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityRequest.class)
                .flatMap(capacityHelper::createCapacity)
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> listCapacities(ServerRequest request) {

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("asc");

        return capacityHelper
                .getCapacities(page, size, sortBy, direction)
                .flatMap(pageResponse ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(pageResponse)
                );
    }

    public Mono<ServerResponse> getCapacitiesByIds(ServerRequest request) {

        List<Long> ids = request.queryParam("ids")
                .map(value ->
                        Arrays.stream(value.split(","))
                                .map(String::trim)
                                .map(Long::valueOf)
                                .toList()
                )
                .orElseThrow(() -> new IllegalArgumentException("ids es requerido"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(capacityHelper.getCapacitiesByIds(ids), CapacityResponse.class);
    }

}
