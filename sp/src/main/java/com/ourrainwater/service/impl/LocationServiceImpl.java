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

    @Override
    public List<Location> getByLocation(Location location) {
        return locationMapper.selectByLocation(location);
    }

    @Override
    public int insert(Location location) {
        return locationMapper.insert(location);
    }

    @Override
    public int update(Location location) {
        return locationMapper.update(location);
    }

    @Override
    public int deleteByLocation(Location location) {
        List<Integer> ids = locationMapper.selectByLocation(location).stream()
                .map(Location::getId)
                .toList();
        if (ids.isEmpty()) return 0;
        return locationMapper.deleteByIDs(ids);
    }
}
