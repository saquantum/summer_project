package com.ourrainwater.dao;

import com.ourrainwater.pojo.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LocationMapper {

    public List<Location> selectAllLocations();

    public List<Location> selectLocationByID(Integer id);

    public List<Location> selectByLocation(Location location);

    public int insert(Location location);

    public int update(Location location);

    public int deleteByIDs(Integer[] ids);
    public int deleteByIDs(List<Integer> ids);
}
