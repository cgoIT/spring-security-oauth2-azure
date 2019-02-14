package spring.security.oauth2.azure

import com.github.scribejava.core.model.OAuth2AccessToken
import grails.plugin.springsecurity.oauth2.token.OAuth2SpringToken

class AzureOauth2SpringToken extends OAuth2SpringToken {

    String oid
    String username

    AzureOauth2SpringToken(OAuth2AccessToken token, String oid, String username) {
        super(token)
        this.oid = oid
        this.username = username
    }

    String getProviderName() {
        'azure'
    }

    String getSocialId() {
        oid
    }

    String getScreenName() {
        username
    }
}
