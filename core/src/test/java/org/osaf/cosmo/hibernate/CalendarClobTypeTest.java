package org.osaf.cosmo.hibernate;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Uid;
import org.osaf.cosmo.calendar.ICalendarUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.UUID;

/**
 * @since 20/08/2013
 */
public class CalendarClobTypeTest extends TestCase {

    private static final long ONE_DAY = 1000L * 60 * 60 * 24;
    private static final long ONE_HOUR = 1000L * 60 * 60;
    private final CalendarClobType calendarClobType = new CalendarClobType();

    public void test_GivenCalendarContainsVAvailability_WhenDeepCopied_ThenAvailableComponentsAreCopied() throws ParseException, IOException, URISyntaxException {
        final Calendar calendar = createCalendar();
        final Calendar copiedCalendar = (Calendar) calendarClobType.deepCopy(calendar);
        final VAvailability copiedVAvailability = (VAvailability) copiedCalendar.getComponent(Component.VAVAILABILITY);
        assertNotNull("Expect a VAVAILABILITY component", copiedVAvailability);
        assertEquals("Expect one AVAILABLE component", 1, copiedVAvailability.getAvailable().size());
        assertEquals("Expect calendars to be identical as strings", calendar.toString(), copiedCalendar.toString());
    }

    private Calendar createCalendar() {
        final VAvailability vAvailability = new VAvailability();
        vAvailability.getProperties().add(new Uid(UUID.randomUUID().toString()));
        // DtStamp is created automatically by the VAvailability constructor
        final long now = System.currentTimeMillis();
        vAvailability.getProperties().add(new DtStart(new net.fortuna.ical4j.model.DateTime(now)));
        vAvailability.getProperties().add(new DtEnd(new net.fortuna.ical4j.model.DateTime(now + ONE_DAY)));
        Available available = new Available();
        available.getProperties().add(new Uid(UUID.randomUUID().toString()));
        available.getProperties().add(new DtStamp(new net.fortuna.ical4j.model.DateTime(now)));
        available.getProperties().add(new DtStart(new net.fortuna.ical4j.model.DateTime(now + (ONE_HOUR * 9))));
        available.getProperties().add(new DtEnd(new net.fortuna.ical4j.model.DateTime(now + (ONE_HOUR * 18))));
        vAvailability.getAvailable().add(available);

        return ICalendarUtils.createBaseCalendar(vAvailability);
    }
}
