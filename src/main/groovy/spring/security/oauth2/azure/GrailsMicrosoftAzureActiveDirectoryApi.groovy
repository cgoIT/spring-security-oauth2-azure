package spring.security.oauth2.azure

import com.github.scribejava.apis.MicrosoftAzureActiveDirectoryApi
import grails.util.Holders

class GrailsMicrosoftAzureActiveDirectoryApi extends MicrosoftAzureActiveDirectoryApi {

    GrailsMicrosoftAzureActiveDirectoryApi() {
        String tenantId = Holders.grailsApplication.config.getProperty(
            'grails.plugin.springsecurity.oauth2.providers.azure.tenant', String)
        new GrailsMicrosoftAzureActiveDirectoryApi(tenantId)
    }

    GrailsMicrosoftAzureActiveDirectoryApi(String tenant) {
        super(tenant, null)
    }
}
