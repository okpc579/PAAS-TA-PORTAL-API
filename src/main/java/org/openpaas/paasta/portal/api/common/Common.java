package org.openpaas.paasta.portal.api.common;

import org.apache.http.conn.util.InetAddressUtils;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.tokens.GetTokenByClientCredentialsRequest;
import org.cloudfoundry.uaa.tokens.GetTokenByClientCredentialsResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.openpaas.paasta.portal.api.config.cloudfoundry.provider.TokenGrantTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


@Service
public class Common {

    private static final Logger LOGGER = LoggerFactory.getLogger(Common.class);

    @Value("${cloudfoundry.cc.api.url}")
    public String apiTarget;


    @Value("${cloudfoundry.cc.api.uaaUrl}")
    public String uaaTarget;

    @Value("${cloudfoundry.cc.api.sslSkipValidation}")
    public boolean cfskipSSLValidation;

    @Value("${cloudfoundry.user.admin.username}")
    public String adminUserName;

    @Value("${cloudfoundry.user.admin.password}")
    public String adminPassword;

    public static final String AUTHORIZATION_HEADER_KEY = "cf-Authorization";

    @Value("${cloudfoundry.user.uaaClient.clientId}")
    public String uaaClientId;

    @Value("${cloudfoundry.user.uaaClient.clientSecret}")
    public String uaaClientSecret;

    @Value("${cloudfoundry.user.uaaClient.adminClientId}")
    public String uaaAdminClientId;

    @Value("${cloudfoundry.user.uaaClient.adminClientSecret}")
    public String uaaAdminClientSecret;

    @Value("${cloudfoundry.user.uaaClient.skipSSLValidation}")
    public boolean uaaskipSSLValidation;

    @Value("${monitoring.api.url}")
    public String monitoringApiTarget;

    private Long currentTimeMiils = System.currentTimeMillis();


    @Autowired
    PaastaConnectionContext paastaConnectionContext;

    @Autowired
    PaastaTokenContext paastaTokenContext;


    public ObjectMapper objectMapper = new ObjectMapper();

    /**
     * DefaultCloudFoundryOperations을 생성하여, 반환한다.
     *
     * @param connectionContext
     * @param tokenProvider
     * @return DefaultCloudFoundryOperations
     */
    public DefaultCloudFoundryOperations cloudFoundryOperations(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return DefaultCloudFoundryOperations.builder().cloudFoundryClient(cloudFoundryClient(connectionContext, tokenProvider)).dopplerClient(dopplerClient(connectionContext, tokenProvider)).uaaClient(uaaClient(connectionContext, tokenProvider)).build();
    }

    public DefaultCloudFoundryOperations cloudFoundryOperations(TokenProvider tokenProvider) {
        return DefaultCloudFoundryOperations.builder().cloudFoundryClient(cloudFoundryClient(connectionContext(), tokenProvider)).dopplerClient(dopplerClient(connectionContext(), tokenProvider)).uaaClient(uaaClient(connectionContext(), tokenProvider)).build();
    }

    public DefaultCloudFoundryOperations cloudFoundryOperations() {
        return DefaultCloudFoundryOperations.builder().cloudFoundryClient(cloudFoundryClient(connectionContext(), tokenProvider())).dopplerClient(dopplerClient(connectionContext(), tokenProvider())).uaaClient(uaaClient(connectionContext(), tokenProvider())).build();
    }

    /**
     * ReactorCloudFoundryClient 생성하여, 반환한다.
     *
     * @param connectionContext
     * @param tokenProvider
     * @return DefaultCloudFoundryOperations
     */
    public ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
    }

    public ReactorCloudFoundryClient cloudFoundryClient(TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider).build();
    }

    public ReactorCloudFoundryClient cloudFoundryClient() {
        return ReactorCloudFoundryClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider()).build();
    }

    /**
     * ReactorDopplerClient 생성하여, 반환한다.
     *
     * @param connectionContext
     * @param tokenProvider
     * @return ReactorDopplerClient
     */
    public ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
    }

    public ReactorDopplerClient dopplerClient(TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider).build();
    }

    public ReactorDopplerClient dopplerClient() {
        return ReactorDopplerClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider()).build();
    }

    /**
     * ReactorUaaClient 생성하여, 반환한다.
     *
     * @param connectionContext
     * @param tokenProvider
     * @return ReactorUaaClient
     */
    public ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorUaaClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
    }

    public ReactorUaaClient uaaClient(TokenProvider tokenProvider) {
        return ReactorUaaClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider).build();
    }

    public ReactorUaaClient uaaClient() {
        return ReactorUaaClient.builder().connectionContext(connectionContext()).tokenProvider(tokenProvider()).build();
    }


    /**
     * ReactorUaaClient(관리자) 생성하여, 반환한다.
     *
     * @param connectionContext
     * @param uaaAdminClientId
     * @param uaaAdminClientSecret
     * @return ReactorUaaClient
     */
    public ReactorUaaClient uaaAdminClient(ConnectionContext connectionContext, String adminUserName, String adminPassword, String uaaAdminClientId, String uaaAdminClientSecret) {
        ReactorUaaClient reactorUaaClient = uaaClient(connectionContext, tokenProvider());
        GetTokenByClientCredentialsResponse getTokenByClientCredentialsResponse = reactorUaaClient.tokens().getByClientCredentials(GetTokenByClientCredentialsRequest.builder().clientId(uaaAdminClientId).clientSecret(uaaAdminClientSecret).build()).block();
        return uaaClient(connectionContext, clinetTokenProvider(getTokenByClientCredentialsResponse.getAccessToken()));
    }


    private ProxyConfiguration proxyConfiguration(String proxyHost) {
        int proxyPort = 9022;
        String proxyIP = null;
        if (!InetAddressUtils.isIPv4Address(proxyHost) && !InetAddressUtils.isIPv6Address(proxyHost)) {
            try {
                InetAddress ia = InetAddress.getByName(proxyHost);
                proxyIP = ia.getHostAddress();
            } catch (UnknownHostException e) {
                throw new Error("Unable to resolve proxyhost", e);
            }
        } else {
            proxyIP = proxyHost;
        }
        LOGGER.info(" -> Proxy :: " + proxyIP + ":" + proxyPort);

        return ProxyConfiguration.builder().host(proxyIP).port(proxyPort).build();

    }


    public DefaultConnectionContext defaultConnectionContextBuild(String cfApiUrl, boolean cfskipSSLValidation) {
        return DefaultConnectionContext.builder().apiHost(cfApiUrl.replace("https://", "").replace("http://", "")).skipSslValidation(cfskipSSLValidation).keepAlive(true).build();
    }

    /**
     * DefaultConnectionContext 가져온다.
     *
     * @return DefaultConnectionContext
     */
    public DefaultConnectionContext connectionContext() {
        if (paastaConnectionContext == null) {
            paastaConnectionContext = new PaastaConnectionContext(defaultConnectionContextBuild(apiTarget, cfskipSSLValidation), new Date());
        } else {
            if (paastaConnectionContext.getCreate_time() != null) {
                if(ContextAndTokenTimeOut(paastaConnectionContext, 10)) {
                    LOGGER.info("paastaConnectionContext reproduction");
                    paastaConnectionContext.getConnectionContext().dispose();
                    paastaConnectionContext = null;
                    paastaConnectionContext = new PaastaConnectionContext(defaultConnectionContextBuild(apiTarget, cfskipSSLValidation), new Date());
                }
            } else {
                paastaConnectionContext = null;
                paastaConnectionContext = new PaastaConnectionContext(defaultConnectionContextBuild(apiTarget, cfskipSSLValidation), new Date());
            }
        }
        return paastaConnectionContext.getConnectionContext();
    }


    /**
     * TokenGrantTokenProvider 생성하여, 반환한다.
     *
     * @param token
     * @return DefaultConnectionContext
     * @throws Exception
     */
    public TokenProvider tokenProvider(String token) {

        if (token.indexOf("bearer") < 0) {
            token = "bearer " + token;
        }
        TokenProvider tokenProvider = new TokenGrantTokenProvider(token);

        return tokenProvider;

    }

    public TokenGrantTokenProvider clinetTokenProvider(String token) {

        if (token.indexOf("bearer") < 0) {
            token = "bearer " + token;
        }
        TokenGrantTokenProvider tokenProvider = new TokenGrantTokenProvider(token);
        return tokenProvider;
    }


    public PasswordGrantTokenProvider tokenProvider(String username, String password) {
        return PasswordGrantTokenProvider.builder().password(password).username(username).build();
    }

    public PasswordGrantTokenProvider tokenProvider() {
        if (paastaTokenContext == null) {
            paastaTokenContext = new PaastaTokenContext(PasswordGrantTokenProvider.builder().password(adminPassword).username(adminUserName).build(), new Date());
        } else if(paastaTokenContext.getCreate_time() != null && ContextAndTokenTimeOut(paastaTokenContext, 5)){
            LOGGER.info("paastaTokenContext reproduction");
            paastaTokenContext = new PaastaTokenContext(PasswordGrantTokenProvider.builder().password(adminPassword).username(adminUserName).build(), new Date());
        }
        return paastaTokenContext.tokenProvider();
    }

    private boolean ContextAndTokenTimeOut(PaastaContextInterface paastaContextInterface, int timelimit){
        Calendar now = Calendar.getInstance();
        Calendar create_time = Calendar.getInstance();
        create_time.setTime(paastaContextInterface.getCreate_time());
        create_time.add(Calendar.MINUTE, timelimit);
        return create_time.getTimeInMillis() < now.getTimeInMillis();
    }


    /******************************************************************************* Utill/
     /**
     * 요청 파라미터들의 빈값 또는 null값 확인을 하나의 메소드로 처리할 수 있도록 생성한 메소드
     * 요청 파라미터 중 빈값 또는 null값인 파라미터가 있는 경우, false를 리턴한다.
     *
     * @param params
     * @return boolean
     */
    public boolean stringNullCheck(String... params) {
        return Arrays.stream(params).allMatch(param -> null != param && !param.equals(""));
    }

}
