package com.busapp.repository;

import com.busapp.model.BusRoute;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {
    Optional<BusRoute> findByRouteNumber(String routeNumber);
}
