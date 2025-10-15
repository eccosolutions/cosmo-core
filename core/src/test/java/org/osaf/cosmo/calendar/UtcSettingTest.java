package org.osaf.cosmo.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import net.fortuna.ical4j.model.DateTime;
import org.junit.jupiter.api.Test;

public class UtcSettingTest  {

    @Test
    public void datesShouldDefaultToUtc() throws Exception {
        //noinspection deprecation
        assertThat(new java.util.Date().getTimezoneOffset()).isEqualTo(0);
        assertThat(new DateTime("20070101T000000Z").isUtc()).isTrue();

    }
}
