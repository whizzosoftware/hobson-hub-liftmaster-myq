/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the "running" state of the plugin. This means the plugin has successfully logged into the myQ cloud
 * and has a valid security token.
 *
 * @author Dan Noguerol
 */
public class RunningState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running;

    RunningState() {
        logger.debug("Entering runningState");
    }

    @Override
    public void onRefresh(StateContext ctx) {
        logger.trace("RunningState.onRefresh()");

        if (!running) {
            ctx.setPluginStatus(PluginStatus.running());
            running = true;
        }

        try {
            for (String deviceId : ctx.getDeviceIds()) {
                ctx.sendHttpGetRequest(new URI("https://myqexternal.myqdevice.com/Device/getDeviceAttribute?appId=" + APP_ID + "&securityToken=" + ctx.getSecurityToken() + "&devId=" + deviceId + "&name=doorstate"), null, "getState:" + deviceId);
            }
        } catch (Exception e) {
            logger.error("Error sending state request", e);
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {

    }

    @Override
    public void onHttpResponse(StateContext ctx, int statusCode, List<Map.Entry<String, String>> headers, String response, Object reqCtx) {
        String s = (String)reqCtx;
        if (s.startsWith("getState:") && statusCode == 200) {
            logger.trace("Received successful device state update: {}", response);
            JSONObject json = new JSONObject(new JSONTokener(response));
            String deviceId = s.substring(9);
            if (json.has("AttributeValue")) {
                String value = json.getString("AttributeValue");
                Boolean b = "1".equals(value) || "4".equals(value) || "5".equals(value) || "9".equals(value);
                logger.trace("Got response from {}: {}", deviceId, response);
                ctx.publishDeviceStateUpdate(deviceId, b);
            } else if (json.has("ErrorMessage")) {
                logger.debug("Received error message; attempting to re-login");
                ctx.setState(new LoggingInState());
            }
        } else if ("setState".equals(s) && statusCode == 200) {
            logger.trace("Successfully made change state request: {}", response);
        } else {
            logger.error("Got unexpected response ({}) for context {}: {}", statusCode, reqCtx, response);
        }
    }

    @Override
    public void onHttpRequestFailure(StateContext ctx, Throwable cause, Object reqCtx) {
        if (((String)reqCtx).startsWith("getState:")) {
            logger.error("Error retrieving device state", cause);
        } else if ("setState".equals(reqCtx)) {
            logger.error("Error setting device state", cause);
        }
    }

    @Override
    public void setDeviceState(StateContext ctx, String deviceId, Boolean state) {
        try {
            JSONObject body = new JSONObject();
            body.put("AttributeName", "desireddoorstate");
            body.put("DeviceId", deviceId);
            body.put("ApplicationId", APP_ID);
            body.put("AttributeValue", state ? "1" : "0");
            body.put("SecurityToken", ctx.getSecurityToken());
            String data = body.toString();
            logger.trace("Sending PUT: {}", data);
            ctx.sendHttpPutRequest(new URI("https://myqexternal.myqdevice.com/Device/setDeviceAttribute"), Collections.singletonMap("Content-Type", "application/json"), data.getBytes(), "setState");
        } catch (Exception e) {
            logger.error("Error sending state change request", e);
        }
    }
}
