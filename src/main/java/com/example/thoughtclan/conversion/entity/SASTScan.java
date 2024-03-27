package com.example.thoughtclan.conversion.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SASTScan {
	
	@Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( columnDefinition = "BINARY(16)" )
    private UUID id;
	
	private String name;
	
	private String scannedBy;
	
	private LocalDateTime scannedOn;
	
	private Long totalFlaws;
	
	private Long flawsNotMitigated;
	
	private String staticAnalysisUnitId;
	
	private String policyName;
	
	private String policyComplianceStatus;
	
	private String analysisRating;
	
	private String toolAppId;
	
	private String toolAccountId;
	
	private String score;
	
	private String scanId;
	
	private String buildId;
	
	@ManyToOne
	private Tools toolId;
	
	@ManyToOne
	private Application application;
	
	@ManyToOne
	private BitBucketRepository repository;

}
