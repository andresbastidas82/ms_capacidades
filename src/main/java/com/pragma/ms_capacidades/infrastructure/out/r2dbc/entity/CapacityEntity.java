package com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("capacities")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapacityEntity {

    @Id
    private Long id;
    private String name;
    private String description;

    @Transient
    private List<Long> technologyIds;
}
