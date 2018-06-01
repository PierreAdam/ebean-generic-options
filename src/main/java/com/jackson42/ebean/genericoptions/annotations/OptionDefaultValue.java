/*
 * Copyright (C) 2014 - 2018 PayinTech, SAS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.jackson42.ebean.genericoptions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OptionDefaultValue.
 *
 * @author Pierre Adam
 * @since 18.05.30
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionDefaultValue {

    /**
     * The default value.
     *
     * @return The default value
     */
    String defaultValue() default "";

    /**
     * The class of the default value.
     *
     * @return The class of the default value
     */
    Class<?>[] type() default {String.class};
}
