package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.BitBucketRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BitbucketRepoRepository extends JpaRepository<BitBucketRepository, UUID> {
	
	public BitBucketRepository findByNameAndApplication_Name(String name, String appName);
	
}
