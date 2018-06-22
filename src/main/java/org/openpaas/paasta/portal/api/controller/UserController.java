package org.openpaas.paasta.portal.api.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.cloudfoundry.client.v2.users.GetUserResponse;
import org.cloudfoundry.uaa.users.User;
import org.cloudfoundry.uaa.users.UserInfoResponse;
import org.openpaas.paasta.portal.api.common.Common;
import org.openpaas.paasta.portal.api.common.Constants;
import org.openpaas.paasta.portal.api.model.UserDetail;
import org.openpaas.paasta.portal.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HeaderParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 유저 컨트롤러 - 마이페이지의 유저의 조회 수정을 처리한다.
 *
 * @author 조민구
 * @version 1.0
 * @since 2016.5.23 최초작성
 */
@RestController
@Transactional
public class UserController extends Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * V1 URL HEAD = (empty string)
     */
    private static final String V1_URL = Constants.V1_URL;

    /**
     * V2 URL HEAD = "/v2"
     */
    private static final String V2_URL = Constants.V2_URL;


    /**
     * Update user password map.
     *
     * @param userId   the user id
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the map
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "updateUserPassword")
    @PutMapping(value = {V2_URL + "/users/{userId}/password/update"})
    public Map updateUserPassword(@PathVariable String userId, @RequestBody Map<String, Object> body, @RequestHeader(AUTHORIZATION_HEADER_KEY) String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("> into updateUserPassword");
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("password");
        Map<String, Object> result = userService.updateUserPassword(userId, oldPassword, newPassword, token);
        return result;
    }


    /**
     * Update user password map.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the map
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "resetPassword")
    @PostMapping(value = {V2_URL + "/users/password/reset"})
    public Map resetPassword(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("> into resetPassword");
        String newPassword = (String) body.get("password");
        String userId = (String) body.get("userId");
        Map<String, Object> result = userService.resetPassword(userId, newPassword);
        return result;
    }


    /**
     * Update user password map.
     *
     * @param userGuid the user id
     * @param body     the body
     * @param request  the request
     * @return the map
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "expiredPassword")
    @PutMapping(value = {V2_URL + "/users/{userGuid}/password/expired"})
    public Map expiredPassword(@PathVariable String userGuid, @RequestBody Map<String, Object> body, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("> into expiredPassword");
        Map<String, Object> result = userService.expiredPassword(userGuid);
        return result;
    }


    /**
     * Delete user map.
     *
     * @param guid the user id
     * @return the map
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "deleteUser")
    @DeleteMapping(value = {V2_URL + "/users/{guid}"})
    public Map deleteUser(@PathVariable String guid, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("> into deleteUser");
        Map result = userService.deleteUser(guid);
        return result;
    }


    /**
     * 모든 Uaa 유저의 이름과 Guid를 목록으로 가져온다.
     *
     * @return map all user name
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "getAllUserName")
    @GetMapping(value = {V2_URL + "/users"})
    public List<User> getAllUserName(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<User> userInfo = userService.allUsers();
        Map<String, Object> resultMap = new HashMap();
        return userInfo;
    }


    /**
     * 유저의 이름과 Guid를 목록으로 가져온다.
     *
     * @return map all user name
     * @throws Exception the exception
     */
    @HystrixCommand(fallbackMethod = "getUser")
    @GetMapping(value = {V2_URL + "/users/{userId}"})
    public GetUserResponse getUser(@PathVariable String userId, @RequestHeader(AUTHORIZATION_HEADER_KEY) String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return userService.getUser(userId, token);
    }


    /**
     * 계정생성 화면
     *
     * @param userDetail the request
     * @param response   the response
     * @return map
     */
    @HystrixCommand(fallbackMethod = "addUser")
    @PostMapping(value = V2_URL + "/users")
    @ResponseBody
    public Map<String, Object> addUser(@RequestBody UserDetail userDetail, HttpServletRequest request, HttpServletResponse response) {
        Map result = userService.createUser(userDetail);
        return result;
    }


    @HystrixCommand(fallbackMethod = "getUserId")
    @GetMapping(V2_URL + "/user/name-by-id/{username}")
    public String getUserId(@PathVariable String username) {
        return userService.getUserIdByUsername(username);
    }

    @HystrixCommand(fallbackMethod = "getUsername")
    @GetMapping(V2_URL + "/user/id-by-name/{userId}")
    public String getUsername(@PathVariable String userId) {
        return userService.getUsernameByUserId(userId);
    }

    @HystrixCommand(fallbackMethod = "getUserSummary")
    @GetMapping(V2_URL + "/user/summary/{userIdentifier}")
    public User getUserSummary(@PathVariable String userIdentifier, @RequestParam String type) {
        String filterType;
        if (null == type) filterType = "default";
        else filterType = type;

        switch (filterType) {
            case "name":
                return userService.getUserSummaryByUsername(userIdentifier);
            default:
            case "id":
                return userService.getUserSummary(userIdentifier);
        }
    }
}