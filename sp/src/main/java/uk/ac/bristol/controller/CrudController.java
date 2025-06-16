package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.service.SqlService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api")
@CrossOrigin
public class CrudController {

    @Autowired
    private SqlService sqlService;

    @GetMapping("/me")
    public ResponseResult getUserInfo(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String token = JwtUtil.getJWTFromCookie(request, response);
        Claims claims = JwtUtil.parseJWT(token);
        Map<String, Object> data = new HashMap<>();
        boolean isAdmin = claims.get("isAdmin", Boolean.class);
        data.put("isAdmin", isAdmin);
        // for user, must get asset holder id, or something has gone wrong
        if(!isAdmin){
            data.put("id", claims.get("assetHolderId", Integer.class));
            return new ResponseResult(Code.SUCCESS, data);
        }
        // for admin, two possibilities:
        else{
            // 1. with proxy id
            if(claims.containsKey("asUserId")){
                data.put("id", claims.get("asUserId", Integer.class));
                if(claims.containsKey("asUserInAssetId")){
                    data.put("asUserInAssetId", claims.get("asUserInAssetId", Integer.class));
                }
                return new ResponseResult(Code.SUCCESS, data);
            }
            // 2. without proxy id
            else{
                data.put("id", -1);
                return new ResponseResult(Code.SUCCESS, data);
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseResult getUserByAssetHolderId(@PathVariable Integer id){
        return new ResponseResult(Code.SELECT_OK, sqlService.selectUserByAssetHolderId(id));
    }

    @GetMapping("/warning")
    public ResponseResult getAllWarnings() {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllWarnings());
    }

    @GetMapping("/asset")
    public ResponseResult getAllAssets() {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssets());
    }

    @GetMapping("/asset/{id}")
    public ResponseResult getAssetsById(@PathVariable Integer id) {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectByAsset(new Asset(id)));
    }

    @GetMapping("holder/{id}/asset")
    public ResponseResult getAllAssetsOfHolder(@PathVariable Integer id) {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssetsOfHolder(id));
    }

    @GetMapping("/holder")
    public ResponseResult getAllAssetHolders() {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssetHolders());
    }

    @GetMapping("/holder/{id}")
    public ResponseResult getAssetHolderByName(@PathVariable Integer id) {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectByAssetHolder(new AssetHolder(id)));
    }

    @PostMapping("/asset")
    public ResponseResult insertAsset(@RequestBody Asset asset) {
        return new ResponseResult(Code.INSERT_OK, sqlService.insertAsset(asset));
    }

    @PostMapping("/holder")
    public ResponseResult insertAssetHolder(@RequestBody AssetHolder assetHolder) {
        return new ResponseResult(Code.INSERT_OK, sqlService.insertAssetHolder(assetHolder));
    }

    @PutMapping("/asset")
    public ResponseResult updateAsset(@RequestBody Asset asset) {
        return new ResponseResult(Code.UPDATE_OK, sqlService.updateAsset(asset));
    }

    @PutMapping("/holder")
    public ResponseResult updateAssetHolder(@RequestBody AssetHolder assetHolder) {
        return new ResponseResult(Code.UPDATE_OK, sqlService.updateAssetHolder(assetHolder));
    }

    @DeleteMapping("/asset")
    public ResponseResult deleteByAsset(@RequestBody Asset asset) {
        return new ResponseResult(Code.DELETE_OK, sqlService.deleteByAsset(asset));
    }

    @DeleteMapping("/holder")
    public ResponseResult deleteByAssetHolder(@RequestBody AssetHolder assetHolder) {
        return new ResponseResult(Code.DELETE_OK, sqlService.deleteByAssetHolder(assetHolder));
    }
}
