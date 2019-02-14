package spring.security.oauth2.azure

import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.oauth2.service.OAuth2AbstractProviderService
import grails.plugin.springsecurity.oauth2.token.OAuth2SpringToken
import grails.plugin.springsecurity.oauth2.util.OAuth2ProviderConfiguration
import groovy.json.JsonSlurper
import org.springframework.util.Base64Utils
import javax.annotation.PostConstruct

@Transactional
class AzureOauth2ProviderService extends OAuth2AbstractProviderService {

    GrailsApplication grailsApplication

    private String appId

    @PostConstruct
    void init() {
        appId = grailsApplication.config.getProperty('grails.plugin.springsecurity.oauth2.providers.azure.api_key',
            String)
    }

    /**
     * @return The ProviderID
     */
    String getProviderID() {
        'azure'
    }

    /**
     * A scribeJava API class to use for the oAuth Request or any other class that extends the @link{DefaultApi20}
     * @return The ApiClass that is to use
     */
    Class<? extends DefaultApi20> getApiClass() {
        GrailsMicrosoftAzureActiveDirectoryApi
    }

    /**
     * Path to the OAuthScope that is returning the UserIdentifier
     * i.e 'https://graph.facebook.com/me' for facebook
     */
    String getProfileScope() {
        'https://graph.microsoft.com/User.Read'
    }

    /**
     * The scopes that are at least required by the oauth2 provider, to get an email-address
     * Additional scopes can be configured in the application.yml
     */
    String getScopes() {
        profileScope
    }

    /**
     * Get separator string for concatenating the mandatory and the optional scopes
     */
    String getScopeSeparator() {
        ' '
    }

    /**
     * This method parses and verifies the id_token param returned by Azure AD on successful
     * authentication.
     * {@see https://docs.microsoft.com/en-us/azure/active-directory/develop/id-tokens}
     *
     * @param accessToken
     * @return AzureOauth2SpringToken
     */
    OAuth2SpringToken createSpringAuthToken(OAuth2AccessToken accessToken) {
        def rawResponse = new JsonSlurper().parseText(accessToken.rawResponse)
        String encodedIdToken = rawResponse.id_token
        List<String> encodedIdTokenSegments = encodedIdToken.split('\\.')

        String payloadClaimsStr = new String(Base64Utils.decodeFromUrlSafeString(encodedIdTokenSegments[1]))
        Map payloadClaims = new JsonSlurper().parseText(payloadClaimsStr) as Map
        verifyIdToken(payloadClaims)

        String oid = payloadClaims.oid
        String username = payloadClaims.unique_name

        new AzureOauth2SpringToken(accessToken, oid, username)
    }

    OAuth20Service buildScribeService(OAuth2ProviderConfiguration providerConfiguration) {
        ServiceBuilder serviceBuilder = new ServiceBuilder(providerConfiguration.apiKey)
            .apiSecret(providerConfiguration.apiSecret)
        if (providerConfiguration.callbackUrl) {
            serviceBuilder.callback(providerConfiguration.callbackUrl)
        }
        if (providerConfiguration.debug) {
            serviceBuilder.debug()
        }

        serviceBuilder.build(new GrailsMicrosoftAzureActiveDirectoryApi())
    }

    private void verifyIdToken(Map payloadClaims) {
        if (payloadClaims.aud != appId) {
            throw new IllegalStateException("ID Token rejected: token specified incorrect recipient ID " +
                "${payloadClaims.aud}")
        }

        Integer now = new Date().time / 1000 as Integer // UNIX timestamp
        if (now < payloadClaims.nbf) {
            throw new IllegalStateException("ID Token rejected: token cannot be processed before " +
                "${payloadClaims.nbf}; current time is $now")
        }

        if (now >= payloadClaims.exp) {
            throw new IllegalStateException("ID Token rejected: token has expired")
        }

        if (now < payloadClaims.iat) {
            throw new IllegalStateException("ID Token rejected: token cannot be from the future!")
        }
    }
}
