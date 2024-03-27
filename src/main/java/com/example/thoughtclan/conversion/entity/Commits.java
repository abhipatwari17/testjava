package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
public class Commits extends AbstractBaseEntity {

    private String message;
    private String description;
    private Date createdOn;
    private String author;
    private String cId;
    @ManyToOne
    private Branch branch;
}
