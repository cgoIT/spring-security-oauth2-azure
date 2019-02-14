# spring-security-oauth2-azure

This is a provider extension for the Grails 3 plugin [`spring-security-oauth2`](http://plugins.grails.org/plugin/matrixcrawler/spring-security-oauth2) 
to support Microsoft Azure AD.

**NB:** This extension uses a much later version of [ScribeJava](https://github.com/scribejava/scribejava) than `spring-security-oauth2`.  The base plugin uses v2.7.3;
this one uses v6.3.0.  I am using a newer version because support for Microsoft Azure AD was only added to ScribeJava in early v6.  This extension
should not break `spring-security-oauth2`, but upgrading ScribeJava may break other plugins or extensions, including `spring-security-oauth2`
provider extensions, that rely on earlier versions of ScribeJava.  Please see the end of this README for some notes on ScribeJava version issues I encountered.

## Installation

This plugin is available through the [Binx Health Bintray](https://bintray.com/binxhealth/grails-plugins).

## Configuration

The following properties may be configured in `application.yml` or `application.groovy`, as you prefer.

```yaml
grails:
    plugin:
        springsecurity:
            oauth2:
                providers:
                    azure:
                        api_key: azure-app-id             # required
                        api_secret: azure-api-secret      # required
                        callback: /my/oauth/callbackUri   # required
                        tenant: azure-tenant-id           # required
                        scopes: scope1 scope2             # optional
```

The config properties are:

| Property Name   | Property Value    | Required? |
|-----------------|-------------------|-----------|
| `api_key`       | Your Azure appId      | Yes |
| `api_secret`    | Your Azure API Secret | Yes |
| `callback`      | Your oAuth callback URI (configured in Azure) | Yes |
| `tenant`        | Your Azure tenantId   | Yes |
| `scopes`        | Additional scopes you want to specify in your authentication requests.  The default scope provided by this extension is `User.Read` | No  |

## Troubleshooting ScribeJava

While writing this plugin, I ran into a ScribeJava version issue in `OAuth2AbstractProviderService.buildScribeService()`.
It seems that the `com.github.scribejava.core.builder.ServiceBuilder` constructor implementation was changed between v2 and v6 (surprise!)

I fixed the issue by overriding the `buildScribeService()` method in my custom `ProviderService`.  If you are seeing "Invalid API key" errors from
other `spring-security-oauth2` provider extensions after installing this plugin, that is likely the cause.
