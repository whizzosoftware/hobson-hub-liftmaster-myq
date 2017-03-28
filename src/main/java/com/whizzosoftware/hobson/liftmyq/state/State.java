/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.liftmyq.state;

import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;

/**
 * An interface for all callbacks and actions that can occur in any given plugin state.
 *
 * @author Dan Noguerol
 */
public interface State {
    /**
     * The hard-coded application ID used in all API requests.
     */
    String APP_ID = "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu";
    /**
     * The hard-coded culture value used in all API requests.
     */
    String CULTURE = "en";

    /**
     * Callback when an HTTP response failure is received.
     *
     * @param ctx the context to use
     * @param cause an exception for the cause of the failure
     * @param reqCtx the request context ID
     */
    void onHttpRequestFailure(StateContext ctx, Throwable cause, Object reqCtx);

    /**
     * Callback when an HTTP response success is received.
     *
     * @param ctx the context to use
     * @param response the HTTP response
     * @param reqCtx the request context ID
     */
    void onHttpResponse(StateContext ctx, HttpResponse response, Object reqCtx);

    /**
     * Callback when the state should be refreshed.
     *
     * @param ctx the context to use
     */
    void onRefresh(StateContext ctx);

    /**
     * Callback when the plugin configuration has changed.
     *
     * @param ctx the context to use
     */
    void onPluginConfigurationUpdate(StateContext ctx);

    /**
     * Sets the state of a device.
     *
     * @param ctx the context to use
     * @param deviceId the ID of the device whose state has changed
     * @param state the new state value
     */
    void setDeviceState(StateContext ctx, String deviceId, Boolean state);
}