package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
	
	public Branch findByNameAndRepository_NameAndRepository_Application_Name(String branchName, String repoName, String appName);
	
}
