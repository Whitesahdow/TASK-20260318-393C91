package com.busapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.busapp.model.BusStop;

@Repository
public interface SearchRepository extends JpaRepository<BusStop, Long> {

    @Query(value = """
        SELECT s.id AS stopId,
               s.name_en AS stopName,
               s.pinyin_initials AS initials,
               r.route_number AS routeNumber,
               ((r.frequency_priority * :fWeight) + (s.popularity_score * :pWeight)) AS score,
               s.popularity_score AS popularity
        FROM bus_stops s
        JOIN route_stop_mapping m ON s.id = m.stop_id
        JOIN bus_routes r ON m.route_id = r.id
        WHERE (LOWER(s.name_en) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(s.pinyin_initials) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(r.route_number) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY score DESC
        """, nativeQuery = true)
    List<SearchRowProjection> searchStops(
            @Param("query") String query,
            @Param("fWeight") double fWeight,
            @Param("pWeight") double pWeight
    );
}
