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
public class PullRequests extends AbstractBaseEntity {

    private String message;
    private String description;
    private LocalDateTime createdOn;
    private String prId;
    private String author;
    private String status;
    private String comments;
    private String source;
    private String destination;
    @ManyToOne
    private BitBucketRepository bitBucketRepository;
}
