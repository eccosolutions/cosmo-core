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
package org.osaf.cosmo.dav.provider;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.ConflictException;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResponse;
import org.osaf.cosmo.dav.MethodNotAllowedException;
import org.osaf.cosmo.dav.impl.DavFile;
import org.osaf.cosmo.model.EntityFactory;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavFile</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavFile
 */
public class FileProvider extends BaseProvider {
    private static final Log log = LogFactory.getLog(FileProvider.class);

    public FileProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws DavException, IOException {
        if (! content.getParent().exists())
            throw new ConflictException("One or more intermediate collections must be created");

        int status = content.exists() ? 204 : 201;
        content.getParent().addContent(content, createInputContext(request));
        response.setStatus(status);
        response.setHeader("ETag", content.getETag());
    }

    public void mkcol(DavRequest request,
                      DavResponse response,
                      DavCollection collection)
        throws DavException, IOException {
        throw new MethodNotAllowedException("MKCOL not allowed for a file");
    }

    public void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws DavException, IOException {
        throw new MethodNotAllowedException("MKCALENDAR not allowed for a file");
    }
}
