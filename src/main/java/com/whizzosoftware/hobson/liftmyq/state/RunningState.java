/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

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
                ctx.sendHttpRequest(new URI("https://myqexternal.myqdevice.com/Device/getDeviceAttribute?appId=" + APP_ID + "&securityToken=" + ctx.getSecurityToken() + "&devId=" + deviceId + "&name=doorstate"), HttpRequest.Method.GET, null, null, "getState:" + deviceId);
            }
        } catch (Exception e) {
            logger.error("Error sending state request", e);
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {

    }

    @Override
    public void onHttpResponse(StateContext ctx, HttpResponse response, Object reqCtx) {
        try {
            String s = (String)reqCtx;
            String body = response.getBody();
            if (s.startsWith("getState:") && response.getStatusCode() == 200) {
                logger.trace("Received successful device state update: {}", body);
                JSONObject json = new JSONObject(new JSONTokener(body));
                String deviceId = s.substring(9);
                if (json.has("AttributeValue")) {
                    String value = json.getString("AttributeValue");
                    Boolean b = "1".equals(value) || "4".equals(value) || "5".equals(value) || "9".equals(value);
                    logger.trace("Got response from {}: {}", deviceId, body);
                    ctx.publishDeviceStateUpdate(deviceId, b);
                } else if (json.has("ErrorMessage")) {
                    logger.debug("Received error message; attempting to re-login");
                    ctx.setState(new LoggingInState());
                }
            } else if ("setState".equals(s) && response.getStatusCode() == 200) {
                logger.trace("Successfully made change state request: {}", body);
            } else {
                logger.error("Got unexpected response ({}) for context {}: {}", response.getStatusCode(), reqCtx, body);
            }
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
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
            ctx.sendHttpRequest(new URI("https://myqexternal.myqdevice.com/Device/setDeviceAttribute"), HttpRequest.Method.PUT, Collections.singletonMap("Content-Type", "application/json"), data.getBytes(), "setState");
        } catch (Exception e) {
            logger.error("Error sending state change request", e);
        }
    }
}
