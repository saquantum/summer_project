package com.ourrainwater.service;

import com.ourrainwater.pojo.Location;

import java.util.List;

public interface LocationService {
    List<Location> getAllLocations();

    List<Location> getLocationByID(Integer id);
}
