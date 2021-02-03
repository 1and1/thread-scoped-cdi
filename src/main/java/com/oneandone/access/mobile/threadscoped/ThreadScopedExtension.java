package com.oneandone.access.mobile.threadscoped;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

/**
 * @author aschoerk
 */
public class ThreadScopedExtension implements Extension {

    public void addScope(@Observes final BeforeBeanDiscovery event) {
        event.addScope(ThreadScoped.class, true, false);
    }

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
    }

    public void registerContext(@Observes final AfterBeanDiscovery event) {
        event.addContext(new ThreadScopedContext());
    }

    public void unregisterContext(@Observes final BeforeShutdown event) {
        ThreadScopedContextHolder.shutdown();
    }
}
