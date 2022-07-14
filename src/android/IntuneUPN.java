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

package com.zalaris.cordova.intuneupn;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class IntuneUPN extends CordovaPlugin {
    private final String TAG = "IntuneUPNPlugin";

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of
     * work, use:
     * cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     * cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param args         The exec() arguments in JSON form.
     * @param callbackContext The callback context used when calling back into
     *                        JavaScript.
     * @return Whether the action was valid.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if ("getUPN".equals(action)) {
            try {
                JSONObject upn = this.getUPN(callbackContext);
                callbackContext.success(upn);
                return true;

            } catch (Exception e){
                Log.e(TAG, e.getMessage());

                callbackContext.error("Something wrong has happened! Most likely the app is not wrapped using MS Intune Wrapping Tool or does not implement MS Intune SDK.");
                return false;
            }
        } else {
            callbackContext.error("Wrong method!");
            return false;
        }
    }

    private JSONObject getUPN(CallbackContext callbackContext) throws Exception {
        JSONObject result = new JSONObject();

        try {
            Class clsMamComponents = Class.forName("com.microsoft.intune.mam.client.app.MAMComponents");
            Class clsUserInfo = Class.forName("com.microsoft.intune.mam.policy.MAMUserInfo");
            Class clsMAMIdentityManager = Class.forName("com.microsoft.intune.mam.client.identity.MAMIdentityManager");

            // Reflect MAMComponents.get
            Method methodMamGet = clsMamComponents.getMethod("get", new Class[] { Class.class });

            // Reflect MAMComponents.get(MAMUserInfo.class)
            Object objUserInfo = methodMamGet.invoke(clsMamComponents, clsUserInfo);

            // Get User's UPN
            Method methodGetUser = objUserInfo.getClass().getMethod("getPrimaryUser", null);
            String userUPN = methodGetUser.invoke(objUserInfo).toString();
            result.put("upn", userUPN);

            // Reflect MAMComponents.get(MAMIdentityManager.class)
            Object objIdentityManager = methodMamGet.invoke(clsMamComponents, clsMAMIdentityManager);

            // Get User Identity instance (contains Azure Tenant ID)
            Method methodFromString = objIdentityManager.getClass().getMethod("fromString", new Class[] { String.class });
            Object userIdentity = methodFromString.invoke(objIdentityManager, userUPN);

            // Get Azure Tenant ID
            try {
                Method methodGetTenantId = userIdentity.getClass().getMethod("tenantId", null);
                String tenantId = methodGetTenantId.invoke(userIdentity).toString();
                result.put("tenantId", tenantId);

            } catch (NoSuchMethodException e) {
                Log.i(TAG, "There is no method " + e.getMessage() + ". Switching to reflect the field mTenantAadId.");

                Field fTenantId = userIdentity.getClass().getDeclaredField("mTenantAadId");
                fTenantId.setAccessible(true);
                String tenantId = fTenantId.get(userIdentity).toString();
                result.put("tenantId", tenantId);
            }

        } catch (NoSuchMethodException e) {
            throw new Exception("There is no method " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("Cannot execute method " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new Exception("There is no class " + e.getMessage());
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new Exception("There is no field " + e.getMessage());
        }

        return result;
    }
}