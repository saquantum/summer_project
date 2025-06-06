package com.ourrainwater.controller;

import com.ourrainwater.pojo.Asset;
import com.ourrainwater.pojo.AssetHolder;
import com.ourrainwater.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api")
@CrossOrigin
public class CrudController {

    @Autowired
    private SqlService sqlService;

    @GetMapping("/asset")
    public ResponseResult getAllAssets(){
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssets());
    }

    @GetMapping("/asset/{id}")
    public ResponseResult getAssetsById(@PathVariable Integer id){
        return new ResponseResult(Code.SELECT_OK, sqlService.selectByAsset(new Asset(id)));
    }

    @GetMapping("/holder")
    public ResponseResult getAllAssetHolders(){
        return new ResponseResult(Code.SELECT_OK, sqlService.selectAllAssetHolders());
    }

    @GetMapping("/holder/{id}")
    public ResponseResult getAssetHolderByName(@PathVariable Integer id){
        return new ResponseResult(Code.SELECT_OK, sqlService.selectByAssetHolder(new AssetHolder(id)));
    }

    @PostMapping("/asset")
    public ResponseResult insertAsset(@RequestBody Asset asset){
        return new ResponseResult(Code.INSERT_OK, sqlService.insertAsset(asset));
    }

    @PostMapping("/holder")
    public ResponseResult insertAssetHolder(@RequestBody AssetHolder assetHolder){
        return new ResponseResult(Code.INSERT_OK, sqlService.insertAssetHolder(assetHolder));
    }

    @PutMapping("/asset")
    public ResponseResult updateAsset(@RequestBody Asset asset){
        return new ResponseResult(Code.UPDATE_OK, sqlService.updateAsset(asset));
    }

    @PutMapping("/holder")
    public ResponseResult updateAssetHolder(@RequestBody AssetHolder assetHolder){
        System.out.println(assetHolder);
        return new ResponseResult(Code.UPDATE_OK, sqlService.updateAssetHolder(assetHolder));
    }

    @DeleteMapping("/asset")
    public ResponseResult deleteByAsset(@RequestBody Asset asset){
        return new ResponseResult(Code.DELETE_OK, sqlService.deleteByAsset(asset));
    }

    @DeleteMapping("/holder")
    public ResponseResult deleteByAssetHolder(@RequestBody AssetHolder assetHolder){
        return new ResponseResult(Code.DELETE_OK, sqlService.deleteByAssetHolder(assetHolder));
    }
}
