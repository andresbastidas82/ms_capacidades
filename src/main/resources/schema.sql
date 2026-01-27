CREATE TABLE IF NOT EXISTS capacities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(90) NOT NULL
);

CREATE TABLE IF NOT EXISTS capacity_technology (
     capacity_id BIGINT NOT NULL,
     technology_id BIGINT NOT NULL,
     PRIMARY KEY (capacity_id, technology_id),
     CONSTRAINT fk_capacity
         FOREIGN KEY (capacity_id)
             REFERENCES capacities(id)
             ON DELETE CASCADE
);