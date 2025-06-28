package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // get all assets
    // get, update asset

    /* --------- for users and admin proxy --------- */

    @GetMapping()
    public ResponseBody getMyProfile(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        Boolean isAdmin = (Boolean) claims.get("isAdmin");
        if (isAdmin == null) {
            throw new RuntimeException("Failed to parse token, no valid identity found");
        }

        String assetHolderId = (String) claims.get("assetHolderId");
        if (assetHolderId == null) {
            throw new RuntimeException("Failed to parse token, no assetHolderId found");
        }

        User user = userService.getUserByAssetHolderId(assetHolderId, null, null, null);
        if (user == null) {
            throw new RuntimeException("No user is found using asset holder id " + assetHolderId);
        }
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @PutMapping()
    public ResponseBody updateMyProfile(HttpServletResponse response, HttpServletRequest request, @RequestBody User user) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        Boolean isAdmin = (Boolean) claims.get("isAdmin");
        if (isAdmin == null) {
            throw new RuntimeException("Failed to parse token, no valid identity found");
        }

        if (isAdmin) {
            userService.updateUser(user);
            return new ResponseBody(Code.UPDATE_OK, null);
        }

        String id = (String) claims.get("id");
        String assetHolderId = (String) claims.get("assetHolderId");
        if (assetHolderId == null && id == null) {
            throw new RuntimeException("No valid id is found");
        }
        if (id != null) {
            if (!user.getId().equals(id)) {
                throw new RuntimeException("Token id does not match with provided user id.");
            }
            if (user.getAssetHolder() != null && (!user.getAssetHolder().getId().equals(assetHolderId) || !user.getAssetHolderId().equals(assetHolderId))) {
                throw new RuntimeException("Token assetHolder id does not match with provided asset holder id.");
            }
            int n = userService.updateUser(user);
            if (n != 1) {
                throw new RuntimeException("Failed to update user.");
            }
            return new ResponseBody(Code.UPDATE_OK, null);
        }
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @DeleteMapping()
    public ResponseBody deleteMyProfile(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        Boolean isAdmin = (Boolean) claims.get("isAdmin");
        if (isAdmin == null) {
            throw new RuntimeException("Failed to parse token, no valid identity found");
        }

        String assetHolderId = (String) claims.get("assetHolderId");
        if (assetHolderId == null) {
            throw new RuntimeException("No valid id is found");
        }

        userService.deleteUserByAssetHolderIds(new String[]{assetHolderId});
        return new ResponseBody(Code.DELETE_OK, null);
    }

}
