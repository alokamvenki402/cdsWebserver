package org.pesc.cds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

/**
 * Created by James Whetstone on 10/4/16.
 */
@Configuration
@EnableResourceServer
public class OAuthServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${authentication.oauth.secret}") String secret;
    @Value("${networkServer.id}") String clientId;
    @Value("${directory.server.base.url}") String directoryServerBaseURL;

    @Autowired(required = false)
    ClientHttpRequestFactory clientHttpRequestFactory;

    /*
     * ClientHttpRequestFactory is autowired and checked in case somewhere in
     * your configuration you provided {@link ClientHttpRequestFactory}
     * implementation Bean where you defined specifics of your connection, if
     * not it is instantiated here with {@link SimpleClientHttpRequestFactory}
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        if (clientHttpRequestFactory == null) {
            clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        }
        return clientHttpRequestFactory;
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(directoryServerBaseURL + "/oauth/check_token");
        remoteTokenServices.setClientId(clientId);
        remoteTokenServices.setClientSecret(secret);
        remoteTokenServices.setRestTemplate(new RestTemplate(getClientHttpRequestFactory()));
        return remoteTokenServices;
    }
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenServices(tokenServices()).resourceId("edexchange");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                    .antMatchers(HttpMethod.POST, "/api/v1/transactions")
                    .antMatchers(HttpMethod.POST, "/api/v1/documents/inbox").and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/api/v1/transactions").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/v1/documents/inbox").authenticated();
    }
}
