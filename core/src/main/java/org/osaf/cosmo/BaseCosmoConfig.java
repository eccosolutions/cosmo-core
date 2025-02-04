package org.osaf.cosmo;

import java.util.function.Consumer;
import org.hibernate.Interceptor;
import org.osaf.cosmo.calendar.query.CalendarQueryProcessor;
import org.osaf.cosmo.calendar.query.impl.StandardCalendarQueryProcessor;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.dao.ServerPropertyDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.dao.hibernate.CalendarDaoImpl;
import org.osaf.cosmo.dao.hibernate.ContentDaoImpl;
import org.osaf.cosmo.dao.hibernate.DefaultItemPathTranslator;
import org.osaf.cosmo.dao.hibernate.ItemPathTranslator;
import org.osaf.cosmo.dao.hibernate.ServerPropertyDaoImpl;
import org.osaf.cosmo.dao.hibernate.UserDaoImpl;
import org.osaf.cosmo.dao.hibernate.query.ItemFilterProcessor;
import org.osaf.cosmo.dao.hibernate.query.StandardItemFilterProcessor;
import org.osaf.cosmo.model.hibernate.AuditableObjectInterceptor;
import org.osaf.cosmo.model.hibernate.EventStampInterceptor;
import org.osaf.cosmo.model.hibernate.HibEntityFactory;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardServerPropertyService;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.service.lock.LockManager;
import org.osaf.cosmo.service.lock.SingleVMLockManager;
import org.springframework.context.annotation.Bean;

/**
 * Note you need @EntityScan to include
 * <code>@EntityScan(basePackages = "org.osaf.cosmo.model")</code>
 */
public abstract class BaseCosmoConfig {

    /**
     * @param registerInterceptor method reference to method to add interceptors to the static interceptor list
     *                            of the class provided to Hibernate
     */
    protected BaseCosmoConfig(Consumer<Interceptor> registerInterceptor) {
        registerInterceptor.accept(new AuditableObjectInterceptor());
        registerInterceptor.accept(new EventStampInterceptor());
    }

    @Bean(initMethod="init", destroyMethod="destroy")
    public StandardServerPropertyService serverPropertyService(ServerPropertyDao serverPropertyDao) {
        var propertyService = new StandardServerPropertyService();
        propertyService.setServerPropertyDao(serverPropertyDao);
        return propertyService;
    }

    @Bean(initMethod="init", destroyMethod="destroy")
    public ServerPropertyDaoImpl serverPropertyDao() {
        return new ServerPropertyDaoImpl();
    }

    @Bean
    public HibEntityFactory cosmoEntityFactory() {
        return new HibEntityFactory();
    }

    @Bean(initMethod="init", destroyMethod="destroy")
    public StandardUserService cosmoUserService(ContentDao contentDao,
        UserDao cosmoUserDao) {
        var userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(cosmoUserDao);
        return userService;
    }

    @Bean(initMethod="init", destroyMethod="destroy")
    public ContentDaoImpl contentDao(ItemPathTranslator itemPathTranslator,
        ItemFilterProcessor itemFilterProcessor) {
        var dao = new ContentDaoImpl();
        dao.setItemPathTranslator(itemPathTranslator);
        dao.setItemFilterProcessor(itemFilterProcessor);
        return dao;
    }

    @Bean(initMethod="init", destroyMethod="destroy")
    public UserDaoImpl cosmoUserDao() {
        return new UserDaoImpl();
    }


    @Bean(initMethod="init", destroyMethod="destroy")
    public StandardContentService contentService(CalendarDao calendarDao,
        ContentDao contentDao,
        LockManager contentLockManager) {
        var contentService = new StandardContentService();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.setLockManager(contentLockManager);
        return contentService;
    }

    @Bean(initMethod="init")
    public CalendarDaoImpl calendarDao(ItemFilterProcessor itemFilterProcessor) {
        var calendarDao = new CalendarDaoImpl();
        calendarDao.setItemFilterProcessor(itemFilterProcessor);
        return calendarDao;
    }
    @Bean
    public LockManager contentLockManager() {
        return new SingleVMLockManager();
    }
    @Bean
    public ItemFilterProcessor standardItemFilterProcessor() {
        return new StandardItemFilterProcessor();
    }

    @Bean
    public ItemPathTranslator itemPathTranslator() {
        return new DefaultItemPathTranslator();
    }

    @Bean
    public CalendarQueryProcessor calendarQueryProcessor(CalendarDao calendarDao,
        ContentDao contentDao) {
        var processor = new StandardCalendarQueryProcessor();
        processor.setCalendarDao(calendarDao);
        processor.setContentDao(contentDao);
        return processor;
    }
}
