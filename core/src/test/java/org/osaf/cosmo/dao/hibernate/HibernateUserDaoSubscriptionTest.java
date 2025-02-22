/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import org.junit.Test;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibCollectionSubscription;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HibernateUserDaoSubscriptionTest
    extends AbstractHibernateDaoTestCase {

    @Autowired
    protected ContentDaoImpl contentDao;
    @Autowired
    protected UserDaoImpl userDao;

    public HibernateUserDaoSubscriptionTest() {
        super();
    }

    @Test
    public void testSubscribe() throws Exception {
        User user = getUser(userDao, "subuser1");
        CollectionItem root = contentDao.getRootItem(user);
        CollectionItem collection = getCollection(root, "subcoll1");

        CollectionSubscription sub1 = new HibCollectionSubscription();
        sub1.setDisplayName("sub1");
        sub1.setCollection(collection);
        user.addSubscription(sub1);
        userDao.updateUser(user);

        clearSession();

        user = getUser(userDao, "subuser1");

        assertFalse("no subscriptions saved",
                user.getCollectionSubscriptions().isEmpty());

        CollectionSubscription querySub = user
                .getSubscription("sub1");
        assertNotNull("sub1 not found", querySub);
        assertEquals("sub1 not same subscriber", user.getUid(), querySub
                .getOwner().getUid());
        assertEquals("sub1 not same collection", collection.getUid(), querySub
                .getCollectionUid());

        querySub.setDisplayName("sub2");
        userDao.updateUser(user);

        clearSession();

        user = getUser(userDao, "subuser1");

        querySub = user.getSubscription("sub1");
        assertNull("sub1 mistakenly found", querySub);

        querySub = user.getSubscription("sub2");
        assertNotNull("sub2 not found", querySub);

        user.removeSubscription(querySub);
        userDao.updateUser(user);

        clearSession();

        user = getUser(userDao, "subuser1");

        querySub = user.getSubscription("sub1");
        assertNull("sub1 mistakenly found", querySub);

        querySub = user.getSubscription("sub2");
        assertNull("sub2 mistakenly found", querySub);
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private CollectionItem getCollection(CollectionItem parent,
                                         String name) {
        for (Item child : parent.getChildren()) {
            if (child.getName().equals(name))
                return (CollectionItem) child;
        }
        CollectionItem collection = new HibCollectionItem();
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(parent.getOwner());
        return contentDao.createCollection(parent, collection);
    }
}
