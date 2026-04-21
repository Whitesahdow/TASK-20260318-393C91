package com.busapp.repository;

import com.busapp.model.RouteStopMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteStopMappingRepository extends JpaRepository<RouteStopMapping, Long> {
    long countByRoute_IdAndStop_Id(Long routeId, Long stopId);
}
