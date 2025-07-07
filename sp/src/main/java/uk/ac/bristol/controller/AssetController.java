package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AssetController {
    private final UserService userService;
    private final AssetService assetService;
    private final WarningService warningService;

    public AssetController(UserService userService, AssetService assetService, WarningService warningService) {
        this.userService = userService;
        this.assetService = assetService;
        this.warningService = warningService;
    }

    @GetMapping("/user/uid/{uid}/asset")
    public ResponseBody getMyAssetsByUserId(HttpServletResponse response,
                                            HttpServletRequest request,
                                            @PathVariable String uid,
                                            @RequestParam(required = false) List<String> orderList,
                                            @RequestParam(required = false) Integer limit,
                                            @RequestParam(required = false) Integer offset) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByUserId(uid);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(user.getAssetHolderId(), QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/user/aid/{aid}/asset")
    public ResponseBody getMyAssetsByAssetHolderId(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @PathVariable String aid,
                                                   @RequestParam(required = false) List<String> orderList,
                                                   @RequestParam(required = false) Integer limit,
                                                   @RequestParam(required = false) Integer offset) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(aid, QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/admin/user/uid/{uid}/asset")
    public ResponseBody getAllAssetsOfHolderByUserId(@PathVariable String uid,
                                                     @RequestParam(required = false) List<String> orderList,
                                                     @RequestParam(required = false) Integer limit,
                                                     @RequestParam(required = false) Integer offset) {
        User user = userService.getUserByUserId(uid);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(user.getAssetHolderId(), QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/admin/user/aid/{aid}/asset")
    public ResponseBody getAllAssetsOfHolderByAssetHolderId(@PathVariable String aid,
                                                            @RequestParam(required = false) List<String> orderList,
                                                            @RequestParam(required = false) Integer limit,
                                                            @RequestParam(required = false) Integer offset) {
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(aid, QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/user/uid/{uid}/asset/{assetId}")
    public ResponseBody getAssetWithUserIdById(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @PathVariable String uid,
                                               @PathVariable String assetId) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        if (!verifyAssetOwnership(assetId, uid, null)) {
            throw new SpExceptions.GetMethodException("Asset owner identification failed");
        }
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(assetId));
    }

    @GetMapping("/user/aid/{aid}/asset/{assetId}")
    public ResponseBody getAssetWithAssetHolderIdById(HttpServletResponse response,
                                                      HttpServletRequest request,
                                                      @PathVariable String aid,
                                                      @PathVariable String assetId) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        if (!verifyAssetOwnership(assetId, null, aid)) {
            throw new SpExceptions.GetMethodException("Asset owner identification failed");
        }
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(assetId));
    }

    @PostMapping("/user/uid/{uid}/asset")
    public ResponseBody insertAssetWithUserId(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @PathVariable String uid,
                                              @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        if (!QueryTool.getUserPermissions(uid, null).getCanCreateAsset()) {
            throw new SpExceptions.PostMethodException("The user is not allowed to insert asset.");
        }
        asset.setId(null);
        if (!QueryTool.getUserPermissions(uid, null).getCanSetPolygonOnCreate()) {
            asset.setLocationAsJson(null);
            return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset), "The asset is successfully inserted but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PostMapping("/user/aid/{aid}/asset")
    public ResponseBody insertAssetWithAssetHolderId(HttpServletResponse response,
                                                     HttpServletRequest request,
                                                     @PathVariable String aid,
                                                     @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        if (!QueryTool.getUserPermissions(null, aid).getCanCreateAsset()) {
            throw new SpExceptions.PostMethodException("The user is not allowed to insert asset");
        }
        asset.setId(null);
        if (!QueryTool.getUserPermissions(null, aid).getCanSetPolygonOnCreate()) {
            asset.setLocationAsJson(null);
            return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset), "The asset is successfully inserted but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PostMapping("/admin/asset")
    public ResponseBody insertAsset(@RequestBody Asset asset) {
        asset.setId(null);
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PutMapping("/user/uid/{uid}/asset")
    public ResponseBody updateAssetWithUserId(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @PathVariable String uid,
                                              @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        if (!verifyAssetOwnership(asset.getId(), uid, null)) {
            throw new SpExceptions.PutMethodException("Asset owner identification failed");
        }
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateAssetFields()) {
            throw new SpExceptions.PutMethodException("The user is not allowed to update asset.");
        }
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateAssetPolygon()) {
            asset.setLocationAsJson(null);
            return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset), "The asset is successfully updated but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @PutMapping("/user/aid/{aid}/asset")
    public ResponseBody updateAssetWithAssetHolderId(HttpServletResponse response,
                                                     HttpServletRequest request,
                                                     @PathVariable String aid,
                                                     @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        if (!verifyAssetOwnership(asset.getId(), null, aid)) {
            throw new SpExceptions.PutMethodException("Asset owner identification failed");
        }
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateAssetFields()) {
            throw new SpExceptions.PutMethodException("The user is not allowed to update asset.");
        }
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateAssetPolygon()) {
            asset.setLocationAsJson(null);
            return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset), "The asset is successfully updated but without polygon since the user is not allowed to do so.");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @PutMapping("/admin/asset")
    public ResponseBody updateAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @DeleteMapping("/user/uid/{uid}/asset")
    public ResponseBody deleteAssetsWithUserIdByIds(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @PathVariable String uid,
                                                    @RequestBody Map<String, Object> body) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        if (!QueryTool.getUserPermissions(uid, null).getCanDeleteAsset()) {
            throw new SpExceptions.DeleteMethodException("The user is not allowed to delete asset.");
        }

        List<String> ids = (List<String>) body.get("ids");
        for (String s : ids) {
            if (!verifyAssetOwnership(s, uid, null)) {
                throw new SpExceptions.DeleteMethodException("Asset owner identification failed: " + s + " does not belong to current user");
            }
        }
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    @DeleteMapping("/user/aid/{aid}/asset")
    public ResponseBody deleteAssetsWithAssetHolderIdByIds(HttpServletResponse response,
                                                           HttpServletRequest request,
                                                           @PathVariable String aid,
                                                           @RequestBody Map<String, Object> body) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        if (!QueryTool.getUserPermissions(null, aid).getCanDeleteAsset()) {
            throw new SpExceptions.DeleteMethodException("The user is not allowed to delete asset.");
        }

        List<String> ids = (List<String>) body.get("ids");
        for (String s : ids) {
            if (!verifyAssetOwnership(s, null, aid)) {
                throw new SpExceptions.DeleteMethodException("Asset owner identification failed");
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
    public ResponseBody getAllAssetsTypes(@RequestParam(required = false) List<String> orderList,
                                          @RequestParam(required = false) Integer limit,
                                          @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetTypes(QueryTool.getOrderList(orderList), limit, offset));
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

    /* ---------------- Warnings ---------------- */

    @GetMapping("/warning")
    public ResponseBody getAllLiveWarnings(@RequestParam(required = false) List<String> orderList,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarnings(QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/admin/warning/all")
    public ResponseBody getAllWarningsIncludingOutdated(@RequestParam(required = false) List<String> orderList,
                                                        @RequestParam(required = false) Integer limit,
                                                        @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarningsIncludingOutdated(QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/warning/{id}")
    public ResponseBody getWarningById(@PathVariable Long id) {
        return new ResponseBody(Code.SELECT_OK, warningService.getWarningById(id));
    }

    // NOTICE: no post or put mapping for warnings, since they should be handled by the crawler.

    @DeleteMapping("/admin/warning")
    public ResponseBody deleteWarningsByIds(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, warningService.deleteWarningByIDs(ids));
    }

    // TODO: move this method to QueryTools
    private boolean verifyAssetOwnership(String assetId, String uid, String aid) {
        if (assetId == null) {
            return false;
        }
        if (uid == null && aid == null) {
            return false;
        }
        List<Asset> asset = assetService.getAssetById(assetId);
        if (asset.size() != 1) return false;
        if (uid != null) {
            User user = userService.getUserByUserId(uid);
            if (user.getAssetHolder() == null) return false;
            if (!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        if (aid != null) {
            User user = userService.getUserByAssetHolderId(aid, null, null, null);
            if (user.getAssetHolder() == null) return false;
            if (!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        return true;
    }
}
