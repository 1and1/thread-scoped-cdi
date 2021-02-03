package com.oneandone.access.mobile.threadscoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aschoerk
 */
public final class ThreadScopedContextHolder {
    private static Logger logger = LoggerFactory.getLogger(ThreadScopedContextHolder.class);
    private static ThreadScopedContextHolder instance;
    //we will have only one instance of a type so the key is a class
    private static ThreadLocal<Map<Class, ThreadScopedInstance>> beans = new ThreadLocal<>();
    private static Map<Long, Map<Class, ThreadScopedInstance>> allThreadScopes = new ConcurrentHashMap<>();


    private ThreadScopedContextHolder() {
    }

    public static synchronized ThreadScopedContextHolder getInstance() {
        if (instance == null) {
            instance = new ThreadScopedContextHolder();
        }
        return instance;
    }

    public static Map<Class, ThreadScopedInstance> getBeans() {
        if (beans.get() == null) {
            final HashMap<Class, ThreadScopedInstance> map = new HashMap<>();
            beans.set(map);
            allThreadScopes.put(Thread.currentThread().getId(), map);
        }
        return beans.get();
    }

    public static void shutdown() {
        allThreadScopes.values().forEach(m -> clearMap(m));
    }

    public ThreadScopedInstance getBean(Class type) {
        return getBeans().get(type);
    }

    public void putBean(ThreadScopedInstance threadScopedInstance) {
        getBeans().put(threadScopedInstance.bean.getBeanClass(), threadScopedInstance);
    }

    void destroyBean(ThreadScopedInstance threadScopedInstance) {
        getBeans().remove(threadScopedInstance.bean.getBeanClass());
        threadScopedInstance.bean.destroy(threadScopedInstance.beanInstance, threadScopedInstance.ctx);
    }

    void clearMap() {
        Map<Class, ThreadScopedInstance> map = getBeans();
        clearMap(map);
        beans.set(null);
        allThreadScopes.remove(Thread.currentThread().getId());
    }

    private static void clearMap(final Map<Class, ThreadScopedInstance> map) {
        map.values().forEach(entry -> {
            try {
                entry.bean.destroy(entry.beanInstance, entry.ctx);
            } catch (Exception thw) {
                logger.error("Ignored Exception occurred while destroying bean",thw);
            }
        });
        map.clear();
    }

    public static class ThreadScopedInstance<T> {
        private Bean<T> bean;
        private CreationalContext<T> ctx;
        private T beanInstance;

        public Bean<T> getBean() {
            return bean;
        }

        public void setBean(final Bean<T> bean) {
            this.bean = bean;
        }

        public CreationalContext<T> getCtx() {
            return ctx;
        }

        public void setCtx(final CreationalContext<T> ctx) {
            this.ctx = ctx;
        }

        public T getBeanInstance() {
            return beanInstance;
        }

        public void setBeanInstance(final T beanInstance) {
            this.beanInstance = beanInstance;
        }
    }
}
