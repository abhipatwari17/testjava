package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class BitBucketRepository extends AbstractBaseEntity {

    private String name;
    private String url;
    private String slug;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    @ManyToOne
    private Application application;
}
