# cordova-plugin-intune-upn

The plugin helps to fetch UPN (user principal name) and Tenant ID out of MS Intune cache for cordova apps that are wrapped with MS Intune Wrapping Tool or implement MS Intune SDK and MSAL. The plugin does not require any dependencies and does not interfere with Intune Wrapping Tool.

## Install

Install latest release from npm

     cordova plugin add cordova-plugin-intune-upn
     
Or install from github main

    cordova plugin add https://github.com/zalaris/cordova-plugin-intune-upn.git
     
## Uninstall

    cordova plugin rm cordova-plugin-intune-upn

## Usage

The plugin returns a promise that when resolved returns an object with upn and tenantId properties. The promise returns error message when rejected.

```
cordova.plugins.IntuneUPN.getUPN()
    .then((intuneUser) => {
        console.log('User UPN:', intuneUser.upn);
        console.log('User Tenant ID:', intuneUser.tenantId);
    })
    .catch((error) => {
        concole.error(error);
    });
````

## Limitations
### iOS
The plugin can only fetch data if user is logged in using default Intune MAM client id (e.g. the app is wrapped without specifying client id)