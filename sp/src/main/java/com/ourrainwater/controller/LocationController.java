package com.ourrainwater.controller;

import com.ourrainwater.pojo.Location;
import com.ourrainwater.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseResult insert(@RequestBody Location location){
        return new ResponseResult(Code.INSERT_OK, locationService.insert(location));
    }

    @PutMapping
    public ResponseResult update(@RequestBody Location location){
        return new ResponseResult(Code.UPDATE_OK, locationService.update(location));
    }

    @DeleteMapping
    public ResponseResult deleteByLocation(@RequestBody Location location){
        return new ResponseResult(Code.DELETE_OK, locationService.deleteByLocation(location));
    }
}
