/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.hibernate;

import org.springframework.jdbc.support.lob.DefaultLobHandler;

/**
 * LobHandler that uses java.sql.Blob to work with PostgreSQL.
 * Hibernate creates BLOB columns as OID in Postgres and 
 * DefaultLobHandler uses setBinaryStream(), which works fine
 * with MySQL and Derby BLOB, but not Postgres OID.
 */
public class CosmoLobHandler extends DefaultLobHandler {
    public static final CosmoLobHandler INSTANCE = new CosmoLobHandler();

    private CosmoLobHandler() {
        super();
        setWrapAsLob(true);
    }

    // The code that was here seems to be identical to that included in DefaultLobHandler, if you set the wrapAsLob property.
}
