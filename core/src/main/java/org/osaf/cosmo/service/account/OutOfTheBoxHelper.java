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
package org.osaf.cosmo.service.account;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.User;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * A helper class that creates out of the box collections and items
 * for a new user account.
 */
public class OutOfTheBoxHelper {
    private static final Log log = LogFactory.getLog(OutOfTheBoxHelper.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private ContentDao contentDao;
    private MessageSource messageSource;
    private EntityFactory entityFactory;

    /**
     * <p>
     * Creates a collection named like the user's full name. Inside
     * the collection, places a variety of welcome items.
     * </p>
     */
    public CollectionItem createOotbCollection(OutOfTheBoxContext context) {
        CollectionItem initial =
            contentDao.createCollection(context.getHomeCollection(),
                                        makeCollection(context));

        return initial;
    }

    private CollectionItem makeCollection(OutOfTheBoxContext context) {
        CollectionItem collection = entityFactory.createCollection();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = _("Ootb.Collection.Name", locale, user.getFirstName(),
                        user.getLastName(), user.getUsername());
        String displayName = _("Ootb.Collection.DisplayName",
                               locale, user.getFirstName(),
                               user.getLastName(), user.getUsername());

        collection.setName(name);
        collection.setDisplayName(displayName);
        collection.setOwner(user);

        CalendarCollectionStamp ccs = entityFactory.createCalendarCollectionStamp(collection);
        collection.addStamp(ccs);

        return collection;
    }

    public void init() {
        if (contentDao == null)
            throw new IllegalStateException("contentDao is required");
        if (messageSource == null)
            throw new IllegalStateException("messageSource is required");
        if (entityFactory == null)
            throw new IllegalStateException("entityFactory is required");
    }



    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String _(String key,
                     Locale locale,
                     Object... params) {
        return messageSource.getMessage(key, params, locale);
    }

    private String _(String key,
                     Locale locale) {
        return _(key, locale, new Object[] {});
    }

    private net.fortuna.ical4j.model.TimeZone vtz(String tzid) {
        return TIMEZONE_REGISTRY.getTimeZone(tzid);
    }
}
