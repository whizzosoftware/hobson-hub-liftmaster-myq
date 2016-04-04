/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
            ctx.sendHttpGetRequest(new URI("https://myqexternal.myqdevice.com/Membership/ValidateUserWithCulture?appId=" + APP_ID + "&securityToken=null&username=" + username + "&password=" + password + "&culture=" + CULTURE), null, "login");
        } catch (URISyntaxException e) {
            throw new HobsonRuntimeException("Error sending login request", e);
        }
    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {
    }

    @Override
    public void onHttpResponse(StateContext ctx, int statusCode, List<Map.Entry<String, String>> headers, String response, Object reqCtx) {
        if ("login".equals(reqCtx)) {
            if (statusCode == 200) {
                logger.trace("Received successful login response: {}", response);
                JSONObject json = new JSONObject(new JSONTokener(response));
                ctx.setSecurityToken(json.getString("SecurityToken"));
                ctx.setState(new DetailRetrievalState());
            } else {
                ctx.setPluginStatus(PluginStatus.failed("Unable to login; got response " + statusCode));
            }
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
