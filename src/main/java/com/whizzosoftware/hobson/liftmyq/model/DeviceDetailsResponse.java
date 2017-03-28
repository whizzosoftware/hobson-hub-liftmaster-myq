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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A model for a device details response including the logic to parse its JSON.
 *
 * @author Dan Noguerol
 */
public class DeviceDetailsResponse {
    private Collection<DeviceDetails> details;

    public DeviceDetailsResponse(JSONArray devices) {
        details = new ArrayList<>();
        for (int i=0; i < devices.length(); i++) {
            JSONObject device = devices.getJSONObject(i);
            if (device.has("MyQDeviceTypeName")) {
                String type = device.getString("MyQDeviceTypeName");
                if ("GarageDoorOpener".equals(type) || "VGDO".equals(type) || "Gate".equals(type)) {
                    if (device.has("Attributes")) {
                        JSONArray attributes = device.getJSONArray("Attributes");
                        String name = null;
                        Boolean state = null;
                        boolean active = false;

                        for (int i2 = 0; i2 < attributes.length(); i2++) {
                            JSONObject attribute = attributes.getJSONObject(i2);
                            if (attribute.has("AttributeDisplayName")) {
                                String dname = attribute.getString("AttributeDisplayName");
                                if ("doorstate".equals(dname)) {
                                    String value = attribute.getString("Value");
                                    state = "1".equals(value) || "4".equals(value) || "5".equals(value) || "9".equals(value);
                                } else if ("desc".equals(dname)) {
                                    name = attribute.getString("Value");
                                } else if ("myqmonitormode".equals(dname)) {
                                    active = "0".equals(attribute.getString("Value"));
                                }
                            }
                        }

                        details.add(new DeviceDetails(Integer.toString(device.getInt("MyQDeviceId")), name, active, state));
                    }
                }
            }
        }
    }

    public Collection<DeviceDetails> getDetails() {
        return details;
    }
}
