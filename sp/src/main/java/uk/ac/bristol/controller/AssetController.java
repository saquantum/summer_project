package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AssetController {
    private final UserService userService;
    private final AssetService assetService;

    public AssetController(UserService userService, AssetService assetService) {
        this.userService = userService;
        this.assetService = assetService;
    }

    @UserIdentificationUIDExecution
    @GetMapping("/user/uid/{uid}/asset")
    public ResponseBody getMyAssetsByUID(HttpServletResponse response,
                                             HttpServletRequest request,
                                             @UserUID @PathVariable String uid,
                                             @RequestParam(required = false) List<String> orderList,
                                             @RequestParam(required = false) Integer limit,
                                             @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        User user = userService.getUserByUserId(uid);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(
                user.getAssetHolderId(),
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset);
        return new ResponseBody(Code.SELECT_OK, assets, message);
    }

    @UserIdentificationAIDExecution
    @GetMapping("/user/aid/{aid}/asset")
    public ResponseBody getMyAssetsByAID(HttpServletResponse response,
                                             HttpServletRequest request,
                                             @UserAID @PathVariable String aid,
                                             @RequestParam(required = false) List<String> orderList,
                                             @RequestParam(required = false) Integer limit,
                                             @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(
                aid,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset);
        return new ResponseBody(Code.SELECT_OK, assets, message);
    }

    @GetMapping("/admin/user/uid/{uid}/asset")
    public ResponseBody getAllAssetsOfHolderByUserId(@PathVariable String uid,
                                                     @RequestParam(required = false) List<String> orderList,
                                                     @RequestParam(required = false) Integer limit,
                                                     @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        User user = userService.getUserByUserId(uid);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(
                user.getAssetHolderId(),
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset);
        return new ResponseBody(Code.SELECT_OK, assets, message);
    }

    @GetMapping("/admin/user/aid/{aid}/asset")
    public ResponseBody getAllAssetsOfHolderByAssetHolderId(@PathVariable String aid,
                                                            @RequestParam(required = false) List<String> orderList,
                                                            @RequestParam(required = false) Integer limit,
                                                            @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(
                aid,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset);
        return new ResponseBody(Code.SELECT_OK, assets, message);
    }

    @UserIdentificationUIDExecution
    @AssetOwnershipUIDExecution
    @GetMapping("/user/uid/{uid}/asset/{assetId}")
    public ResponseBody getMyAssetByAssetIdWithUID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserUID @PathVariable String uid,
                                                   @UserAssetId @PathVariable String assetId) {
        return new ResponseBody(Code.SELECT_OK, List.of(assetService.getAssetWithWarningsById(assetId)));
    }

    @UserIdentificationAIDExecution
    @AssetOwnershipAIDExecution
    @GetMapping("/user/aid/{aid}/asset/{assetId}")
    public ResponseBody getMyAssetByAssetIdWithAID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserAID @PathVariable String aid,
                                                   @UserAssetId @PathVariable String assetId) {
        return new ResponseBody(Code.SELECT_OK, List.of(assetService.getAssetWithWarningsById(assetId)));
    }

    @UserIdentificationUIDExecution
    @AssetOwnershipUIDExecution
    @RequestMapping(value = "/user/uid/{uid}/asset/{assetId}", method = RequestMethod.HEAD)
    public void headMyAssetLastModifiedByAssetIdWithUID(HttpServletResponse response,
                                                        HttpServletRequest request,
                                                        @UserUID @PathVariable String uid,
                                                        @UserAssetId @PathVariable String assetId,
                                                        @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = assetService.compareAssetLastModified(assetId, timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @UserIdentificationAIDExecution
    @AssetOwnershipAIDExecution
    @RequestMapping(value = "/user/aid/{aid}/asset/{assetId}", method = RequestMethod.HEAD)
    public void headMyAssetLastModifiedByAssetIdWithAID(HttpServletResponse response,
                                                        HttpServletRequest request,
                                                        @UserAID @PathVariable String aid,
                                                        @UserAssetId @PathVariable String assetId,
                                                        @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = assetService.compareAssetLastModified(assetId, timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @RequestMapping(value = "/admin/asset/{assetId}", method = RequestMethod.HEAD)
    public void headAssetLastModified(HttpServletResponse response,
                                      HttpServletRequest request,
                                      @UserAssetId @PathVariable String assetId,
                                      @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = assetService.compareAssetLastModified(assetId, timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @UserIdentificationUIDExecution
    @PostMapping("/user/uid/{uid}/asset")
    public ResponseBody insertAssetWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid,
                                               @RequestBody Asset asset) {
        if (!QueryTool.getUserPermissions(uid, null).getCanCreateAsset()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to insert asset.");
        }
        asset.setId(null);
        if (!QueryTool.getUserPermissions(uid, null).getCanSetPolygonOnCreate()) {
            asset.clearLocation();
            return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset), "The asset is successfully inserted but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @UserIdentificationAIDExecution
    @PostMapping("/user/aid/{aid}/asset")
    public ResponseBody insertAssetWithAID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserAID @PathVariable String aid,
                                               @RequestBody Asset asset) {
        if (!QueryTool.getUserPermissions(null, aid).getCanCreateAsset()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to insert asset");
        }
        asset.setId(null);
        if (!QueryTool.getUserPermissions(null, aid).getCanSetPolygonOnCreate()) {
            asset.clearLocation();
            return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset), "The asset is successfully inserted but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @GetMapping("/admin/asset")
    public ResponseBody getAllAssetsWithWarnings(@RequestParam(required = false) List<String> orderList,
                                                 @RequestParam(required = false) Integer limit,
                                                 @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/admin/asset/search")
    public ResponseBody getAllAssetsWithWarnings(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/admin/asset/{assetId}")
    public ResponseBody getAssetById(@PathVariable String assetId) {
        return new ResponseBody(Code.SELECT_OK, List.of(assetService.getAssetWithWarningsById(assetId)));
    }

    @PostMapping("/admin/asset/count")
    public ResponseBody countAssetByFilter(@RequestBody FilterDTO filter) {
        return new ResponseBody(Code.SELECT_OK, assetService.countAssetsWithFilter(filter.getFilters()));
    }

    @PostMapping("/admin/asset")
    public ResponseBody insertAsset(@RequestBody Asset asset) {
        asset.setId(null);
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @UserIdentificationUIDExecution
    @AssetOwnershipUIDExecution
    @PutMapping("/user/uid/{uid}/asset")
    public ResponseBody updateMyAssetWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid,
                                               @UserAsset @RequestBody Asset asset) {
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateAssetFields()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update asset.");
        }
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateAssetPolygon()) {
            asset.clearLocation();
            return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset), "The asset is successfully updated but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @UserIdentificationAIDExecution
    @AssetOwnershipAIDExecution
    @PutMapping("/user/aid/{aid}/asset")
    public ResponseBody updateMyAssetWithAID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserAID @PathVariable String aid,
                                               @UserAsset @RequestBody Asset asset) {
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateAssetFields()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update asset.");
        }
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateAssetPolygon()) {
            asset.clearLocation();
            return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset), "The asset is successfully updated but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @PutMapping("/admin/asset")
    public ResponseBody updateAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @UserIdentificationUIDExecution
    @DeleteMapping("/user/uid/{uid}/asset")
    public ResponseBody deleteMyAssetsByAssetIdsWithUID(HttpServletResponse response,
                                                          HttpServletRequest request,
                                                          @UserUID @PathVariable String uid,
                                                          @RequestBody Map<String, Object> body) {
        if (!QueryTool.getUserPermissions(uid, null).getCanDeleteAsset()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to delete asset.");
        }

        List<String> ids = (List<String>) body.get("ids");
        for (String s : ids) {
            if (!QueryTool.verifyAssetOwnership(s, uid, null)) {
                throw new SpExceptions.ForbiddenException("Asset owner identification failed: " + s + " does not belong to current user");
            }
        }
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    @UserIdentificationAIDExecution
    @DeleteMapping("/user/aid/{aid}/asset")
    public ResponseBody deleteMyAssetsByAssetIdsWithAID(HttpServletResponse response,
                                                          HttpServletRequest request,
                                                          @UserAID @PathVariable String aid,
                                                          @RequestBody Map<String, Object> body) {
        if (!QueryTool.getUserPermissions(null, aid).getCanDeleteAsset()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to delete asset.");
        }

        List<String> ids = (List<String>) body.get("ids");
        for (String s : ids) {
            if (!QueryTool.verifyAssetOwnership(s, null, aid)) {
                throw new SpExceptions.ForbiddenException("Asset owner identification failed");
            }
        }
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    @DeleteMapping("/admin/asset")
    public ResponseBody deleteAssetsByIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    /* ---------------- Asset Types ---------------- */

    @GetMapping("/asset/type")
    public ResponseBody getAllAssetTypes(@RequestParam(required = false) List<String> orderList,
                                         @RequestParam(required = false) Integer limit,
                                         @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetTypes(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/asset/type/search")
    public ResponseBody getAllAssetTypes(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetTypes(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @PostMapping("/admin/asset/type")
    public ResponseBody insertAssetType(@RequestBody AssetType type) {
        type.setId(null);
        return new ResponseBody(Code.INSERT_OK, assetService.insertAssetType(type));
    }

    @PutMapping("/admin/asset/type")
    public ResponseBody updateAssetType(@RequestBody AssetType type) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAssetType(type));
    }

    @DeleteMapping("/admin/asset/type")
    public ResponseBody deleteAssetTypesByIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetTypeByIDs(ids));
    }
}
