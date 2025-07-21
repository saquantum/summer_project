package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface WarningService {

    List<Warning> getAllWarnings(Map<String, Object> filters,
                                 List<Map<String, String>> orderList,
                                 Integer limit,
                                 Integer offset);

    List<Warning> getAllWarningsIncludingOutdated(Map<String, Object> filters,
                                                  List<Map<String, String>> orderList,
                                                  Integer limit,
                                                  Integer offset);

    List<Warning> getWarningById(Long id);

    List<Warning> getWarningsIntersectingWithGivenAsset(String assetId);

    boolean storeWarningsAndSendNotifications(List<Warning> parsedWarnings);

    int insertWarning(Warning warning);

    int insertWarningsList(List<Warning> warnings);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(Long[] ids);

    int deleteWarningByIDs(List<Long> ids);
}
