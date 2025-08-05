package uk.ac.bristol.service;

import uk.ac.bristol.pojo.User;

import java.util.List;
import java.util.Map;

public interface PostcodeService {

    Map<String, String> getRandomPostcode();

    Map<String, String> getColumnsOfPostcode(String postcode);

    Map<String, List<User>> groupUserAddressPostcodeByCountry(Map<String, Object> filters);

    Map<String, List<User>> groupUserAddressPostcodeByRegion(Map<String, Object> filters);

    Map<String, List<User>> groupUserAddressPostcodeByAdminDistrict(Map<String, Object> filters);
}