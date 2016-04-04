/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The "configuration wait" state of the plugin. This means that the plugin does not have myQ cloud credentials
 * yet and must wait for the user to provide them.
 *
 * @author Dan Noguerol
 */
public class ConfigurationWaitState implements State {

    private boolean notConfiguredSet = false;

    public ConfigurationWaitState() {
        LoggerFactory.getLogger(getClass()).debug("Entering configurationWaitState");
    }

    @Override
    public void onRefresh(StateContext ctx) {
        PropertyContainer pc = ctx.getConfiguration();
        if (pc != null && pc.hasPropertyValue("username") && pc.hasPropertyValue("password")) {
            ctx.setState(new LoggingInState());
        } else if (!notConfiguredSet) {
            ctx.setPluginStatus(PluginStatus.notConfigured("No username or password has been set"));
            notConfiguredSet = true;
        }

    }

    @Override
    public void onPluginConfigurationUpdate(StateContext ctx) {
        onRefresh(ctx);
    }

    @Override
    public void onHttpResponse(StateContext ctx, int statusCode, List<Map.Entry<String, String>> headers, String response, Object reqCtx) {

    }

    @Override
    public void onHttpRequestFailure(StateContext ctx, Throwable cause, Object reqCtx) {

    }

    @Override
    public void setDeviceState(StateContext ctx, String deviceId, Boolean state) {

    }
}
