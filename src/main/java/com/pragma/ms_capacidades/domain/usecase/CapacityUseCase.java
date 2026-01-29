package com.pragma.ms_capacidades.domain.usecase;

import com.pragma.ms_capacidades.domain.api.ICapacityServicePort;
import com.pragma.ms_capacidades.domain.exception.BadRequestException;
import com.pragma.ms_capacidades.domain.exception.CapacityAlreadyExistsException;
import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.model.Technology;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.domain.spi.TechnologyClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.pragma.ms_capacidades.domain.utils.Constants.CAPACITY_ALREADY_EXISTS;
import static com.pragma.ms_capacidades.domain.utils.Constants.DESCRIPTION_IS_REQUIRED;
import static com.pragma.ms_capacidades.domain.utils.Constants.INVALID_TECH_SIZE;
import static com.pragma.ms_capacidades.domain.utils.Constants.NAME_IS_REQUIRED;
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

    @Override
    public Flux<Capacity> getCapacities(int page, int size, String sortBy, String direction) {
        return capacityPersistencePort
                .findAllPaged(page, size, sortBy, direction)
                .flatMap(this::getCapacityWhitTechnologies);
    }

    @Override
    public Mono<Long> count() {
        return capacityPersistencePort.count();
    }

    @Override
    public Flux<Capacity> getCapacitiesByIds(List<Long> ids) {
        return capacityPersistencePort.findCapacitiesByIds(ids)
                .flatMap(this::getCapacityWhitTechnologies);
    }

    private Mono<Capacity> getCapacityWhitTechnologies(Capacity capacity) {
        return capacityPersistencePort
                .findTechnologyIdsByCapacityId(capacity.getId())
                .collectList()
                .flatMap(technologyIds ->
                    technologyClientPort.getTechnologiesByIds(technologyIds)
                        .map(item -> new Technology(item.getId(), item.getName()))
                        .collectList()
                        .map(technologies -> {
                            capacity.setTechnologies(technologies);
                            capacity.setTechnologyCount(technologies.size());
                            return capacity;
                        })
                );
    }

    private Mono<Capacity> validateBusinessRules(Capacity capacity) {
        List<String> errors = new ArrayList<>();
        List<Long> techs = capacity.getTechnologyIds();

        if(capacity.getName() == null || capacity.getName().isEmpty()) {
            errors.add(NAME_IS_REQUIRED);
        }
        if(capacity.getDescription() == null || capacity.getDescription().isEmpty()) {
            errors.add(DESCRIPTION_IS_REQUIRED);
        }
        if (techs == null || techs.isEmpty() || techs.size() < 3 || techs.size() > 20) {
            errors.add(INVALID_TECH_SIZE);
        }
        if (techs != null && (techs.size() != techs.stream().distinct().count())) {
            errors.add(REPEATED_TECH);
        }

        return technologyClientPort.existAllByIds(techs)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        errors.add(TECHNOLOGY_NOT_EXIST);
                    }
                    if (!errors.isEmpty()) {
                        return Mono.error(new BadRequestException(String.join("|", errors)));
                    }
                    return Mono.just(capacity);
                });
    }
}
