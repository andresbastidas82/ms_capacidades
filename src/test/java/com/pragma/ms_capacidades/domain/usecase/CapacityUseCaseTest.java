package com.pragma.ms_capacidades.domain.usecase;

import com.pragma.ms_capacidades.domain.exception.CapacityAlreadyExistsException;
import com.pragma.ms_capacidades.domain.exception.InvalidCapacityException;
import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.infrastructure.out.client.TechnologyClientPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityUseCaseTest {

    @Mock
    private ICapacityPersistencePort capacityPersistencePort;

    @Mock
    private TechnologyClientPort technologyClientPort;

    @InjectMocks
    private CapacityUseCase capacityUseCase;

    @Test
    @DisplayName("Save should succeed when all rules are met")
    void save_WhenAllRulesMet_ShouldReturnCapacity() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Fullstack Java", "Descripción", techIds, null, null);

        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(true));
        when(capacityPersistencePort.existsByName(capacity.getName())).thenReturn(Mono.just(false));
        when(capacityPersistencePort.save(capacity)).thenReturn(Mono.just(capacity));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectNext(capacity)
                .verifyComplete();

        verify(technologyClientPort).existAllByIds(techIds);
        verify(capacityPersistencePort).existsByName(capacity.getName());
        verify(capacityPersistencePort).save(capacity);
    }

    @Test
    @DisplayName("Save should fail when technology list is null or empty")
    void save_WhenTechListIsNull_ShouldThrowException() {
        // Arrange
        Capacity capacity = new Capacity(null, "Name", "Desc", null, null, null);

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCapacityException.class)
                .verify();

        verifyNoInteractions(technologyClientPort, capacityPersistencePort);
    }

    @Test
    @DisplayName("Save should fail when technology list has less than 3 items")
    void save_WhenTechListHasLessThan3_ShouldThrowException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L);
        Capacity capacity = new Capacity(null, "Name", "Desc", techIds, null, null);

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCapacityException.class) // Espera MIN_3_TECH
                .verify();
    }

    @Test
    @DisplayName("Save should fail when technology list has more than 20 items")
    void save_WhenTechListHasMoreThan20_ShouldThrowException() {
        // Arrange
        List<Long> techIds = LongStream.rangeClosed(1, 21).boxed().collect(Collectors.toList());
        Capacity capacity = new Capacity(null, "Name", "Desc", techIds, null, null);

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCapacityException.class) // Espera MAX_20_TECH
                .verify();
    }

    @Test
    @DisplayName("Save should fail when technology list has duplicates")
    void save_WhenTechListHasDuplicates_ShouldThrowException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 1L); // 1L repetido
        Capacity capacity = new Capacity(null, "Name", "Desc", techIds, null, null);

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCapacityException.class) // Espera REPEATED_TECH
                .verify();
    }

    @Test
    @DisplayName("Save should fail when technologies do not exist in external service")
    void save_WhenTechsDoNotExist_ShouldThrowException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Name", "Desc", techIds, null, null);

        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(false));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCapacityException.class) // Espera TECHNOLOGY_NOT_EXIST
                .verify();

        verify(technologyClientPort).existAllByIds(techIds);
        verifyNoInteractions(capacityPersistencePort); // No debe intentar guardar ni buscar nombre
    }

    @Test
    @DisplayName("Save should fail when capacity name already exists")
    void save_WhenCapacityNameExists_ShouldThrowException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Existing Name", "Desc", techIds, null, null);

        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(true));
        when(capacityPersistencePort.existsByName(capacity.getName())).thenReturn(Mono.just(true));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(CapacityAlreadyExistsException.class)
                .verify();

        verify(technologyClientPort).existAllByIds(techIds);
        verify(capacityPersistencePort).existsByName(capacity.getName());
        verify(capacityPersistencePort, never()).save(any());
    }

}