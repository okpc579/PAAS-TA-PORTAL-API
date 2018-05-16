package org.openpaas.paasta.portal.api.controller;

import org.cloudfoundry.uaa.clients.GetClientResponse;
import org.cloudfoundry.uaa.clients.ListClientsRequest;
import org.cloudfoundry.uaa.clients.ListClientsResponse;
import org.openpaas.paasta.portal.api.common.Common;
import org.openpaas.paasta.portal.api.common.Constants;
import org.openpaas.paasta.portal.api.model.Client;
import org.openpaas.paasta.portal.api.service.AppService;
import org.openpaas.paasta.portal.api.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
public class ClientController extends Common {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    /**
     * 클라이언트 목록 조회
     *
     * @param
     * @return Map client list
     * @throws Exception the exception
     */
    @GetMapping(value = {Constants.V2_URL+ "/clients"})
    public ListClientsResponse getClientList() throws Exception {
        return clientService.getClientList();
    }

    /**
     * 클라이언트 상세 정보 조회
     *
     * @param clientId clientId
     * @return Map client
     * @throws Exception the exception
     */
    @GetMapping(value = {"/clients/{clientId}"})
    public GetClientResponse getClient(@PathVariable String clientId) throws Exception {
        return clientService.getClient(clientId);
    }

    /**
     * 클라이언트 등록
     *
     * @param client Client
     * @return Map map
     * @throws Exception the exception
     */
    @PostMapping(value = {"/clients"})
    public Map<String, Object> registerClient(@RequestBody Client client) throws Exception {
//        CustomCloudFoundryClient adminCcfc = getCustomCloudFoundryClient(uaaAdminClientId, uaaAdminClientSecret);
//        return clientService.registerClient(adminCcfc, param);

        return null;
    }

    /**
     * 클라이언트 수정
     *
     * @param param Map
     * @return Map map
     * @throws Exception the exception
     */
    @RequestMapping(value = {"/updateClient"}, method = RequestMethod.POST)
    public Map<String, Object> updateClient(@RequestBody Map<String, Object> param) throws Exception {
//        CustomCloudFoundryClient adminCcfc = getCustomCloudFoundryClient(uaaAdminClientId, uaaAdminClientSecret);
//        return clientService.updateClient(adminCcfc, param);

        return null;
    }

    /**
     * 클라이언트 삭제
     *
     * @param param Map
     * @return Map map
     * @throws Exception the exception
     */
    @RequestMapping(value = {"/deleteClient"}, method = RequestMethod.POST)
    public Map<String, Object> deleteClient(@RequestBody Map<String, Object> param) throws Exception {
//        CustomCloudFoundryClient adminCcfc = getCustomCloudFoundryClient(uaaAdminClientId, uaaAdminClientSecret);
//        return clientService.deleteClient(adminCcfc, param);

        return null;
    }

}