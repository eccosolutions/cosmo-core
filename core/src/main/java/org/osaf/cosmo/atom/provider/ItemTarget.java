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
package org.osaf.cosmo.atom.provider;

import java.util.Date;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;
import org.osaf.cosmo.model.NoteItem;

public class ItemTarget extends BaseItemTarget implements Constants {

    private NoteItem item;

    public ItemTarget(RequestContext request,
                      NoteItem item) {
        this(request, item, null, null);
    }

    public ItemTarget(RequestContext request,
                      NoteItem item,
                      String projection,
                      String format) {
        super(type(request), request, projection, format);
        this.item = item;
    }

    // AuditableTarget methods

    public EntityTag getEntityTag() {
        return item != null ? new EntityTag(item.getEntityTag()) : null;
    }

    public Date getLastModified() {
        return item != null ? item.getModifiedDate() : null;
    }

    // our methods

    public NoteItem getItem() {
        return item;
    }

    private static TargetType type(RequestContext request) {
        // on a write operation, the content type distinguishes
        // between entry and media
        if (request.getMethod().equals("PUT")) {
            try {
                if (request.getContentType() != null &&
                    request.getContentType().match(ATOM_MEDIA_TYPE))
                    return TargetType.TYPE_ENTRY;
            } catch (Exception e) {
                // missing or invalid content type - treat as media
            }
            return TargetType.TYPE_MEDIA;
        }
        return TargetType.TYPE_ENTRY;
    }
}
