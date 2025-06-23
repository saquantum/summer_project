package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.WarningService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AssetController {
    private final AssetService assetService;
    private final WarningService warningService;

    public AssetController(AssetService assetService, WarningService warningService) {
        this.assetService = assetService;
        this.warningService = warningService;
    }

    @GetMapping("/asset")
    public ResponseBody getAllAssetsWithWarnings() {
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings());
    }

    @GetMapping("/asset/{id}")
    public ResponseBody getAssetsById(@PathVariable String id) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(id));
    }

    @GetMapping("/asset/holder/{id}")
    public ResponseBody getAllAssetsOfHolder(@PathVariable String id) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarningsByAssetHolderId(id));
    }

    @PostMapping("/asset")
    public ResponseBody insertAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.INSERT_OK, assetService.insertAsset(asset));
    }

    @PutMapping("/asset")
    public ResponseBody updateAsset(@RequestBody Asset asset) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAsset(asset));
    }

    @DeleteMapping("/asset")
    public ResponseBody deleteByAsset(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetByIDs(ids));
    }

    /* ---------------- Asset Types ---------------- */

    @GetMapping("/asset")
    public ResponseBody getAllAssetsTypes() {
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings());
    }

    @PostMapping("/asset")
    public ResponseBody insertAssetType(@RequestBody AssetType type) {
        return new ResponseBody(Code.INSERT_OK, assetService.insertAssetType(type));
    }

    @PutMapping("/asset")
    public ResponseBody updateAssetType(@RequestBody AssetType type) {
        return new ResponseBody(Code.UPDATE_OK, assetService.updateAssetType(type));
    }

    @DeleteMapping("/asset")
    public ResponseBody deleteByAssetTypesByIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, assetService.deleteAssetTypeByIDs(ids));
    }

    /* ---------------- Warnings ---------------- */

    @GetMapping("/warning")
    public ResponseBody getAllLiveWarnings() {
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarnings());
    }

    @GetMapping("/warning/all")
    public ResponseBody getAllWarningsIncludingOutdated() {
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarningsIncludingOutdated());
    }

    @GetMapping("/warning/{id}")
    public ResponseBody getWarningById(@PathVariable Long id) {
        return new ResponseBody(Code.SELECT_OK, warningService.getWarningById(id));
    }

    // NOTICE: no post or put mapping for warnings, since they should be handled by the crawler.

    @DeleteMapping("/warning")
    public ResponseBody deleteWarningsByIds(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        return  new ResponseBody(Code.DELETE_OK, warningService.deleteWarningByIDs(ids));
    }
}
