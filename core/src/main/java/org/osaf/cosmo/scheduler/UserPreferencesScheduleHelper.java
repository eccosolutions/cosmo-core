/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.StringPropertyUtils;

/**
 * Helper class that translates Schedule objects into user
 * preferences.  Since a schedule is essentially a map
 * of key,value properties, these properties can be stored
 * as user preferences which also have a key and value.
 * 
 * A schedule's properties are stored in the following format:
 * <p>
 * <code>cosmo.scheduler.job.[name].[key] = [value]</code>
 *
 */
public class UserPreferencesScheduleHelper {
    
    EntityFactory entityFactory;
    
    public UserPreferencesScheduleHelper(EntityFactory factory) {
        entityFactory = factory;
    }
    
    public void enableScheduleForUser(User user, Schedule schedule, boolean enabled) {
        Preference pref = user.getPreference("cosmo.scheduler.enabled");
        if(pref==null) {
            pref = entityFactory.createPreference("cosmo.scheduler.enabled", Boolean.toString(enabled));
            user.addPreference(pref);
        } else {
            pref.setValue(Boolean.toString(enabled));
        }
    }
    
    public void addScheduleToUser(User user, Schedule schedule) {
        String prefix = "cosmo.scheduler.job." + schedule.getName() + ".";
        HashMap<String, String> userPrefs = getUserPrefs(user);
        Map<String, String> jobPrefs = StringPropertyUtils.getSubProperties(prefix, userPrefs);
        
        if(jobPrefs.size()!=0)
            throw new IllegalArgumentException("schdedule exists for user");
        
        // add properties
        for(Entry<String, String> entry : schedule.getProperties().entrySet()) {
            String key = prefix + entry.getKey();
            user.addPreference(entityFactory.createPreference(key, entry.getValue()));
        }
    }
    
    public void removeScheduleFromUser(User user, Schedule schedule) {
        String prefix = "cosmo.scheduler.job." + schedule.getName() + ".";
        HashMap<String, String> userPrefs = getUserPrefs(user);
        Map<String, String> jobPrefs = StringPropertyUtils.getSubProperties(prefix, userPrefs);
        
        if(jobPrefs.size()==0)
            throw new IllegalArgumentException("schdedule does not exist for user");
        
        // remove prreferences
        for(String key: jobPrefs.keySet())
            user.removePreference(prefix + key);
    }
    
    public void updateScheduleForUser(User user, Schedule schedule) {
        String prefix = "cosmo.scheduler.job." + schedule.getName() + ".";
        HashMap<String, String> userPrefs = getUserPrefs(user);
        Map<String, String> jobPrefs = StringPropertyUtils.getSubProperties(prefix, userPrefs);
        
        if(jobPrefs.size()==0)
            throw new IllegalArgumentException("schdedule does not exist for user");
        
        // update/add properties
        for(Entry<String, String> entry : schedule.getProperties().entrySet()) {
            String key = prefix + entry.getKey();
            Preference pref = user.getPreference(key);
            if(pref==null) {
                pref = entityFactory.createPreference(key, entry.getValue());
                user.addPreference(pref);
            } else {
                pref.setValue(entry.getValue());
            }
            
            jobPrefs.remove(entry.getKey());
        }
        
        // remove unused props
        for(String key: jobPrefs.keySet())
            user.removePreference(prefix + key);
    }
    
    public Set<Schedule> getSchedulesForUser(User user) {
        HashSet<Schedule> schedules = new HashSet<Schedule>();
        HashMap<String, String> userPrefs = getUserPrefs(user);
        String[] allKeys = userPrefs.keySet().toArray(new String[0]);
        String[] jobKeys = StringPropertyUtils.getChildKeys(
                "cosmo.scheduler.job.", allKeys);

        for (String key : jobKeys) {
            Map<String, String> scheduleProps = StringPropertyUtils
                    .getSubProperties("cosmo.scheduler.job." + key, userPrefs);
            // only add schedule if enabled = true
            if ("true".equals(scheduleProps.get("enabled")))
                schedules.add(new Schedule(key, scheduleProps));
        }

        return schedules;
    }
    
    
    private HashMap<String, String> getUserPrefs(User user) {
        HashMap<String, String> prefs = new HashMap<String, String>();
        for (Preference pref : user.getPreferences())
            prefs.put(pref.getKey(), pref.getValue());

        return prefs;
    }
}
