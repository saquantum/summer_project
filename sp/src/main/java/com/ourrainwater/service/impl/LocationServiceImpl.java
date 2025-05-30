package com.ourrainwater.service.impl;

import com.ourrainwater.dao.LocationMapper;
import com.ourrainwater.pojo.Location;
import com.ourrainwater.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationMapper locationMapper;

    @Override
    public List<Location> getAllLocations() {
        return locationMapper.selectAllLocations();
    }

    @Override
    public List<Location> getLocationByID(Integer id) {
        return locationMapper.selectLocationByID(id);
    }
}
