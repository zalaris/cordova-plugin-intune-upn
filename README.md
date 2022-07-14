# cordova-plugin-intune-upn

The plugin helps to fetch UPN (user principal name) and Tenant ID out of MS Intune cache for cordova apps that are wrapped with MS Intune Wrapping Tool or implement MS Intune SDK and MSAL. It might be usefull as login hints for further authentications in the app.

The plugin does not require any dependencies and does not interfere with Intune Wrapping Tool and other libraries.

## Install

The plugin accepts CLIENT_ID as optional parameter. This allows you to set your own Client Id from Azure. If omitted the default Intune MAM Client ID is used instead.

Install latest release from CLI

     cordova plugin add cordova-plugin-intune-upn [--variable CLIENT_ID=myAzureClientId]
     
Or using config.xml

    <plugin name="cordova-plugin-intune-upn" source="npm">
        <variable name="CLIENT_ID" value="myAzureClientId" />
    </plugin>
     
## Uninstall

    cordova plugin rm cordova-plugin-intune-upn

## Usage
The plugin also accepts the client id as optional parameter during runtime. If supplied it will overwrite the one defined in config.xml.
The plugin returns a promise that when resolved returns an object with upn and tenantId properties. The promise returns error message when rejected.

```
let clientId = 'myAzureClientId'; //optional

cordova.plugins.IntuneUPN.getUPN(clientId)
    .then((intuneUser) => {
        console.log('UPN:', intuneUser.upn);
        console.log('Tenant ID:', intuneUser.tenantId);
    })
    .catch((error) => {
        console.error(error);
    });
````
