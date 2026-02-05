package com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity;

import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Table("capacity_technology")
@NoArgsConstructor
public class CapacityTechnologyEntity {

    private Long capacityId;
    private Long technologyId;

    public CapacityTechnologyEntity(Long capacityId, Long technologyId) {
        this.capacityId = capacityId;
        this.technologyId = technologyId;
    }
}
