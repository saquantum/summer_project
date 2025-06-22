package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Warning;

import java.util.List;

public interface WarningService {

    List<Warning> getAllWarnings();

    List<Warning> getAllWarningsIncludingOutdated();

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(Long[] ids);
}
