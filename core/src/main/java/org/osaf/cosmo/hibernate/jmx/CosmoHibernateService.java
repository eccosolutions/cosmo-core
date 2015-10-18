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
package org.osaf.cosmo.hibernate.jmx;

import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

/**
 * Implementation of {@link CosmoHibernateServiceMBean}
 * @author bobbyrullo
 */
public class CosmoHibernateService implements CosmoHibernateServiceMBean {
    private SessionFactory sessionFactory;
    private Statistics statistics;
    
    /**
     * This needs to be done because the {@link Statistics} does not expose
     * getters to the session factory.
     */
    public void setSessionFactory(SessionFactory sf){
        this.sessionFactory = sf;
        this.statistics = sf.getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    public void evictEntity(String entityName) {
        try {
            this.sessionFactory.evictEntity(entityName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        statistics.clear();
    }

    @Override
    public EntityStatistics getEntityStatistics(String entityName) {
        return statistics.getEntityStatistics(entityName);
    }

    @Override
    public CollectionStatistics getCollectionStatistics(String role) {
        return statistics.getCollectionStatistics(role);
    }

    @Override
    public SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
        return statistics.getSecondLevelCacheStatistics(regionName);
    }

    @Override
    public NaturalIdCacheStatistics getNaturalIdCacheStatistics(String regionName) {
        return statistics.getNaturalIdCacheStatistics(regionName);
    }

    @Override
    public QueryStatistics getQueryStatistics(String queryString) {
        return statistics.getQueryStatistics(queryString);
    }

    @Override
    public long getEntityDeleteCount() {
        return statistics.getEntityDeleteCount();
    }

    @Override
    public long getEntityInsertCount() {
        return statistics.getEntityInsertCount();
    }

    @Override
    public long getEntityLoadCount() {
        return statistics.getEntityLoadCount();
    }

    @Override
    public long getEntityFetchCount() {
        return statistics.getEntityFetchCount();
    }

    @Override
    public long getEntityUpdateCount() {
        return statistics.getEntityUpdateCount();
    }

    @Override
    public long getQueryExecutionCount() {
        return statistics.getQueryExecutionCount();
    }

    @Override
    public long getQueryExecutionMaxTime() {
        return statistics.getQueryExecutionMaxTime();
    }

    @Override
    public String getQueryExecutionMaxTimeQueryString() {
        return statistics.getQueryExecutionMaxTimeQueryString();
    }

    @Override
    public long getQueryCacheHitCount() {
        return statistics.getQueryCacheHitCount();
    }

    @Override
    public long getQueryCacheMissCount() {
        return statistics.getQueryCacheMissCount();
    }

    @Override
    public long getQueryCachePutCount() {
        return statistics.getQueryCachePutCount();
    }

    @Override
    public long getNaturalIdQueryExecutionCount() {
        return statistics.getNaturalIdQueryExecutionCount();
    }

    @Override
    public long getNaturalIdQueryExecutionMaxTime() {
        return statistics.getNaturalIdQueryExecutionMaxTime();
    }

    @Override
    public String getNaturalIdQueryExecutionMaxTimeRegion() {
        return statistics.getNaturalIdQueryExecutionMaxTimeRegion();
    }

    @Override
    public long getNaturalIdCacheHitCount() {
        return statistics.getNaturalIdCacheHitCount();
    }

    @Override
    public long getNaturalIdCacheMissCount() {
        return statistics.getNaturalIdCacheMissCount();
    }

    @Override
    public long getNaturalIdCachePutCount() {
        return statistics.getNaturalIdCachePutCount();
    }

    @Override
    public long getUpdateTimestampsCacheHitCount() {
        return statistics.getUpdateTimestampsCacheHitCount();
    }

    @Override
    public long getUpdateTimestampsCacheMissCount() {
        return statistics.getUpdateTimestampsCacheMissCount();
    }

    @Override
    public long getUpdateTimestampsCachePutCount() {
        return statistics.getUpdateTimestampsCachePutCount();
    }

    @Override
    public long getFlushCount() {
        return statistics.getFlushCount();
    }

    @Override
    public long getConnectCount() {
        return statistics.getConnectCount();
    }

    @Override
    public long getSecondLevelCacheHitCount() {
        return statistics.getSecondLevelCacheHitCount();
    }

    @Override
    public long getSecondLevelCacheMissCount() {
        return statistics.getSecondLevelCacheMissCount();
    }

    @Override
    public long getSecondLevelCachePutCount() {
        return statistics.getSecondLevelCachePutCount();
    }

    @Override
    public long getSessionCloseCount() {
        return statistics.getSessionCloseCount();
    }

    @Override
    public long getSessionOpenCount() {
        return statistics.getSessionOpenCount();
    }

    @Override
    public long getCollectionLoadCount() {
        return statistics.getCollectionLoadCount();
    }

    @Override
    public long getCollectionFetchCount() {
        return statistics.getCollectionFetchCount();
    }

    @Override
    public long getCollectionUpdateCount() {
        return statistics.getCollectionUpdateCount();
    }

    @Override
    public long getCollectionRemoveCount() {
        return statistics.getCollectionRemoveCount();
    }

    @Override
    public long getCollectionRecreateCount() {
        return statistics.getCollectionRecreateCount();
    }

    @Override
    public long getStartTime() {
        return statistics.getStartTime();
    }

    @Override
    public void logSummary() {
        statistics.logSummary();
    }

    @Override
    public boolean isStatisticsEnabled() {
        return statistics.isStatisticsEnabled();
    }

    @Override
    public void setStatisticsEnabled(boolean b) {
        statistics.setStatisticsEnabled(b);
    }

    @Override
    public String[] getQueries() {
        return statistics.getQueries();
    }

    @Override
    public String[] getEntityNames() {
        return statistics.getEntityNames();
    }

    @Override
    public String[] getCollectionRoleNames() {
        return statistics.getCollectionRoleNames();
    }

    @Override
    public String[] getSecondLevelCacheRegionNames() {
        return statistics.getSecondLevelCacheRegionNames();
    }

    @Override
    public long getSuccessfulTransactionCount() {
        return statistics.getSuccessfulTransactionCount();
    }

    @Override
    public long getTransactionCount() {
        return statistics.getTransactionCount();
    }

    @Override
    public long getPrepareStatementCount() {
        return statistics.getPrepareStatementCount();
    }

    @Override
    public long getCloseStatementCount() {
        return statistics.getCloseStatementCount();
    }

    @Override
    public long getOptimisticFailureCount() {
        return statistics.getOptimisticFailureCount();
    }
}
