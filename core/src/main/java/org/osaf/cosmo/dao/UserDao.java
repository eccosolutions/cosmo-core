/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao;

import java.util.Set;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for DAOs that manage user resources.
 *
 * A user resource stores properties about a user account and acts as
 * the root collection for an account's shared data (its "home
 * directory").
 */
public interface UserDao extends Dao {

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    @Transactional(readOnly = true)
    Set<User> getUsers();

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    @Transactional(readOnly = true)
    User getUser(String username);


    /**
     * Returns the user account identified by the given uid.
     *
     * @param uid the uid of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    @Transactional(readOnly = true)
    User getUserByUid(String uid);

    @Transactional(readOnly = true)
    User getUserById(long id);

    /**
     * Returns the user account identified by the given activation id.
     *
     * @param id the activation hash corresponding to the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    @Transactional(readOnly = true)
    User getUserByActivationId(String id);

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    @Transactional(readOnly = true)
    User getUserByEmail(String email);

    /**
     * Creates a user account in the repository. Returns a new
     * instance of <code>User</code> after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    User createUser(User user);

    /**
     * Updates a user account that exists in the repository. Returns a
     * new instance of <code>User</code>  after saving the original
     * one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    User updateUser(User user);

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    void removeUser(String username);

    /**
     * Removes a user account from the repository.
     *
     * @param user the user to remove
     */
    void removeUser(User user);
}
