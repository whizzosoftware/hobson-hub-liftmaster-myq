/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;

/**
 * Device that represents a myQ garage door opener.
 *
 * @author Dan Noguerol
 */
class MyQGarageDoor extends AbstractHobsonDevice {
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
        super(plugin, id);
        setDefaultName(name);
        this.initialState = initialState;
    }

    @Override
    public void onStartup(PropertyContainer config) {
        super.onStartup(config);
        publishVariable(VariableConstants.ON, initialState, HobsonVariable.Mask.READ_WRITE, initialState != null ? System.currentTimeMillis() : null);
    }

    @Override
    public void onShutdown() {}

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.SWITCH;
    }

    @Override
    public void onSetVariable(String variableName, Object value) {
        if (VariableConstants.ON.equals(variableName)) {
            ((MyQPlugin)getPlugin()).setDeviceState(getContext().getDeviceId(), (Boolean)value);
        }
    }
}
