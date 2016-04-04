/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The "detail retrieval state" of the plugin. This means that the plugin has a valid security token and must
 * retrieve a list of devices from the cloud and publish them to the Hobson runtime.
 *
 * @author Dan Noguerol
 */
class DetailRetrievalState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    DetailRetrievalState() {
        logger.debug("Entering detailRetrievalState");
    }

    @Override
    public void onRefresh(StateContext ctx) {
        try {
            ctx.sendHttpGetRequest(new URI("https://myqexternal.myqdevice.com/api/UserDeviceDetails?appId=" + APP_ID + "&securityToken=" + ctx.getSecurityToken()), null, "detail");
        } catch (Exception e) {
            logger.error("Error receiving device details", e);
            ctx.setPluginStatus(PluginStatus.failed("Unable to retrieve device details; see log file for details"));
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {

    }

    @Override
    public void onHttpResponse(StateContext ctx, int statusCode, List<Map.Entry<String, String>> headers, String response, Object reqCtx) {
        if ("detail".equals(reqCtx)) {
            if (statusCode == 200) {
                logger.trace("Received successful system details: {}", response);
                JSONObject json = new JSONObject(new JSONTokener(response));
                if (json.has("Devices")) {
                    processDevices(ctx, json.getJSONArray("Devices"));
                    ctx.setState(new RunningState());
                }
            } else {
                ctx.setPluginStatus(PluginStatus.failed("Unable to retrieve system details; got response " + statusCode));
            }
        }
    }

    @Override
    public void onHttpRequestFailure(StateContext ctx, Throwable cause, Object reqCtx) {
        logger.error("Error received retrieving system details", cause);
        ctx.setPluginStatus(PluginStatus.failed("Error received retrieving system details; check the log for details"));
    }

    @Override
    public void setDeviceState(StateContext ctx, String deviceId, Boolean state) {

    }

    private void processDevices(StateContext ctx, JSONArray devices) {
        for (int i=0; i < devices.length(); i++) {
            JSONObject device = devices.getJSONObject(i);
            if (device.has("MyQDeviceTypeName") && ("GarageDoorOpener".equals(device.getString("MyQDeviceTypeName")) || "VGDO".equals(device.getString("MyQDeviceTypeName")))) {
                if (device.has("Attributes")) {
                    String name = null;
                    Boolean initialState = null;
                    boolean active = false;

                    JSONArray attributes = device.getJSONArray("Attributes");
                    for (int i2=0; i2 < attributes.length(); i2++) {
                        JSONObject attribute = attributes.getJSONObject(i2);
                        if ("doorstate".equals(attribute.getString("Name"))) {
                            initialState = ("1".equals(attribute.getString("Value")));
                        } else if ("name".equals(attribute.getString("Name"))) {
                            name = attribute.getString("Value");
                        } else if ("myqmonitormode".equals(attribute.getString("Name"))) {
                            active = "0".equals(attribute.getString("Value"));
                        }
                    }

                    if (active) {
                        ctx.publishGarageDoorOpener(device.getString("DeviceId"), name, initialState);
                    } else {
                        logger.info("Ignoring device {} since it doesn't appear to be active", name);
                    }
                }
            }
        }
    }
}
