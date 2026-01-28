package com.pragma.ms_capacidades.infrastructure.out.r2dbc.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapacityWithTechCountProjection {

    private Long id;
    private String name;
    private String description;
    private Long technologyCount;
}
