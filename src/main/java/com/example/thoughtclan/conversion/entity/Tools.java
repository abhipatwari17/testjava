package com.example.thoughtclan.conversion.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Tools{
	
	@Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( columnDefinition = "BINARY(16)" )
    private UUID id;
	
	public String name;

}
