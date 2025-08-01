package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WarningServiceImpl implements WarningService {

    private final MetaDataMapper metaDataMapper;
    private final WarningMapper warningMapper;
    private final UserService userService;
    private final ContactService contactService;

    public WarningServiceImpl(MetaDataMapper metaDataMapper, WarningMapper warningMapper, UserService userService, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.warningMapper = warningMapper;
        this.userService = userService;
        this.contactService = contactService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Warning> getAllWarnings(Map<String, Object> filters,
                                        List<Map<String, String>> orderList,
                                        Integer limit,
                                        Integer offset) {
        return warningMapper.selectAllWarnings(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "weather_warnings"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Warning> getAllWarningsIncludingOutdated(Map<String, Object> filters,
                                                         List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset) {
        return warningMapper.selectAllWarningsIncludingOutdated(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "weather_warnings"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Warning> getWarningById(Long id) {
        return warningMapper.selectWarningById(id);
    }

    @Override
    public List<Warning> getWarningsIntersectingWithGivenAsset(String assetId) {
        return warningMapper.selectWarningsIntersectingWithGivenAsset(assetId);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public boolean storeWarningsAndSendNotifications(List<Warning> parsedWarnings) {
        List<Warning> warningsToNotify = new ArrayList<>();

        int s = 0;
        for (Warning warning : parsedWarnings) {
            s++;
            // 1. new warning: send notifications
            if (!warningMapper.testWarningExists(warning.getId())) {
                warningsToNotify.add(warning);
                warningMapper.insertWarning(warning);
                metaDataMapper.increaseTotalCountByTableName("weather_warnings", 1);
            }
            // 2. warning details updated: send notifications
            else if (warningMapper.testWarningDetailDiff(warning)) {
                warningsToNotify.add(warning);
                warningMapper.updateWarning(warning);
            }
            // 3. warning area updated: send notifications to assets not intersecting with it beforehand
            else if (warningMapper.testWarningAreaDiff(warning.getId(), warning.getAreaAsJson())) {
                handleGroupedUsersWithRespectToPagination(warning, true);
            } else {
                s--;
            }
        }

        System.out.println("Successfully inserted or updated " + s + " weather warning records at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // send notifications for case 1 and 2 here
        if (!warningsToNotify.isEmpty()) {
            for (Warning warning : warningsToNotify) {
                handleGroupedUsersWithRespectToPagination(warning, false);
            }
            return true;
        }
        return false;
    }

    private void handleGroupedUsersWithRespectToPagination(Warning warning, boolean getDiff) {
        int limit = Code.PAGINATION_MAX_LIMIT;
        long cursor = 0L;
        int length = 0;
        do {
            List<UserWithAssets> list = userService.groupUsersWithOwnedAssetsByWarningId(limit, cursor, warning.getId(), getDiff, warning.getAreaAsJson());
            length = list.size();
            if(length == 0) break;
            for (UserWithAssets uwa : list) {
                contactService.sendNotificationsToUser(warning, uwa);
            }
            cursor = userService.getUserRowIdByUserId(list.get(list.size() - 1).getUser().getId());
        } while (length > 0);
        if (cursor == 0L) {
            if (getDiff) {
                System.out.println("No newly intersecting assets with updated warning " + warning.getId() + ", no notification has been sent.");
            } else {
                System.out.println("No users has asset intersecting with new warning " + warning.getId() + ", no notification has been sent.");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertWarning(Warning warning) {
        int n = warningMapper.insertWarning(warning);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertWarningsList(List<Warning> warnings) {
        if (warnings.isEmpty()) return 0;
        int sum = 0;
        for (Warning warning : warnings) {
            List<Warning> search = warningMapper.selectWarningById(warning.getId());
            if (search.size() > 1) {
                throw new SpExceptions.SystemException("Found" + search.size() + " weather warning data stored in database for id " + warning.getId());
            } else if (search.size() == 1) {
                warningMapper.updateWarning(warning);
            } else {
                warningMapper.insertWarning(warning);
                sum++;
            }
        }
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", sum);
        return sum;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateWarning(Warning warning) {
        return warningMapper.updateWarning(warning);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteWarningByIDs(Long[] ids) {
        return warningMapper.deleteWarningByIDs(ids);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteWarningByIDs(List<Long> ids) {
        int n = warningMapper.deleteWarningByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", -n);
        return n;
    }
}
