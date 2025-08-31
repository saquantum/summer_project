package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface WarningService {

    List<Warning> getWarnings(Map<String, Object> filters,
                              List<Map<String, String>> orderList,
                              Integer limit,
                              Integer offset);

    List<Warning> getCursoredWarnings(Long lastWarningRowId,
                                      Map<String, Object> filters,
                                      List<Map<String, String>> orderList,
                                      Integer limit,
                                      Integer offset);

    List<Warning> getWarningsIncludingOutdated(Map<String, Object> filters,
                                               List<Map<String, String>> orderList,
                                               Integer limit,
                                               Integer offset);

    List<Warning> getCursoredWarningsIncludingOutdated(Long lastWarningRowId,
                                                       Map<String, Object> filters,
                                                       List<Map<String, String>> orderList,
                                                       Integer limit,
                                                       Integer offset);

    Warning getWarningById(Long id);

    List<Warning> getWarningsIntersectingWithGivenAsset(String assetId);

    boolean storeWarningsAndSendNotifications(List<Warning> parsedWarnings);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(Long[] ids);

    int deleteWarningByIDs(List<Long> ids);

    // UK Regions

    String getRegionNameGivenAsset(Asset asset);

    int insertUkRegion(Map<String, String> region);
}
