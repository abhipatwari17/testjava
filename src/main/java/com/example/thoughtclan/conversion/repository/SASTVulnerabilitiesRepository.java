package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.SASTVulnerabilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SASTVulnerabilitiesRepository extends JpaRepository<SASTVulnerabilities, UUID> {
}
