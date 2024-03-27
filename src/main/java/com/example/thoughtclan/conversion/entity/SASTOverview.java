package com.example.thoughtclan.conversion.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SASTOverview {
	
	@Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( columnDefinition = "BINARY(16)" )
    private UUID id;

	private Long newFlaws;
	
	private Long flawsReopened;
	
	private Long flawsOpen;
	
	private Long flawsFixed;
	
	private Long totalFlaws;
	
	private Long flawsNotMitigated;
	
	private Long totalFlawsSev0;
	
	private Long totalFlawsSev1;
	
	private Long totalFlawsSev2;
	
	private Long totalFlawsSev3;
	
	private Long totalFlawsSev4;
	
	private Long totalFlawsSev5;
	
	@OneToOne
	private SASTScan sastScan;

}
