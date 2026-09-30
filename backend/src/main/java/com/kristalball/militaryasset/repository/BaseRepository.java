package com.kristalball.militaryasset.repository;

import com.kristalball.militaryasset.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseRepository extends JpaRepository<Base, Long> {
    Optional<Base> findByName(String name);
}
