package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Application extends AbstractBaseEntity {

    private String name;
    private String url;
    private String ownerName;
    private String ownerEmail;
    private String workspaceSlug;
    private Boolean isPrivate;
    private String applicationKey;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
