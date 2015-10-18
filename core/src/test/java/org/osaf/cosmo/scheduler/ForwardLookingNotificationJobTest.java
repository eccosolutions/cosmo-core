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

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.apache.commons.id.random.SessionIdGenerator;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEventStamp;
import org.osaf.cosmo.scheduler.ForwardLookingReport.UpcomingResult;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.TriggerFiredBundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test ForwardLookingNotificationJob
 */
public class ForwardLookingNotificationJobTest extends TestCase {
   
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    private StandardContentService contentService;
    private StandardUserService userService;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        contentService = new StandardContentService();
        lockManager = new SingleVMLockManager();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.setLockManager(lockManager);
        contentService.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
        contentService.init();
        
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();
    }

    public void testGenerateReport() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        JobDetail jobDetail = new JobDetailImpl("1", "user1", Job.class);

        Date fireTime = new DateTime("20080101T100000", tz);
        
        TriggerFiredBundle tfb = new TriggerFiredBundle(jobDetail, new SimpleTriggerImpl(), null, false, fireTime, null, null, null);
        
        JobExecutionContext context = new JobExecutionContextImpl(null, tfb, null);
        
        ForwardLookingNotificationJob job = new ForwardLookingNotificationJob();
        job.setUsername("user1");
        job.setContentService(contentService);
        job.setUserService(userService);
        job.setReportType(ForwardLookingNotificationJob.REPORT_TYPE_DAILY);
        job.setTimezone("America/Chicago");
        
        List<String> colUids = new ArrayList<String>();
        colUids.add("COL-UID");
        job.setCollectionUids(colUids);
        
        // create test data
        User user = testHelper.makeDummyUser("user1", "user1");
        userService.createUser(user);
        CollectionItem root = contentDao.getRootItem(user);
        
        CollectionItem calendar = testHelper.makeDummyCollection(user);
        calendar.setUid("COL-UID");
        
        contentDao.createCollection(root, calendar);
       
        NoteItem note = testHelper.makeDummyItem(user);
        note.setUid("NOTE-UID-1");
        note.getTriageStatus().setCode(TriageStatus.CODE_DONE);
        
        contentDao.createContent(calendar, note);
        
        // test no matching items
        Report report = job.generateReport(context);
        Assert.assertNotNull(report);
        
        ForwardLookingReport flr = (ForwardLookingReport) report;
        Assert.assertTrue(flr.getNowItems().isEmpty());
        Assert.assertTrue(flr.getUpcomingItems().isEmpty());
        
        // test 1 NOW item
        note.getTriageStatus().setCode(TriageStatus.CODE_NOW);
        
        report = job.generateReport(context);
        Assert.assertNotNull(report);
        Assert.assertTrue(report instanceof ForwardLookingReport);
        
        flr = (ForwardLookingReport) report;
        Assert.assertEquals(ForwardLookingNotificationJob.REPORT_TYPE_DAILY, flr.getReportType());
        Assert.assertEquals(new DateTime("20080101T000000", tz).getTime(), flr.getStartDate().getTime());
        Assert.assertEquals(1, flr.getNowItems().size());
        Assert.assertEquals(0, flr.getUpcomingItems().size());
        Assert.assertEquals(user, flr.getUser());
        Assert.assertEquals("America/Chicago", flr.getTimezone().getID());
        
        // add alarm to item so that is shows up in upcoming
        note.setReminderTime(new DateTime("20080101T120000", tz));
        
        report = job.generateReport(context);
        flr = (ForwardLookingReport) report;
       
        Assert.assertEquals(1, flr.getNowItems().size());
        Assert.assertEquals(1, flr.getUpcomingItems().size());
        
        UpcomingResult uResult = flr.getUpcomingItems().get(0);
        
        Assert.assertEquals(calendar, uResult.getCollection());
        Assert.assertEquals(note, uResult.getNote());
        Assert.assertTrue(uResult.isAlarmResult());
        
        // add event to item/ set triageStatus to LATER
        MockEventStamp eventStamp = new MockEventStamp(note);
        note.addStamp(eventStamp);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20080101T100000", tz));
        eventStamp.setDuration(new Dur("PT1H"));
        note.getTriageStatus().setCode(TriageStatus.CODE_LATER);
        
        report = job.generateReport(context);
        flr = (ForwardLookingReport) report;
       
        Assert.assertEquals(0, flr.getNowItems().size());
        Assert.assertEquals(2, flr.getUpcomingItems().size());
        
        uResult = flr.getUpcomingItems().get(0);
        Assert.assertFalse(uResult.isAlarmResult());
        uResult = flr.getUpcomingItems().get(1);
        Assert.assertTrue(uResult.isAlarmResult());
        
        // change date of event so that it is out of range/remove alarm
        eventStamp.setStartDate(new DateTime("20080103T100000", tz));
        note.setReminderTime(null);
        report = job.generateReport(context);
        Assert.assertNotNull(report);
        flr = (ForwardLookingReport) report;
        Assert.assertTrue(flr.getNowItems().isEmpty());
        Assert.assertTrue(flr.getUpcomingItems().isEmpty());
        
        // make report weekly so that it picks up event
        job.setReportType(ForwardLookingNotificationJob.REPORT_TYPE_WEEKLY);
        
        report = job.generateReport(context);
        flr = (ForwardLookingReport) report;
       
        Assert.assertEquals(0, flr.getNowItems().size());
        Assert.assertEquals(1, flr.getUpcomingItems().size());
        Assert.assertEquals(ForwardLookingNotificationJob.REPORT_TYPE_WEEKLY, flr.getReportType());
    }
    
}
