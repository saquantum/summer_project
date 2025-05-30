package com.ourrainwater.service;

import com.ourrainwater.pojo.Location;

import java.util.List;

public interface LocationService {
    List<Location> getAllLocations();

    List<Location> getLocationByID(Integer id);

    int insert(Location location);

    int update(Location location);

    int deleteByLocation(Location location);
}
