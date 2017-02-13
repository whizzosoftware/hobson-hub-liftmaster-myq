/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.liftmyq;

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.device.proxy.AbstractHobsonDeviceProxy;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableMask;

import java.util.Map;

/**
 * Device that represents a myQ garage door opener.
 *
 * @author Dan Noguerol
 */
class MyQGarageDoor extends AbstractHobsonDeviceProxy {
    private Boolean initialState;

    /**
     * Constructor.
     *
     * @param plugin       the HobsonPlugin that created this device
     * @param id           the device ID
     * @param name         the device name
     * @param initialState the initial state of the door (closed=false, open=true, null=unknown)
     */
    MyQGarageDoor(HobsonPlugin plugin, String id, String name, Boolean initialState) {
        super(plugin, id, name, DeviceType.SWITCH);
        this.initialState = initialState;
    }

    @Override
    public void onStartup(String name, Map<String,Object> config) {
        publishVariables(createDeviceVariable(VariableConstants.ON, VariableMask.READ_WRITE, initialState, initialState != null ? System.currentTimeMillis() : null));
    }

    @Override
    public void onShutdown() {}

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    public void onDeviceConfigurationUpdate(Map<String,Object> config) {

    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return null;
    }

    @Override
    public String getManufacturerName() {
        return "LiftMaster";
    }

    @Override
    public String getManufacturerVersion() {
        return null;
    }

    @Override
    public String getModelName() {
        return null;
    }

    @Override
    public void onSetVariables(Map<String,Object> values) {
        if (values.containsKey(VariableConstants.ON)) {
            ((MyQPlugin)getPlugin()).setDeviceState(getContext().getDeviceId(), (Boolean)values.get(VariableConstants.ON));
        }
    }

    void onUpdateState(Boolean state) {
        long now = System.currentTimeMillis();
        setLastCheckin(now);
        setVariableValue(VariableConstants.ON, state, now);

    }
}
