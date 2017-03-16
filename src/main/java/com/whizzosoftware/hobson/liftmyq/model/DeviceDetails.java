/*
 *******************************************************************************
 * Copyright (c) 2017 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.liftmyq.model;

/**
 * A model for details about a device.
 *
 * @author Dan Noguerol
 */
public class DeviceDetails {
    private String id;
    private String name;
    private Boolean active;
    private Boolean state;

    public DeviceDetails(String id, String name, Boolean active, Boolean state) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean getState() {
        return state;
    }
}
