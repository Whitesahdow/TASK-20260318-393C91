package com.busapp.repository;

public interface SearchRowProjection {
    Long getStopId();
    String getStopName();
    String getInitials();
    String getRouteNumber();
    Double getScore();
    Double getPopularity();
}
