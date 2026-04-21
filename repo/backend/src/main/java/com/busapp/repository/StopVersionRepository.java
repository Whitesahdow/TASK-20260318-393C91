package com.busapp.repository;

import com.busapp.model.StopVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopVersionRepository extends JpaRepository<StopVersion, Long> {
    Optional<StopVersion> findTopByStopNameOrderByVersionNumberDesc(String stopName);
    List<StopVersion> findTop20ByOrderByImportedAtDesc();
    List<StopVersion> findByStopNameOrderByVersionNumberDesc(String stopName);
}
