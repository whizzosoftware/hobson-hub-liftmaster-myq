/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.liftmyq.state.ConfigurationWaitState;
import com.whizzosoftware.hobson.liftmyq.state.State;
import com.whizzosoftware.hobson.liftmyq.state.StateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Hobson plugin that can manage LiftMaster myQ garage door opener devices via the myQ cloud.
 *
 * @author Dan Noguerol
 */
public class MyQPlugin extends AbstractHttpClientPlugin implements StateContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private State state;
    private PropertyContainer config;
    private String token;
    private List<String> deviceIds = new ArrayList<>();

    public MyQPlugin(String pluginId) {
        super(pluginId);
        setState(new ConfigurationWaitState());
    }

    public long getRefreshInterval() {
        return 30;
    }

    @Override
    public void onStartup(PropertyContainer config) {
        this.config = config;
        state.onPluginConfigurationUpdate(this);
    }

    @Override
    public void onRefresh() {
        state.onRefresh(this);
    }

    @Override
    protected void onHttpResponse(int statusCode, List<Map.Entry<String, String>> headers, String response, Object context) {
        state.onHttpResponse(this, statusCode, headers, response, context);
    }

    @Override
    protected void onHttpRequestFailure(Throwable cause, Object context) {
        state.onHttpRequestFailure(this, cause, context);
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder("username", "User name", "Your myQ account username", TypedProperty.Type.STRING).build(),
            new TypedProperty.Builder("password", "Password", "Your myQ account password", TypedProperty.Type.SECURE_STRING).build()
        };
    }

    @Override
    public String getName() {
        return "LiftMaster myQ";
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
        this.config = config;
        state.onPluginConfigurationUpdate(this);
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void setPluginStatus(PluginStatus status) {
        setStatus(status);
    }

    @Override
    public PropertyContainer getConfiguration() {
        return config;
    }

    @Override
    public void setState(State state) {
        this.state = state;
        state.onRefresh(this);
    }

    @Override
    public String getSecurityToken() {
        return token;
    }

    @Override
    public void setSecurityToken(String token) {
        this.token = token;
    }

    @Override
    public void publishGarageDoorOpener(String deviceId, String name, Boolean initialState) {
        if (!hasDevice(DeviceContext.create(getContext(), deviceId))) {
            logger.debug("Publishing garage door opener ({}) with name {} and initial state {}", deviceId, name, initialState);
            publishDevice(new MyQGarageDoor(this, deviceId, name, initialState));
            deviceIds.add(deviceId);
        }
    }

    @Override
    public Collection<String> getDeviceIds() {
        return deviceIds;
    }

    @Override
    public void publishDeviceStateUpdate(String deviceId, Boolean state) {
        logger.trace("Publishing update for device {}: {}", deviceId, state);
        DeviceContext dctx = DeviceContext.create(getContext(), deviceId);
        setDeviceAvailability(dctx, true, System.currentTimeMillis());
        fireVariableUpdateNotification(new VariableUpdate(VariableContext.create(dctx, VariableConstants.ON), state));
    }

    void setDeviceState(String deviceId, Boolean b) {
        state.setDeviceState(this, deviceId, b);
    }
}
