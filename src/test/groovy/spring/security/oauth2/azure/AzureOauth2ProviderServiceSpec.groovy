package spring.security.oauth2.azure

import com.github.scribejava.core.model.OAuth2AccessToken
import grails.plugin.springsecurity.oauth2.token.OAuth2SpringToken
import grails.testing.services.ServiceUnitTest
import groovy.json.JsonBuilder
import org.springframework.util.Base64Utils
import spock.lang.Specification

class AzureOauth2ProviderServiceSpec extends Specification implements ServiceUnitTest<AzureOauth2ProviderService> {

    static final String DUMMY_ACCESS_TOKEN = '{"key": "val"}'
    static final String ID_TOKEN_SEPARATOR = '.'
    static final String APP_ID = 'appId'
    static final String OID = 'abcdef'
    static final String USERNAME = 'bob@test.com'
    static final Integer YESTERDAY = calculateUnixTimestamp(new Date() - 1)
    static final Integer TOMORROW = calculateUnixTimestamp(new Date() + 1)

    def setup() {
        service.appId = APP_ID
    }

    void "test createSpringAuthToken - success"() {
        given: "An access token with a valid ID token"
        OAuth2AccessToken accessToken = createAccessToken(initProviderClaims())

        when: "we consume and process the token"
        OAuth2SpringToken springToken = service.createSpringAuthToken(accessToken)

        then: "it succeeds"
        springToken

        and: "the user data is populated correctly"
        springToken.screenName == USERNAME
        springToken.socialId == OID
    }

    void "test createSpringAuthToken - incorrect id_token.aud"() {
        given: "An access token with an invalid aud param on the ID token"
        Map providerClaims = initProviderClaims()
        providerClaims.aud = 'invalid!'
        OAuth2AccessToken accessToken = createAccessToken(providerClaims)

        when: "we consume and process the token"
        service.createSpringAuthToken(accessToken)

        then: "it fails"
        thrown IllegalStateException
    }

    void "test createSpringAuthToken - token cannot yet be processed"() {
        given: "An access token with an nbf param later than the current time"
        Map providerClaims = initProviderClaims()
        providerClaims.nbf = TOMORROW
        OAuth2AccessToken accessToken = createAccessToken(providerClaims)

        when: "we consume and process the token"
        service.createSpringAuthToken(accessToken)

        then: "it fails"
        thrown IllegalStateException
    }

    void "test createSpringAuthToken - token expired"() {
        given: "An access token with an exp param earlier than the current time"
        Map providerClaims = initProviderClaims()
        providerClaims.exp = YESTERDAY
        OAuth2AccessToken accessToken = createAccessToken(providerClaims)

        when: "we consume and process the token"
        service.createSpringAuthToken(accessToken)

        then: "it fails"
        thrown IllegalStateException
    }

    void "test createSpringAuthToken - token time travel!"() {
        given: "An access token with an iat param later than the current time"
        Map providerClaims = initProviderClaims()
        providerClaims.iat = TOMORROW
        OAuth2AccessToken accessToken = createAccessToken(providerClaims)

        when: "we consume and process the token"
        service.createSpringAuthToken(accessToken)

        then: "it fails"
        thrown IllegalStateException
    }

    private static Map initProviderClaims() {
        [
            oid: OID,
            unique_name: USERNAME,
            aud: APP_ID,
            iat: YESTERDAY,
            nbf: YESTERDAY,
            exp: TOMORROW
        ]
    }

    private static OAuth2AccessToken createAccessToken(Map providerClaimsSegment) {
        String headerSegment = new String(Base64Utils.encodeUrlSafe('{}'.bytes))
        String claimsSegment = new String(Base64Utils.encodeUrlSafe(new JsonBuilder(providerClaimsSegment).toString().bytes))
        String idToken = [headerSegment, claimsSegment, ''].join(ID_TOKEN_SEPARATOR)

        Map rawResponseData = [
            id_token: idToken
        ]

        String rawResponse = new JsonBuilder(rawResponseData)
        return new OAuth2AccessToken(DUMMY_ACCESS_TOKEN, rawResponse)
    }

    private static Integer calculateUnixTimestamp(Date date) {
        date.time / 1000 as Integer
    }

}
