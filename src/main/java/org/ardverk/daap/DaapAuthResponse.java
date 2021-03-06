/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.daap;

/**
 * An abstract base class for DaapAuthResponses which is from client's
 * perspective an request for authentication. Derived classes implement the
 * actual DaapAuthResponse.
 * 
 * @author Roger Kapsi
 */
public abstract class DaapAuthResponse implements DaapResponse {

    protected final DaapRequest request;
    protected final byte[] header;

    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponse(DaapRequest request) {
        this.request = request;

        DaapServer<?> server = request.getServer();
        DaapConfig config = server.getConfig();
        Object scheme = config.getAuthenticationScheme();

        if (scheme.equals(DaapConfig.BASIC_SCHEME)) {
            header = DaapHeaderConstructor.createBasicAuthHeader(request);
        } else {
            header = DaapHeaderConstructor.createDigestAuthHeader(request);
        }
    }

    @Override
    public String toString() {
        return (new String(header));
    }
}
