package com.pragma.ms_capacidades.infrastructure.input.rest.handler;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.application.dto.PageResponse;
import com.pragma.ms_capacidades.application.helper.ICapacityHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityHandlerTest {

    @Mock
    private ICapacityHelper capacityHelper;

    @InjectMocks
    private CapacityHandler capacityHandler;

    @Test
    @DisplayName("Create Capacity: Should return 200 OK on successful creation")
    void createCapacity_ShouldReturnOk() {
        // Arrange
        CapacityRequest requestDto = new CapacityRequest();
        CapacityResponse responseDto = new CapacityResponse();

        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        when(capacityHelper.createCapacity(any(CapacityRequest.class)))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<ServerResponse> result = capacityHandler.createCapacity(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(capacityHelper).createCapacity(any(CapacityRequest.class));
    }

    @Test
    @DisplayName("List Capacities: Should use default params and return 200 OK")
    void listCapacities_WithDefaultParams_ShouldReturnOk() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder().build(); // No query params
        PageResponse<CapacityResponse> pageResponse = new PageResponse<>(0,10, 0, Collections.emptyList());

        when(capacityHelper.getCapacities(0, 10, "name", "asc"))
                .thenReturn(Mono.just(pageResponse));

        // Act
        Mono<ServerResponse> result = capacityHandler.listCapacities(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    @DisplayName("List Capacities: Should use provided params and return 200 OK")
    void listCapacities_WithCustomParams_ShouldReturnOk() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("page", "2")
                .queryParam("size", "5")
                .queryParam("sortBy", "technologyCount")
                .queryParam("direction", "desc")
                .build();

        PageResponse<CapacityResponse> pageResponse = new PageResponse<>(0,10, 0, Collections.emptyList());

        when(capacityHelper.getCapacities(2, 5, "technologyCount", "desc"))
                .thenReturn(Mono.just(pageResponse));

        // Act
        Mono<ServerResponse> result = capacityHandler.listCapacities(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Get Capacities By Ids: Should return 200 OK with data")
    void getCapacitiesByIds_ShouldReturnOk() {
        // Arrange
        String idsParam = "1, 2, 3";
        List<Long> expectedIds = Arrays.asList(1L, 2L, 3L);
        CapacityResponse responseDto = new CapacityResponse();

        MockServerRequest request = MockServerRequest.builder()
                .queryParam("ids", idsParam)
                .build();

        when(capacityHelper.getCapacitiesByIds(expectedIds))
                .thenReturn(Flux.just(responseDto));

        // Act
        Mono<ServerResponse> result = capacityHandler.getCapacitiesByIds(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(capacityHelper).getCapacitiesByIds(expectedIds);
    }

    @Test
    @DisplayName("Get Capacities By Ids: Should throw exception when ids param is missing")
    void getCapacitiesByIds_WhenIdsMissing_ShouldThrowException() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder().build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            capacityHandler.getCapacitiesByIds(request);
        });

        assertEquals("ids es requerido", exception.getMessage());
    }

    @Test
    @DisplayName("Delete Capacities: Should return 200 OK with true on success")
    void deleteCapacities_ShouldReturnOkWithTrue() {
        // Arrange
        String idsParam = "1, 2";
        List<Long> expectedIds = Arrays.asList(1L, 2L);

        MockServerRequest request = MockServerRequest.builder()
                .queryParam("ids", idsParam)
                .build();

        when(capacityHelper.deleteCapacities(expectedIds))
                .thenReturn(Mono.just(true));

        // Act
        Mono<ServerResponse> result = capacityHandler.deleteCapacities(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(capacityHelper).deleteCapacities(expectedIds);
    }

    @Test
    @DisplayName("Delete Capacities: Should return 200 OK with false when helper fails")
    void deleteCapacities_OnError_ShouldReturnOkWithFalse() {
        // Arrange
        String idsParam = "1";
        List<Long> expectedIds = List.of(1L);

        MockServerRequest request = MockServerRequest.builder()
                .queryParam("ids", idsParam)
                .build();

        // Simulamos un error en el helper
        when(capacityHelper.deleteCapacities(expectedIds))
                .thenReturn(Mono.error(new RuntimeException("Error interno")));

        // Act
        Mono<ServerResponse> result = capacityHandler.deleteCapacities(request);

        // Assert
        // Gracias a .onErrorReturn(false), esperamos un 200 OK (con body false)
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Delete Capacities: Should throw exception when ids param is missing")
    void deleteCapacities_WhenIdsMissing_ShouldThrowException() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder().build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            capacityHandler.deleteCapacities(request);
        });

        assertEquals("ids es requerido", exception.getMessage());
    }
}