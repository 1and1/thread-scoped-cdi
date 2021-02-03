package com.oneandone.access.mobile.threadscoped;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author aschoerk
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Inherited
public @interface ThreadScoped {
}
