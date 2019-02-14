package spring.security.oauth2.azure

import grails.plugin.springsecurity.oauth2.SpringSecurityOauth2BaseService
import grails.plugin.springsecurity.oauth2.exception.OAuth2Exception
import grails.plugins.*

class SpringSecurityOauth2AzureGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.5 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Spring Security Oauth2 Azure" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/spring-security-oauth2-azure"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        SpringSecurityOauth2BaseService oAuth2BaseService = grailsApplication.mainContext.getBean(
            'springSecurityOauth2BaseService') as SpringSecurityOauth2BaseService
        AzureOauth2ProviderService oath2ProviderService = grailsApplication.mainContext.getBean(
            'azureOauth2ProviderService') as AzureOauth2ProviderService
        try {
            oAuth2BaseService.registerProvider(oath2ProviderService)
        } catch (OAuth2Exception exception) {
            log.error("There was an oAuth2Exception", exception)
            log.error("OAuth2 Azure not loaded")
        }
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    /*
    @Override
    void doWithApplicationContext() {
        log.trace("doWithApplicationContext")
        def SpringSecurityOauth2BaseService oAuth2BaseService = grailsApplication.mainContext.getBean('springSecurityOauth2BaseService') as SpringSecurityOauth2BaseService
        def FacebookOAuth2Service googleOAuth2Service = grailsApplication.mainContext.getBean('facebookOAuth2Service') as FacebookOAuth2Service
        try {
            oAuth2BaseService.registerProvider(googleOAuth2Service)
        } catch (OAuth2Exception exception) {
            log.error("There was an oAuth2Exception", exception)
            log.error("OAuth2 Google not loaded")
        }
}
     */
}
