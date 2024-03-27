package com.example.thoughtclan.conversion.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "vulnerabilities")
public class SASTVulnerabilities {
	
	@Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( columnDefinition = "BINARY(16)" )
    private UUID id;
	
	private Long categoryId;
	
	private String categoryName;
	
	@Column(columnDefinition = "text")
	private String description;
	
	private String severityLevel;
	
	private Long count;
	
	private String remediationStatus;
	
	private LocalDateTime dateFirstOccurrence;
	
	@Column(columnDefinition = "text")
	private String recommendations;
	
	private Long cweId;
	
	private String cweName;
	
	private String module;
	
	@Column(columnDefinition = "text")
	private String cweDescription;
	
	private String issueId;
	
	private String sourceFile;
	
	private String sourceFilePath;
	
	private Long lineNumber;
	
	@Column(columnDefinition = "text")
	private String functionPrototype;
	
	@ManyToOne
	private SASTScan sastScan;

}
