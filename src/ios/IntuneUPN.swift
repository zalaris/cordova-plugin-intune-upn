/*
 * Copyright 2022 Zalaris ASA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import Foundation

@objc(IntuneUPN) class IntuneUPN : CDVPlugin {
	@objc(getUPN:)
	func getUPN(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(
			status: CDVCommandStatus_ERROR,
			messageAs: "Something wrong happend! Most likely the app is not wrapped using Intune Wrapping Tool or does not implement MS Intune SDK and/or MSAL."
		)

		struct PluginOutput: Encodable {
            let upn: String
            let tenantId: String
        }
        
        let MAMDefaultClientId = "6c7e8096-f593-4d72-807f-a5f86dcc9c77"
        
        do {
			//Get UPN
			let clsIntuneMAMEnrollmentManager = NSClassFromString("IntuneMAMEnrollmentManager") as! NSObject.Type
			let objInstance = clsIntuneMAMEnrollmentManager.perform(NSSelectorFromString("instance")).takeUnretainedValue()
			let objEnrolledAccount = objInstance.perform(NSSelectorFromString("enrolledAccount")).takeUnretainedValue()
			let upn: String = objEnrolledAccount as? String ?? ""
			NSLog(upn)
			
			//Get Tenant Id
			let clsMSALPublicClientApp = NSClassFromString("MSALPublicClientApplication") as! NSObject.Type
			let objMSALPublicClientApp = clsMSALPublicClientApp.init()
		
			let objMSALClientAppInitialized = objMSALPublicClientApp.perform(NSSelectorFromString("initWithClientId:error:"), with: MAMDefaultClientId, with: nil).takeUnretainedValue()
			let objMSALAccount = objMSALClientAppInitialized.perform(NSSelectorFromString("accountForUsername:error:"), with: upn, with: nil).takeUnretainedValue()
			
			let homeAccountId = objMSALAccount.value(forKey: "homeAccountId")
			let tenantId: String = (homeAccountId as AnyObject).value(forKey: "tenantId") as? String ?? ""
			NSLog(tenantId)
			
			//Build plugin output
			let pluginOutput = PluginOutput(upn: upn, tenantId: tenantId)
			let jsonEncoder = JSONEncoder()

			let encodePluginOutputData = try jsonEncoder.encode(pluginOutput)
			let encodePluginOutput = String(data: encodePluginOutputData, encoding: .utf8)!

			pluginResult = CDVPluginResult(
				status: CDVCommandStatus_OK,
				messageAs: encodePluginOutput
			)
				
			self.commandDelegate!.send(
				pluginResult,
				callbackId: command.callbackId
			)

        } catch {
            NSLog(error.localizedDescription)
            self.commandDelegate!.send(
				pluginResult,
				callbackId: command.callbackId
			)
        }
	}
}