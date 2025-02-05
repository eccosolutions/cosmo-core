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
package org.osaf.cosmo.dao.hibernate.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.RecurrenceExpander;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrenceUtil;
import org.osaf.cosmo.model.filter.AttributeFilter;
import org.osaf.cosmo.model.filter.BetweenExpression;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EqualsExpression;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.FilterCriteria;
import org.osaf.cosmo.model.filter.FilterExpression;
import org.osaf.cosmo.model.filter.FilterOrder;
import org.osaf.cosmo.model.filter.FilterOrder.Order;
import org.osaf.cosmo.model.filter.ILikeExpression;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.LikeExpression;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.NullExpression;
import org.osaf.cosmo.model.filter.StampFilter;
import org.osaf.cosmo.model.filter.TextAttributeFilter;
import org.osaf.cosmo.model.hibernate.HibNoteItem;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>.
 * Translates filter into HQL TypedQuery, executes
 * query and processes the results.
 */
public class StandardItemFilterProcessor implements ItemFilterProcessor {

    private static final Log log = LogFactory.getLog(StandardItemFilterProcessor.class);

    public StandardItemFilterProcessor() {}

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.hibernate.query.ItemFilterProcessor#processFilter(org.hibernate.Session, org.osaf.cosmo.model.filter.ItemFilter)
     */
    public Set<Item> processFilter(EntityManager session, ItemFilter filter) {
        TypedQuery<Item> hibQuery = buildQuery(session, filter);
        List<Item> queryResults = hibQuery.getResultList();
        return processResults(queryResults, filter);
    }

    /**
     * Build Hibernate TypedQuery from ItemFilter using HQL.
     * The query returned is essentially the first pass at
     * retrieving the matched items.  A second pass is required in
     * order determine if any recurring events match a timeRange
     * in the filter.  This is due to the fact that recurring events
     * may have complicated recurrence rules that are extremely
     * hard to match using HQL.
     * @param session session
     * @param filter item filter
     * @return hibernate query built using HQL
     */
    public TypedQuery<Item> buildQuery(EntityManager session, ItemFilter filter) {
        StringBuilder selectBuf = new StringBuilder();
        StringBuilder whereBuf = new StringBuilder();
        StringBuilder orderBuf = new StringBuilder();

        HashMap<String, Object> params = new HashMap<>();

        if(filter instanceof NoteItemFilter)
            handleNoteItemFilter(selectBuf, whereBuf, orderBuf, params, (NoteItemFilter) filter);
        else if(filter instanceof ContentItemFilter)
            handleContentItemFilter(selectBuf, whereBuf, orderBuf, params, (ContentItemFilter) filter);
        else
            handleItemFilter(selectBuf, whereBuf, params, filter);

        selectBuf.append(whereBuf);

        for(FilterOrder fo: filter.getOrders()) {
            if(orderBuf.isEmpty())
                orderBuf.append(" order by ");
            else
                orderBuf.append(", ");

            orderBuf.append("i.").append(fo.getName());

            if(fo.getOrder().equals(Order.DESC))
                orderBuf.append(" desc");
        }

        selectBuf.append(orderBuf);

        if(log.isDebugEnabled()) {
            log.debug(selectBuf.toString());
        }

        @SuppressWarnings("SqlSourceToSinkFlow")
        TypedQuery<Item> hqlQuery = session.createQuery(selectBuf.toString(), Item.class);

        for(Entry<String, Object> param: params.entrySet())
            hqlQuery.setParameter(param.getKey(), param.getValue());

        if(filter.getMaxResults()!=null)
            hqlQuery.setMaxResults(filter.getMaxResults());

        return hqlQuery;
    }

    private void handleItemFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {

        if(selectBuf.toString().isEmpty())
            selectBuf.append("select i from HibItem i");

        // filter on uid
        if(filter.getUid()!=null)
            formatExpression(whereBuf, params, "i.uid", filter.getUid());


        // filter on parent
        if(filter.getParent()!=null) {
            selectBuf.append(" join i.parentDetails pd");
            appendWhere(whereBuf, "pd.primaryKey.collection=:parent");
            params.put("parent", filter.getParent());
        }

        if(filter.getDisplayName()!=null)
            formatExpression(whereBuf, params, "i.displayName", filter.getDisplayName());


        handleAttributeFilters(selectBuf, whereBuf, params, filter);
        handleStampFilters(selectBuf, whereBuf, params, filter);

    }

    private void handleAttributeFilters(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        for(AttributeFilter attrFilter: filter.getAttributeFilters()) {
            if(attrFilter instanceof TextAttributeFilter)
                handleTextAttributeFilter(selectBuf, whereBuf, params, (TextAttributeFilter) attrFilter);
            else
                handleAttributeFilter(selectBuf, whereBuf, params, attrFilter);
        }
    }

    private void handleTextAttributeFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            TextAttributeFilter filter) {

        String alias = "ta" + params.size();
        selectBuf.append(", HibTextAttribute ").append(alias);
        appendWhere(whereBuf, alias + ".item=i and " + alias +".qname=:" + alias + "qname");
        params.put(alias + "qname", filter.getQname());
        formatExpression(whereBuf, params, alias + ".value", filter.getValue());
    }

    private void handleStampFilters(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        for(StampFilter stampFilter: filter.getStampFilters()) {
            if(stampFilter instanceof EventStampFilter)
                handleEventStampFilter(selectBuf, whereBuf, params, (EventStampFilter) stampFilter);
            else
                handleStampFilter(selectBuf, whereBuf, params, stampFilter);
        }
    }

    private void handleStampFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            StampFilter filter) {

        String toAppend = "";
        if(filter.isMissing())
            toAppend += "not ";
        toAppend += "exists (select s.id from HibStamp s where s.item=i and s.class=Hib"
                    + filter.getStampClass().getSimpleName() + ")";
        appendWhere(whereBuf, toAppend);
    }

    private void handleAttributeFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            AttributeFilter filter) {

        String param = "param" + params.size();
        String toAppend = "";
        if(filter.isMissing())
            toAppend += "not ";

        toAppend += "exists (select a.id from HibAttribute a where a.item=i and a.qname=:"
                + param + ")";
        appendWhere(whereBuf, toAppend);
        params.put(param, filter.getQname());
    }

    private void handleEventStampFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, HashMap<String, Object> params,
            EventStampFilter filter) {

        selectBuf.append(", HibBaseEventStamp es");
        appendWhere(whereBuf, "es.item=i");

        // handle recurring event filter
        if(filter.getIsRecurring()!=null) {
            if(filter.getIsRecurring())
                appendWhere(whereBuf, "(es.timeRangeIndex.isRecurring=true or i.modifies is not null)");
            else
                appendWhere(whereBuf, "(es.timeRangeIndex.isRecurring=false and i.modifies is null)");
        }

        // handle time range
        if(filter.getPeriod()!=null) {
           whereBuf.append(" and ( ");
           whereBuf.append("(es.timeRangeIndex.isFloating=true and es.timeRangeIndex.startDate < '").append(filter.getFloatEnd()).append("'");
           whereBuf.append(" and es.timeRangeIndex.endDate > '").append(filter.getFloatStart()).append("')");

           whereBuf.append(" or (es.timeRangeIndex.isFloating=false and es.timeRangeIndex.startDate < '").append(filter.getUTCEnd()).append("'");
           whereBuf.append(" and es.timeRangeIndex.endDate > '").append(filter.getUTCStart()).append("')");

           // edge case where start==end
           whereBuf.append(" or (es.timeRangeIndex.startDate=es.timeRangeIndex.endDate and (es.timeRangeIndex.startDate='").append(filter.getFloatStart()).append("' or es.timeRangeIndex.startDate='").append(filter.getUTCStart()).append("'))");

           whereBuf.append(")");
        }
    }

    private void handleNoteItemFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, StringBuilder orderBuf,  HashMap<String, Object> params,
            NoteItemFilter filter) {
        selectBuf.append("select i from HibNoteItem i");
        handleItemFilter(selectBuf, whereBuf, params, filter);
        handleContentItemFilter(selectBuf, whereBuf, orderBuf, params, filter);

        // filter by icaluid
        if(filter.getIcalUid()!=null)
            formatExpression(whereBuf, params, "i.icalUid", filter.getIcalUid());

        // filter by body
        if(filter.getBody()!=null) {
            String alias = "ta" + params.size();
            selectBuf.append(", HibTextAttribute ").append(alias);
            appendWhere(whereBuf, alias + ".item=i and " + alias +".qname=:" + alias + "qname");
            params.put(alias + "qname", HibNoteItem.ATTR_NOTE_BODY);
            formatExpression(whereBuf, params, alias + ".value", filter.getBody());
        }

        // filter by reminderTime
        if(filter.getReminderTime()!=null) {
            String alias = "tsa" + params.size();
            selectBuf.append(", HibTimestampAttribute ").append(alias);
            appendWhere(whereBuf, alias + ".item=i and " + alias +".qname=:" + alias + "qname");
            params.put(alias + "qname", HibNoteItem.ATTR_REMINDER_TIME);
            formatExpression(whereBuf, params, alias + ".value", filter.getReminderTime());
        }

        //filter by master NoteItem
        if(filter.getMasterNoteItem()!=null) {
            appendWhere(whereBuf, "(i=:masterItem or i.modifies=:masterItem)");
            params.put("masterItem", filter.getMasterNoteItem());
        }

        // filter modifications
        if(filter.getIsModification()!=null) {
            if(filter.getIsModification())
                appendWhere(whereBuf,"i.modifies is not null");
            else
                appendWhere(whereBuf,"i.modifies is null");
        }

        if(filter.getHasModifications()!=null) {
            if(filter.getHasModifications())
                appendWhere(whereBuf,"size(i.modifications) > 0");
            else
                appendWhere(whereBuf,"size(i.modifications) = 0");
        }
    }

    private void handleContentItemFilter(StringBuilder selectBuf,
            StringBuilder whereBuf, StringBuilder orderBuf, HashMap<String, Object> params,
            ContentItemFilter filter) {

        if(selectBuf.toString().isEmpty()) {
            selectBuf.append("select i from HibContentItem i");
            handleItemFilter(selectBuf, whereBuf, params, filter);
        }
    }


    private void appendWhere(StringBuilder whereBuf, String toAppend) {
        if(whereBuf.toString().isEmpty())
            whereBuf.append(" where ").append(toAppend);
        else
            whereBuf.append(" and ").append(toAppend);
    }

    private void appendOrder(StringBuilder orderBuf, String toAppend) {
        if(orderBuf.toString().isEmpty())
            orderBuf.append(" order by ").append(toAppend);
        else
            orderBuf.append(", ").append(toAppend);
    }

    private String formatForLike(String toFormat) {
        return "%" + toFormat + "%";
    }

    /**
     * Because a timeRange query requires two passes: one to get the list
     * of possible events that occur in the range, and one
     * to expand recurring events if necessary.
     * This is required because we only index a start and end
     * for the entire recurrence series, and expansion is required to determine
     * if the event actually occurs, and to return individual occurences.
     */
    private HashSet<Item> processResults(List<Item> results, ItemFilter itemFilter) {
        boolean hasTimeRangeFilter = false;
        boolean includeMasterInResults = true;
        boolean doTimeRangeSecondPass = true;

        HashSet<Item> processedResults = new HashSet<>();
        EventStampFilter eventFilter = (EventStampFilter) itemFilter.getStampFilter(EventStampFilter.class);


        if(eventFilter!=null) {
            // does eventFilter have timeRange filter?
            hasTimeRangeFilter = (eventFilter.getPeriod() != null);
        }

        // When expanding recurring events do we include the master item in
        // the results, or just the expanded occurrences/modifications
        if(hasTimeRangeFilter && "false".equalsIgnoreCase(itemFilter
                .getFilterProperty(EventStampFilter.PROPERTY_INCLUDE_MASTER_ITEMS)))
            includeMasterInResults = false;

        // Should we do a second pass to expand recurring events to determine
        // if a recurring event actually occurs in the time-range specified,
        // or should we just return the recurring event without double-checking.
        if (hasTimeRangeFilter && "false".equalsIgnoreCase(itemFilter
                 .getFilterProperty(EventStampFilter.PROPERTY_DO_TIMERANGE_SECOND_PASS)))
            doTimeRangeSecondPass = false;

        for(Item item: results) {

            // If item is not a note, then nothing to do
            if(!(item instanceof NoteItem note)) {
                processedResults.add(item);
                continue;
            }

            // If note is a modification then add both the modification and the
            // master.
            if(note.getModifies()!=null) {
                processedResults.add(note);
                if(includeMasterInResults)
                    processedResults.add(note.getModifies());
            }
            // If filter doesn't have a timeRange, then we are done
            else if(!hasTimeRangeFilter)
                processedResults.add(note);
            else {
                processedResults.addAll(processMasterNote(note, eventFilter,
                        includeMasterInResults, doTimeRangeSecondPass));
            }
        }

        return processedResults;
    }

    private Collection<ContentItem> processMasterNote(NoteItem note,
            EventStampFilter filter, boolean includeMasterInResults,
            boolean doTimeRangeSecondPass) {
        EventStamp eventStamp = (EventStamp) note.getStamp(EventStamp.class);
        ArrayList<ContentItem> results = new ArrayList<>();

        // If the event is not recurring or the filter is configured
        // to not do a second pass then just return the note
        if (!eventStamp.isRecurring() || !doTimeRangeSecondPass) {
            results.add(note);
            return results;
        }

        // Otherwise, expand the recurring item to determine if it actually
        // occurs in the time range specified
        RecurrenceExpander expander = new RecurrenceExpander();
        InstanceList instances = expander.getOcurrences(eventStamp.getEvent(),
                eventStamp.getExceptions(), filter.getPeriod().getStart(),
                filter.getPeriod().getEnd(), filter.getTimezone());

        // If recurring event occurs in range, add master unless the filter
        // is configured to not return the master
        if (!instances.isEmpty() && includeMasterInResults)
            results.add(note);

        // If were aren't expanding, then return
        if(!filter.isExpandRecurringEvents())
            return results;

        // Otherwise, add an occurence item for each occurrence
        for (Entry<String, Instance> entry : instances.entrySet()) {
            // Ignore overrides as they are separate items that should have
            // already been added
            if (!entry.getValue().isOverridden()) {
                results.add(
                    NoteOccurrenceUtil.createNoteOccurrence(entry.getValue().getRid(), note));
            }
        }

        return results;
    }

    private void formatExpression(StringBuilder whereBuf,
            HashMap<String, Object> params, String propName,
            FilterCriteria fc) {

        StringBuilder expBuf = new StringBuilder();

        FilterExpression exp = (FilterExpression) fc;

        if (exp instanceof NullExpression) {
            expBuf.append(propName);
            if (exp.isNegated())
                expBuf.append(" is not null");
            else
                expBuf.append(" is null");
        } else if (exp instanceof BetweenExpression be) {
            expBuf.append(propName);
            if(exp.isNegated())
                expBuf.append(" not");

            String param = "param" + params.size();
            expBuf.append(" between :").append(param);
            params.put(param, be.getValue1());
            param = "param" + params.size();
            expBuf.append(" and :").append(param);
            params.put(param, be.getValue2());
        }
        else {
            String param = "param" + params.size();
            if (exp instanceof EqualsExpression) {
                expBuf.append(propName);
                if (exp.isNegated())
                    expBuf.append("!=");
                else
                    expBuf.append("=");

                params.put(param, exp.getValue());

            } else if (exp instanceof LikeExpression) {
                expBuf.append(propName);
                if (exp.isNegated())
                    expBuf.append(" not like ");
                else
                    expBuf.append(" like ");

                params.put(param, formatForLike(exp.getValue().toString()));
            } else if (exp instanceof ILikeExpression) {
                expBuf.append("lower(").append(propName).append(")");
                if (exp.isNegated())
                    expBuf.append(" not like ");
                else
                    expBuf.append(" like ");

                params.put(param, formatForLike(exp.getValue().toString().toLowerCase()));
            }

            expBuf.append(":").append(param);
        }

        appendWhere(whereBuf, expBuf.toString());
    }

}
