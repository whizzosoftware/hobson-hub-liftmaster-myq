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
import com.whizzosoftware.hobson.api.property.PropertyContainer;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * An interface that represents all actions a state can perform.
 *
 * @author Dan Noguerol
 */
public interface StateContext {
    /**
     * Returns the current plugin configuration.
     *
     * @return a PropertyContainer object
     */
    PropertyContainer getConfiguration();

    /**
     * Returns all published device IDs.
     *
     * @return a Collection of device ID strings
     */
    Collection<String> getDeviceIds();

    /**
     * Returns the current security token.
     *
     * @return a security token string (or null if there is none available)
     */
    String getSecurityToken();

    /**
     * Sets the current plugin status.
     *
     * @param status the new status value
     */
    void setPluginStatus(PluginStatus status);

    /**
     * Sends an HTTP request.
     *
     * @param uri the URI to send the request to
     * @param method the HTTP request method
     * @param headers request headers (or null for none)
     * @param context a request ID (used to correlate requests to async responses)
     */
    void sendHttpRequest(URI uri, HttpRequest.Method method, Map<String,String> headers, byte[] data, Object context);

    /**
     * Set a new plugin state.
     *
     * @param state the new state
     */
    void setState(State state);

    /**
     * Sets the current security token.
     *
     * @param token the new security token string
     */
    void setSecurityToken(String token);

    /**
     * Publishes a device state update.
     *
     * @param deviceId the device ID
     * @param state the updated state
     */
    void publishDeviceStateUpdate(String deviceId, Boolean state);

    /**
     * Publish a new garage door opener device.
     *
     * @param deviceId the ID of the new device
     * @param name the name of the new device
     * @param initialState the device's initial state (or null if unknown)
     */
    void publishGarageDoorOpener(String deviceId, String name, Boolean initialState);
}
