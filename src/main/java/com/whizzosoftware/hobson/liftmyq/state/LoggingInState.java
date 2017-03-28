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

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.liftmyq.RequestUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * The "logging in" state of the plugin. This means that there is no valid security token and the plugin must send a
 * login request and wait for a successful response.
 *
 * @author Dan Noguerol
 */
class LoggingInState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    LoggingInState() {
        logger.debug("Entering loggingInState");
    }

    @Override
    public void onRefresh(StateContext ctx) {
        String username = ctx.getConfiguration().getStringPropertyValue("username");
        String password = ctx.getConfiguration().getStringPropertyValue("password");
        logger.debug("Performing login for user {}", username);
        try {
            String data = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            ctx.sendHttpRequest(new URI("https://myqexternal.myqdevice.com/api/v4/User/Validate"), HttpRequest.Method.POST, RequestUtil.createHeaders(), data.getBytes(), "login");
        } catch (URISyntaxException e) {
            throw new HobsonRuntimeException("Error sending login request", e);
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {
    }

    @Override
    public void onHttpResponse(StateContext ctx, HttpResponse response, Object reqCtx) {
        try {
            if ("login".equals(reqCtx)) {
                if (response.getStatusCode() == 200) {
                    String s = response.getBody();
                    try {
                        JSONObject json = new JSONObject(new JSONTokener(s));
                        ctx.setSecurityToken(json.getString("SecurityToken"));
                        logger.trace("Received security token: {}", ctx.getSecurityToken());
                        ctx.setState(new DetailRetrievalState());
                    } catch (JSONException e) {
                        logger.error("Received malformed JSON response during login: " + s);
                    }
                } else {
                    ctx.setPluginStatus(PluginStatus.failed("Unable to login; got response " + response.getStatusCode()));
                }
            }
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
        }
    }

    @Override
    public void onHttpRequestFailure(StateContext ctx, Throwable cause, Object reqCtx) {
        if ("login".equals(reqCtx)) {
            logger.error("Unable to login", cause);
            ctx.setPluginStatus(PluginStatus.failed("Unable to login; see log file for details"));
        }
    }

    @Override
    public void setDeviceState(StateContext ctx, String deviceId, Boolean state) {

    }
}
