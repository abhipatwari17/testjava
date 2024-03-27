package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ApplicationVulnerabilities extends AbstractBaseEntity{

    private Long lowVulnerabilities;
    private Long mediumVulnerabilities;
    private Long fatalVulnerabilities;
    private Long trivialVulnerabilities;
    private Long criticalvulnerabilities;
    @OneToOne
    private Application application;
}
