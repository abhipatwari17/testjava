package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.SASTOverview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SASTOverviewRepository extends JpaRepository<SASTOverview, UUID> {
	
	public Optional<SASTOverview> findBySastScan_Id(UUID uuid);
	
}
