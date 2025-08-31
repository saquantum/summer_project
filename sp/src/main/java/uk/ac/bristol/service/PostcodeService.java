package uk.ac.bristol.service;

import org.locationtech.jts.geom.Point;

import java.util.Map;

public interface PostcodeService {

    Map<String, Object> getRandomPostcode();

    Map<String, String> getRandomPostcodeAddress();

    Map<String, String> getColumnsOfPostcode(String postcode);

    Map<String, Object> getColumnsOfGeometricPoint(Point point);
}