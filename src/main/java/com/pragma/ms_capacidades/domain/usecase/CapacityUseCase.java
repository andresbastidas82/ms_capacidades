package com.pragma.ms_capacidades.domain.usecase;

import com.pragma.ms_capacidades.domain.api.ICapacityServicePort;
import com.pragma.ms_capacidades.domain.exception.CapacityAlreadyExistsException;
import com.pragma.ms_capacidades.domain.exception.InvalidCapacityException;
import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.infrastructure.out.client.TechnologyClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.pragma.ms_capacidades.domain.utils.Constants.CAPACITY_ALREADY_EXISTS;
import static com.pragma.ms_capacidades.domain.utils.Constants.MAX_20_TECH;
import static com.pragma.ms_capacidades.domain.utils.Constants.MIN_3_TECH;
import static com.pragma.ms_capacidades.domain.utils.Constants.REPEATED_TECH;
import static com.pragma.ms_capacidades.domain.utils.Constants.TECHNOLOGY_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class CapacityUseCase implements ICapacityServicePort {

    private final ICapacityPersistencePort capacityPersistencePort;
    private final TechnologyClientPort technologyClientPort;

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return validateBusinessRules(capacity)
                //Usar Mono.defer para que existsByName solo se ejecute si la validación pasa
                .then(Mono.defer(() -> capacityPersistencePort.existsByName(capacity.getName())))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CapacityAlreadyExistsException(CAPACITY_ALREADY_EXISTS));
                    }
                    return capacityPersistencePort.save(capacity);
                });
    }

    private Mono<Void> validateBusinessRules(Capacity capacity) {

        List<Long> techs = capacity.getTechnologyIds();

        if (techs == null || techs.size() < 3) {
            return Mono.error(new InvalidCapacityException(MIN_3_TECH));
        }

        if (techs.size() > 20) {
            return Mono.error(new InvalidCapacityException(MAX_20_TECH));
        }

        if (techs.size() != techs.stream().distinct().count()) {
            return Mono.error(new InvalidCapacityException(REPEATED_TECH));
        }

        return technologyClientPort.existAllByIds(techs)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new InvalidCapacityException(TECHNOLOGY_NOT_EXIST));
                    }
                    return Mono.empty();
                });
    }
}
