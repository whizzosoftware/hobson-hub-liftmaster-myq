/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.liftmyq.RequestUtil;
import com.whizzosoftware.hobson.liftmyq.model.DeviceDetails;
import com.whizzosoftware.hobson.liftmyq.model.DeviceDetailsResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
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
            Map<String,String> headers = RequestUtil.createHeaders();
            headers.put("SecurityToken", ctx.getSecurityToken());
            ctx.sendHttpRequest(new URI("https://myqexternal.myqdevice.com/api/v4/UserDeviceDetails/Get"), HttpRequest.Method.GET, headers, null, "detail");
        } catch (Exception e) {
            logger.error("Error receiving device details", e);
            ctx.setPluginStatus(PluginStatus.failed("Unable to retrieve device details; see log file for details"));
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {

    }

    @Override
    public void onHttpResponse(StateContext ctx, HttpResponse response, Object reqCtx) {
        try {
            if ("detail".equals(reqCtx)) {
                if (response.getStatusCode() == 200) {
                    String s = response.getBody();
                    logger.trace("Received successful system details: {}", s);
                    JSONObject json = new JSONObject(new JSONTokener(s));
                    if (json.has("Devices")) {
                        processDevices(ctx, json.getJSONArray("Devices"));
                        ctx.setState(new RunningState());
                    }
                } else {
                    ctx.setPluginStatus(PluginStatus.failed("Unable to retrieve system details; got response " + response.getStatusCode()));
                }
            }
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
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
        DeviceDetailsResponse ddr = new DeviceDetailsResponse(devices);
        for (DeviceDetails dd : ddr.getDetails()) {
            if (dd.isActive()) {
                ctx.publishGarageDoorOpener(dd.getId(), dd.getName(), dd.getState());
            }
        }
    }
}
