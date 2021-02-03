package com.oneandone.access.mobile.threadscoped;

import org.jboss.weld.context.ManagedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;

public class ThreadScopedContext implements ManagedContext {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    private final ThreadScopedContextHolder threadScopedContextHolder;
    private final ThreadLocal<Boolean> active = new ThreadLocal<>();
    private final ThreadLocal<Boolean> valid = new ThreadLocal<Boolean>();


    public ThreadScopedContext() {
        log.info("Init ThreadScopedContext");
        this.threadScopedContextHolder = ThreadScopedContextHolder.getInstance();
    }

    @Override
    public <T> T get(final Contextual<T> contextual) {
        if(!isActive()) {
            log.error("Using inactive ThreadScoped-Context");
            return null;
        }
        Bean bean = (Bean) contextual;
        if(ThreadScopedContextHolder.getBeans().containsKey(bean.getBeanClass())) {
            return (T) threadScopedContextHolder.getBean(bean.getBeanClass()).getBeanInstance();
        }
        else {
            return null;
        }
    }

    @Override
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        if(!isActive()) {
            log.error("Using inactive ThreadScoped-Context");
            return null;
        }
        Bean bean = (Bean) contextual;
        if(threadScopedContextHolder.getBeans().containsKey(bean.getBeanClass())) {
            return (T) threadScopedContextHolder.getBean(bean.getBeanClass()).getBeanInstance();
        }
        else {
            T t = (T) bean.create(creationalContext);
            ThreadScopedContextHolder.ThreadScopedInstance threadScopedInstance = new ThreadScopedContextHolder.ThreadScopedInstance();
            threadScopedInstance.setBean(bean);
            threadScopedInstance.setCtx(creationalContext);
            threadScopedInstance.setBeanInstance(t);
            threadScopedContextHolder.putBean(threadScopedInstance);
            return t;
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ThreadScoped.class;
    }

    @Override
    public boolean isActive() {
        if(active.get() == null) {
            // first use in thread, make active
            return true;
        }
        return active.get();
    }

    private boolean isValid() {
        if(valid.get() == null) {
            valid.set(false);
        }
        return valid.get();
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        Bean bean = (Bean) contextual;
        if(threadScopedContextHolder.getBeans().containsKey(bean.getBeanClass())) {
            threadScopedContextHolder.destroyBean(threadScopedContextHolder.getBean(bean.getBeanClass()));
        }
    }

    @Override
    public void activate() {
        log.trace("activating ThreadScopedContext");
        if(active.get() == null) {
            active.set(false);
        }
        if(isActive()) {
            log.error("Trying to activate active ThreadScoped-Context");
        }
        active.set(true);
        valid.set(true);
        this.threadScopedContextHolder.clearMap();
    }

    @Override
    public void deactivate() {
        if(!isActive()) {
            log.error("Deactivating inactive ThreadScoped-Context");
        }
        log.trace("deactivating ThreadScopedContext");
        if(!isValid()) {
            threadScopedContextHolder.clearMap();
        }
        active.set(false);
    }

    @Override
    public void invalidate() {
        if(!isValid()) {
            log.error("Invalidating invalid ThreadScoped-Context");
        }
        log.trace("invalidating ThreadScopedContext");
        valid.set(false);
    }

}
