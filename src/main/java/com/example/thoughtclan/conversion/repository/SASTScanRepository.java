package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.SASTScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SASTScanRepository extends JpaRepository<SASTScan, UUID> {
	
	public List<SASTScan> findByScanIdAndBuildId(String scanId, String buildId);
	
}
