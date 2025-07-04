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

    @GetMapping("/user/uid/{id}/asset")
    public ResponseBody getMyAssetsByUserId(HttpServletResponse response,
                                            HttpServletRequest request,
                                            @PathVariable String id,
                                            @RequestParam(required = false) List<String> orderList,
                                            @RequestParam(required = false) Integer limit,
                                            @RequestParam(required = false) Integer offset) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByUserId(id, null, null, null);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(user.getAssetHolderId(), QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/user/aid/{id}/asset")
    public ResponseBody getMyAssetsByAssetHolderId(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @PathVariable String id,
                                                   @RequestParam(required = false) List<String> orderList,
                                                   @RequestParam(required = false) Integer limit,
                                                   @RequestParam(required = false) Integer offset) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(id, QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/admin/user/uid/{id}/asset")
    public ResponseBody getAllAssetsOfHolderByUserId(@PathVariable String id,
                                                     @RequestParam(required = false) List<String> orderList,
                                                     @RequestParam(required = false) Integer limit,
                                                     @RequestParam(required = false) Integer offset) {
        User user = userService.getUserByUserId(id, null, null, null);
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(user.getAssetHolderId(), QueryTool.getOrderList(orderList), limit, offset);
        return new ResponseBody(Code.SELECT_OK, assets);
    }

    @GetMapping("/admin/user/aid/{id}/asset")
    public ResponseBody getAllAssetsOfHolderByAssetHolderId(@PathVariable String id,
                                                            @RequestParam(required = false) List<String> orderList,
                                                            @RequestParam(required = false) Integer limit,
                                                            @RequestParam(required = false) Integer offset) {
        List<AssetWithWeatherWarnings> assets = assetService.getAllAssetsWithWarningsByAssetHolderId(id, QueryTool.getOrderList(orderList), limit, offset);
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
        if(!verifyAssetOwnership(assetId, uid, null)){
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
        if(!verifyAssetOwnership(assetId, null, aid)){
            throw new SpExceptions.GetMethodException("Asset owner identification failed");
        }
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(assetId));
    }

    @PostMapping("/user/uid/{id}/asset")
    public ResponseBody insertAssetWithUserId(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @PathVariable String id,
                                              @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PostMapping("/user/aid/{id}/asset")
    public ResponseBody insertAssetWithAssetHolderId(HttpServletResponse response,
                                                     HttpServletRequest request,
                                                     @PathVariable String id,
                                                     @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PostMapping("/admin/asset")
    public ResponseBody insertAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PutMapping("/user/uid/{id}/asset")
    public ResponseBody updateAssetWithUserId(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @PathVariable String id,
                                              @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @PutMapping("/user/aid/{id}/asset")
    public ResponseBody updateAssetWithAssetHolderId(HttpServletResponse response,
                                                     HttpServletRequest request,
                                                     @PathVariable String id,
                                                     @RequestBody Asset asset) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @PutMapping("/admin/asset")
    public ResponseBody updateAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @DeleteMapping("/user/uid/{id}/asset")
    public ResponseBody deleteAssetsWithUserIdByIds(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @PathVariable String id,
                                                    @RequestBody Map<String, Object> body) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    @DeleteMapping("/user/aid/{id}/asset")
    public ResponseBody deleteAssetsWithAssetHolderIdByIds(HttpServletResponse response,
                                                           HttpServletRequest request,
                                                           @PathVariable String id,
                                                           @RequestBody Map<String, Object> body) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        List<String> ids = (List<String>) body.get("ids");
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
        return new ResponseBody(Code.INSERT_OK, assetService.insertAssetType(type));
    }

    @PutMapping("/admin/asset/type")
    public ResponseBody updateAssetType(@RequestBody AssetType type) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAssetType(type));
    }

    @DeleteMapping("/admin/asset/type")
    public ResponseBody deleteByAssetTypesByIds(@RequestBody Map<String, Object> body) {
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

    private boolean verifyAssetOwnership(String assetId, String uid, String aid){
        if(assetId == null){
            return false;
        }
        if(uid == null && aid == null){
            return false;
        }
        List<Asset> asset = assetService.getAssetById(assetId);
        if(asset.size() != 1) return false;
        if(uid != null){
            User user = userService.getUserByUserId(uid, null, null, null);
            if(user.getAssetHolder() == null) return false;
            if(!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        if(aid != null){
            User user = userService.getUserByAssetHolderId(aid, null, null, null);
            if(user.getAssetHolder() == null) return false;
            if(!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        return true;
    }
}
