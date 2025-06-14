package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api")
@CrossOrigin
public class CrudController {

    @Autowired
    private SqlService sqlService;

    @GetMapping("/me")
    public ResponseResult getUserInfo(HttpServletResponse response, HttpServletRequest request) throws IOException {
        String token = JwtUtil.getJWTFromCookie(request, response);
        Claims claims = JwtUtil.parseJWT(token);
        Integer id = null;
        try {
            id = claims.get("assetHolderId", Integer.class);
        } catch (Exception ignored) {
        }
        boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("isAdmin", isAdmin);
        return new ResponseResult(Code.SUCCESS, data);
    }

    @GetMapping("/asset")
    public ResponseResult getAllAssets() {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssets());
    }

    @GetMapping("/asset/{id}")
    public ResponseResult getAssetsById(@PathVariable Integer id) {
        return new ResponseResult(Code.SELECT_OK, sqlService.selectByAsset(new Asset(id)));
    }

    @GetMapping("holder/{id}/assets")
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
