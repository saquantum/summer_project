package com.ourrainwater.dao;

import com.ourrainwater.pojo.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LocationMapper {

    public List<Location> selectAllLocations();

    public List<Location> selectLocationByID(Integer id);

    public List<Location> insert(Location location);

    public List<Location> update(Location location);

    public List<Location> deleteByID(Integer id);

    public List<Location> deleteByIDs(Integer[] ids);
    public List<Location> deleteByIDs(List<Integer> ids);
}
