package com.busapp.repository;

import com.busapp.model.BusStop;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusStopRepository extends JpaRepository<BusStop, Long> {
    Optional<BusStop> findByNameEn(String nameEn);
}
