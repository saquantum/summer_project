package com.ourrainwater.controller;

import com.ourrainwater.pojo.Location;
import com.ourrainwater.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public ResponseResult getAllLocations() {
        return new ResponseResult(Code.SELECT_OK, locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseResult getAllLocationsByID(@PathVariable Integer id) {
        return new ResponseResult(Code.SELECT_OK, locationService.getLocationByID(id));
    }
}
