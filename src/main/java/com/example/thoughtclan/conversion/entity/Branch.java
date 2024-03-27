package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
public class Branch extends AbstractBaseEntity{

    private String name;
    private LocalDateTime createdOn;
    private LocalDateTime lastCommittedAt;
    @ManyToOne
    private BitBucketRepository repository;
}
