package org.osaf.cosmo.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Contains time-range data for an event that can be
 * used for determining if an event falls into a
 * given time-range.
 */
@Embeddable
@Table(indexes = {
        @Index(name = "idx_startdt", columnList = "startdate"), // TODO: should have composite cos_es_start_end_float
        @Index(name = "idx_enddt", columnList = "enddate"),
        @Index(name = "idx_floating", columnList = "isfloating"),
        @Index(name = "idx_recurring", columnList = "isrecurring")
})
public class HibEventTimeRangeIndex {

    @Column(table="cosmo_event_stamp", name = "startdate", length=16)
    private String startDate = null;

    @Column(table="cosmo_event_stamp", name = "enddate", length=16)
    private String endDate = null;

    @Column(table="cosmo_event_stamp", name = "isfloating")
    private Boolean isFloating = null;

    @Column(table="cosmo_event_stamp", name = "isrecurring")
    private Boolean isRecurring = null;

    /**
     * The end date of the event.  If the event is recurring, the
     * value is the latest end date for the recurring series (e.g. the second day of a 2-day event that occurs weekly)
     * If the recurring event is infinite, the value will be a
     * String that represents infinity.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format must be one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * Z-TIME-INFINITY<br/>
     * @return start date of the event
     */
    public String getEndDate() {
        return endDate;
    }


    /**
     * The end date of the event.  If the event is recurring, the
     * value is the latest end date for the recurring series (e.g. the second day of a 2-day event that occurs weekly)
     * If the recurring event is infinite, the value will be a
     * String that represents infinity.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format must be one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * Z-TIME-INFINITY<br/>
     * @param endDate end date of the event
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * The start date of the event.  If the event is recurring, the
     * value is the earliest start date for the recurring series.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format is one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     *
     * @return start date of the event
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * The start date of the event.  If the event is recurring, the
     * value is the earliest start date for the recurring series.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format must be one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * @param startDate start date of the event
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
}
