package com.example.thoughtclan.conversion.repository;

import com.example.thoughtclan.conversion.entity.Commits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommitsRepository extends JpaRepository<Commits, UUID> {
}
