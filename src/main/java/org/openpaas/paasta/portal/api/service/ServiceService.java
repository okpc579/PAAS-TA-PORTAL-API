package org.openpaas.paasta.portal.api.service;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.v2.servicebrokers.*;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesResponse;
import org.cloudfoundry.client.v2.serviceinstances.UpdateServiceInstanceRequest;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.CreateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.CreateUserProvidedServiceInstanceResponse;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.UpdateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.UpdateUserProvidedServiceInstanceResponse;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.openpaas.paasta.portal.api.common.Common;
import org.openpaas.paasta.portal.api.model.Service;
import org.openpaas.paasta.portal.api.model.ServiceBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

//import org.openpaas.paasta.portal.api.mapper.portal.ServiceMapper;

/**
 * 서비스 컨트롤 - 서비스 목록 , 서비스 상세 정보, 서비스 인스턴스 추가, 서비스 인스턴스 수정, 서비스 인스턴스 삭제 등 서비스 인스턴스 관리를  제공한다.
 *
 * @author 조민구
 * @version 1.0
 * @since 2016.4.4 최초작성
 */
@EnableAsync
@org.springframework.stereotype.Service
public class ServiceService extends Common {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceService.class);

//    @Autowired
//    private ServiceMapper serviceMapper;


    /**
     * 서비스 인스턴스를 조회한다.
     *
     * @param service the service
     * @param client  the client
     * @return the service instance
     * @throws Exception the exception
     */
    public CloudServiceInstance getServiceInstance(org.openpaas.paasta.portal.api.model.Service service, CloudFoundryClient client) throws Exception {

        CloudServiceInstance cloudServiceInstance = client.getServiceInstance(service.getName());

        return cloudServiceInstance;

    }

    /**
     * 서비스 인스턴스 이름을 변경한다.
     *
     * @param service the service
     * @return the boolean
     * @throws Exception the exception
     */
    public Map renameInstance(Service service, String guid) throws Exception {
        HashMap result = new HashMap();
//        guid.renameInstanceService(service.getGuid(), service.getNewName());
        try {
            ReactorCloudFoundryClient cloudFoundryClient = cloudFoundryClient(connectionContext(), tokenProvider(adminUserName, adminPassword));
            //cloudFoundryClient.applicationsV2().update(UpdateApplicationRequest.builder().applicationId(guid).name(service.getNewName()).build()).block();
            cloudFoundryClient.serviceInstances().update(UpdateServiceInstanceRequest.builder().serviceInstanceId(guid).name(service.getNewName()).build()).block();
            result.put("result", true);
            result.put("msg", "You have successfully completed the task.");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 서비스 인스턴스를 삭제한다.
     *
     * @param guid the service
     * @throws Exception the exception
     */
    public Map deleteInstance(String guid) throws Exception {
        HashMap result = new HashMap();
//        client.deleteInstanceService(service.getGuid());
        try {
            ReactorCloudFoundryClient cloudFoundryClient = cloudFoundryClient(connectionContext(), tokenProvider(adminUserName, adminPassword));
            cloudFoundryClient.serviceInstances().delete(DeleteServiceInstanceRequest.builder().serviceInstanceId(guid).async(false).build()).block();
            result.put("result", true);
            result.put("msg", "You have successfully completed the task.");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 유저 프로바이드 서비스를 조회한다.
     *
     * @param token the token
     * @param body  the body
     * @return boolean user provided
     * @throws Exception the exception
     * @author kimdojun
     * @version 1.0
     * @since 2016.8.4 최초작성
     */
    public Map<String, Object> getUserProvided(String token, Map<String, String> body) throws Exception {

//        String orgName = body.get("orgName");
//        String spaceName = body.get("spaceName");
//        String serviceInstanceName = body.get("serviceInstanceName");
//
//        if (!stringNullCheck(orgName, spaceName, serviceInstanceName)) {
//            throw new CloudFoundryException(HttpStatus.BAD_REQUEST,"Bad Request","Required request body content is missing");
//        }
//
//        CustomCloudFoundryClient client = getCustomCloudFoundryClient(token, orgName, spaceName);
//
//        Map<String, Object> userProvidedServiceInstance = client.getUserProvidedServiceInstance(orgName, spaceName, serviceInstanceName);
//        LOGGER.info(userProvidedServiceInstance.toString());
        return null;
    }

    /**
     * 유저프로바이드 서비스 인스턴스를 생성한다.
     *
     * @param token the token
     * @param service  the body
     * @return boolean boolean
     * @throws Exception the exception
     * @author CSJ
     * @version 2.0
     * @since 2018.5.30 최초작성
     */
    public Map createUserProvided(String token, Service service) throws Exception {
        HashMap result = new HashMap();

        String orgName = service.getOrgName();//("orgName");
        String spaceName = service.getSpaceName();//("spaceName");
        String serviceInstanceName = service.getServiceInstanceName();//("serviceInstanceName");
        String credentialsStr = service.getCredentials();//.get("credentials");
        String syslogDrainUrl = service.getSyslogDrainUrl();//("syslogDrainUrl"); // null 또는 빈값 허용

        try {
            if (!stringNullCheck(orgName, spaceName, serviceInstanceName, credentialsStr)) {}

            Map<String,Object> map = new HashMap<>();
            ReactorCloudFoundryClient cloudFoundryClient = cloudFoundryClient(connectionContext(), tokenProvider(adminUserName,adminPassword));

            CreateUserProvidedServiceInstanceResponse createUserProvidedServiceInstanceResponse = cloudFoundryClient.userProvidedServiceInstances()
                    .create(CreateUserProvidedServiceInstanceRequest.builder().name(serviceInstanceName)
                            .spaceId(String.valueOf(service.getSpaceGuid())).credential("","").syslogDrainUrl("").build()).block();

            LOGGER.info("Created :::: " + createUserProvidedServiceInstanceResponse.getEntity().getName());

            result.put("result", true);
            result.put("msg", "You have successfully completed the task.");

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Error :::: " + e.getMessage());
            result.put("result", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 유저프로바이드 서비스 인스턴스를 수정한다.
     *
     * @param token the token
     * @param service  the body
     * @return boolean boolean
     * @throws Exception the exception
     * @author CSJ
     * @version 2.0
     * @since 2018.5.30 최초작성
     */
    public Map updateUserProvided(String guid, String token, Service service) throws Exception {

        HashMap result = new HashMap();

        String orgName = service.getOrgName();
        String spaceName = service.getSpaceName();
        String serviceInstanceName = service.getServiceInstanceName();
        String newServiceInstanceName = service.getNewServiceInstanceName();
        String credentialsStr = service.getCredentials();
        String syslogDrainUrl = service.getSyslogDrainUrl();

        LOGGER.info("orgName ::::" + orgName);
        LOGGER.info("spaceName ::::" + spaceName);
        LOGGER.info("spaceGuid ::::" + service.getSpaceGuid());
        LOGGER.info("serviceInstanceName ::::" + serviceInstanceName);
        LOGGER.info("newServiceInstanceName ::::" + newServiceInstanceName);
        LOGGER.info("serviceInstanceId ::::" + guid);
        LOGGER.info("credentialsStr ::::" + credentialsStr);
        LOGGER.info("syslogDrainUrl ::::" + syslogDrainUrl);

        try{
            if (!stringNullCheck(orgName, spaceName, serviceInstanceName,newServiceInstanceName, credentialsStr)) {}

            ReactorCloudFoundryClient cloudFoundryClient = cloudFoundryClient(connectionContext(), tokenProvider(adminUserName,adminPassword));
            UpdateUserProvidedServiceInstanceResponse updateUserProvidedServiceInstanceResponse = cloudFoundryClient.userProvidedServiceInstances()
                    .update(UpdateUserProvidedServiceInstanceRequest.builder().name(service.getServiceInstanceName()).userProvidedServiceInstanceId(guid).credential("","").syslogDrainUrl("").build()).block();

            LOGGER.info("Created :::: " + updateUserProvidedServiceInstanceResponse.getEntity().getName());
            result.put("result", true);
            result.put("msg", "You have successfully completed the task.");

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Error :::: " + e.getMessage());
            result.put("result", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }


    /**
     * 서비스 브로커 리스트를 조회한다.
     *
     * @param token token
     * @return the boolean
     * @throws Exception the exception
     */
    public ListServiceBrokersResponse getServiceBrokers(String token) throws Exception {
        return Common.cloudFoundryClient(connectionContext(), tokenProvider(token))
                .serviceBrokers()
                .list(ListServiceBrokersRequest.builder().build())
                .log()
                .block();
    }


    /**
     * 서비스 브로커를 조회한다.
     *
     * @param serviceBroker the serviceBroker
     * @return the boolean
     * @throws Exception the exception
     */
    public GetServiceBrokerResponse getServiceBroker(ServiceBroker serviceBroker, String token) throws Exception {
        return Common.cloudFoundryClient(connectionContext(), tokenProvider(token))
                .serviceBrokers()
                .get(GetServiceBrokerRequest.builder()
                        .serviceBrokerId(serviceBroker.getGuid().toString())
                        .build()
                ).log()
                .block();
    }

    /**
     * 서비스 브로커를 생성한다.
     *
     * @param serviceBroker the cloudServiceBroker
     * @return the boolean
     * @throws Exception the exception
     */
    public CreateServiceBrokerResponse createServiceBroker(ServiceBroker serviceBroker, String token) throws Exception {
        return Common.cloudFoundryClient(connectionContext(), tokenProvider(token))
                .serviceBrokers()
                .create(CreateServiceBrokerRequest.builder()
                        .name(serviceBroker.getName())
                        .brokerUrl(serviceBroker.getUrl())
                        .authenticationUsername(serviceBroker.getUsername())
                        .authenticationPassword(serviceBroker.getPassword())
                        .build()
                ).block();
    }

    /**
     * 서비스 브로커를 수정한다.
     *
     * @param serviceBroker the cloudServiceBroker
     * @param token
     * @return the boolean
     * @throws Exception the exception
     */
    public UpdateServiceBrokerResponse updateServiceBroker(ServiceBroker serviceBroker, String token) throws Exception {

        return Common.cloudFoundryClient(connectionContext(), tokenProvider(token))
                .serviceBrokers()
                .update(UpdateServiceBrokerRequest.builder()
                        .serviceBrokerId(serviceBroker.getGuid().toString())
                        .name(serviceBroker.getName())
                        .authenticationUsername(serviceBroker.getUsername())
                        .authenticationPassword(serviceBroker.getPassword())
                        .brokerUrl(serviceBroker.getUrl())
                        .build()
                ).block();
    }

    /**
     * 서비스 브로커를 삭제한다.
     *
     * @param guid the cloudServiceBroker
     * @return the boolean
     * @throws Exception the exception
     */
    public boolean deleteServiceBroker(String guid, String token) throws Exception {

        Common.cloudFoundryClient(connectionContext(), tokenProvider(token))
                .serviceBrokers()
                .delete(DeleteServiceBrokerRequest.builder()
                        .serviceBrokerId(guid)
                        .build()
                )
                .block();
        return true;
    }

    /**
     * 서비스 이미지를 가져온다.
     *
     * @param serviceName the serviceName
     * @return the boolean
     * @throws Exception the exception
     */
    public String getServiceImageUrl(String serviceName) {

//        String appImageUrl = serviceMapper.getServiceImageUrl(serviceName);
        String appImageUrl = "";
        return appImageUrl;

    }

    public ListServiceInstancesResponse getServicesInstances(String guid, String token) {
        ReactorCloudFoundryClient cloudFoundryClient = cloudFoundryClient(connectionContext(), tokenProvider(token));

        ListServiceInstancesResponse listServicesInstancesResponse =
                cloudFoundryClient.serviceInstances().list(ListServiceInstancesRequest.builder().spaceId(guid).build()
                ).block();

        return listServicesInstancesResponse;
    }


}
