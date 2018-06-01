/*
 * Copyright (C) 2014 - 2018 PayinTech, SAS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.jackson42.ebean.genericoptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackson42.ebean.genericoptions.annotations.OptionDefaultValue;
import com.jackson42.ebean.genericoptions.exception.BadTypeException;
import com.jackson42.ebean.genericoptions.exception.BadValueException;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.EnumValue;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * GenericOption.
 * Must be extended by a model that use option.
 *
 * @author Pierre Adam
 * @since 18.05.30
 */
@MappedSuperclass
public abstract class GenericOption<T extends Enum<T>> extends Model {

    /**
     * The class of the enum used as option.
     */
    @Transient
    private final Class<T> tClass;

    /**
     * The key of option.
     */
    @Size(max = 32)
    @Column(name = "opt_key", nullable = false, unique = false)
    protected String key;

    /**
     * The value of the option.
     */
    @Column(name = "opt_value", nullable = true, unique = false, columnDefinition = "TEXT")
    protected String value;

    /**
     * Basic constructor.
     * Should not be used manually !
     *
     * @param tClass The class
     */
    public GenericOption(final Class<T> tClass) {
        this.tClass = tClass;
    }

    /**
     * Constructor.
     *
     * @param tClass The class
     */
    public GenericOption(final Class<T> tClass, final T key) {
        this(tClass);
        this.key = GenericOption.valueFromEnum(this.tClass, key);
    }

    /**
     * Apply a where close to an ebean query.
     *
     * @param <T>    The type of the key
     * @param <U>    The type of the result from the query
     * @param tClass The class of the key
     * @param query  The original query
     * @param key    The key to search
     * @return The query with the search close
     */
    @Transient
    public static <T extends Enum<T>, U extends GenericOption<T>> ExpressionList<U> whereKey(
            final Class<T> tClass,
            final ExpressionList<U> query, final T key) {
        return query.eq("key", GenericOption.valueFromEnum(tClass, key));
    }

    /**
     * Give the textual representation of an enum.
     * Is the opposite of the function enumFromValue.
     *
     * @param <T>    The type of the enum
     * @param tClass The class of the enum
     * @param key    The value in the enum
     * @return The textual representation
     */
    @Transient
    protected static <T extends Enum<T>> String valueFromEnum(final Class<T> tClass, final T key) {
        String strKey = key.toString();
        try {
            final EnumValue annotation = tClass.getField(key.name()).getAnnotation(EnumValue.class);
            if (annotation != null) {
                strKey = annotation.value();
            }
        } catch (final NoSuchFieldException e) {
            return strKey;
        }
        return strKey;
    }

    /**
     * Give the enum from a textual representation of this enum.
     * Is the opposite of the function valueFromEnum.
     *
     * @param <T>    The type of the enum
     * @param tClass The class T
     * @param value  The textual representation
     * @return Tfhe enum or null
     */
    @Transient
    protected static <T extends Enum<T>> T enumFromValue(final Class<T> tClass, final String value) {
        for (final Field field : tClass.getFields()) {
            final EnumValue annotation = field.getAnnotation(EnumValue.class);
            if (annotation != null && annotation.value().equals(value)) {
                return T.valueOf(tClass, field.getName());
            }
        }
        return T.valueOf(tClass, value);
    }

    /**
     * Get the raw value for the key.
     *
     * @return the raw value of the key
     */
    @Transient
    public String getRawKey() {
        return this.key;
    }

    /**
     * Set the raw value for the key.
     * Should not be used outside of this class or outside of the daughter classes.
     * Using this method without understanding the consequences can induce some critical side effect.
     *
     * @param rawKey The new key.
     */
    @Transient
    protected void setRawKey(final String rawKey) {
        this.key = rawKey;
    }

    /**
     * Get the raw data for the value.
     *
     * @return The raw value
     */
    @Transient
    public String getRawValue() {
        return this.value;
    }

    /**
     * Set the raw data for the value.
     * Should not be used outside of this class or outside of the daughter classes.
     * Using this method without understanding the consequences can induce some critical side effect.
     *
     * @param rawValue The new value
     */
    @Transient
    protected void setRawValue(final String rawValue) {
        this.value = rawValue;
    }

    /**
     * Get the key.
     *
     * @return The key option
     */
    @Transient
    public T getKeyEnum() {
        return GenericOption.enumFromValue(this.tClass, this.key);
    }

    /**
     * Set the key.
     * Should not be used outside of this class or outside of the daughter classes.
     * Using this method without understanding the consequences can induce some critical side effect.
     *
     * @param keyEnum The key to set
     */
    @Transient
    protected void setKeyEnum(final T keyEnum) {
        this.key = GenericOption.valueFromEnum(this.tClass, keyEnum);
    }

    /**
     * Get the value as string.
     *
     * @return The value
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public String getValueAsString() throws BadTypeException, BadValueException {
        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                return this.value;
            } else {
                if (optionDefaultValue == null) {
                    return "";
                } else {
                    return optionDefaultValue.defaultValue();
                }
            }
        }, String.class);
    }

    /**
     * Set the value as string
     *
     * @param valueAsString The value
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsString(final String valueAsString) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = valueAsString, String.class);
    }

    /**
     * Get the value as long.
     *
     * @return The value as long
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public Boolean getValueAsBoolean() throws BadTypeException, BadValueException {
        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                return Boolean.valueOf(this.value);
            } else {
                if (optionDefaultValue == null) {
                    return false;
                } else {
                    return Boolean.valueOf(optionDefaultValue.defaultValue());
                }
            }
        }, Boolean.class);
    }

    /**
     * Set the value as a boolean
     *
     * @param valueAsBoolean The value
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsBoolean(final Boolean valueAsBoolean) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = valueAsBoolean.toString(), Boolean.class);
    }

    /**
     * Get the value as integer.
     *
     * @return The value as integer
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public Integer getValueAsInteger() throws BadTypeException, BadValueException {
        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                try {
                    return Integer.valueOf(this.value);
                } catch (final NumberFormatException e) {
                    try {
                        return Integer.valueOf(optionDefaultValue.defaultValue());
                    } catch (final NumberFormatException e2) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing a Integer default option value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e2);
                    }
                }
            } else {
                if (optionDefaultValue == null) {
                    return 0;
                } else {
                    try {
                        return Integer.valueOf(optionDefaultValue.defaultValue());
                    } catch (final NumberFormatException e) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing a Integer default option value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e
                        );
                    }
                }
            }
        }, Integer.class);
    }

    /**
     * Set the value as integer
     *
     * @param valueAsInteger The value
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsInteger(final Integer valueAsInteger) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = valueAsInteger.toString(), Integer.class);
    }

    /**
     * Get the value as long.
     *
     * @return The value as long
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public Long getValueAsLong() throws BadTypeException, BadValueException, BadValueException {
        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                try {
                    return Long.valueOf(this.value);
                } catch (final NumberFormatException e) {
                    try {
                        return Long.valueOf(optionDefaultValue.defaultValue());
                    } catch (final NumberFormatException e2) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing a Long default option value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e2
                        );
                    }
                }
            } else {
                if (optionDefaultValue == null) {
                    return 0L;
                } else {
                    try {
                        return Long.valueOf(optionDefaultValue.defaultValue());
                    } catch (final NumberFormatException e) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing a Long default option value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e
                        );
                    }
                }
            }
        }, Long.class);
    }

    /**
     * Set the value as long
     *
     * @param valueAsLong The value
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsLong(final Long valueAsLong) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = valueAsLong.toString(), Long.class);
    }

    /**
     * Get the value as a list of long.
     *
     * @return The value as a list of long
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public List<Long> getValueAsLongList() throws BadTypeException, BadValueException {
        return this.getValueAsSimpleList(Long.class);
    }

    /**
     * Set the value from a list of long.
     *
     * @param list The list of long
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsLongList(final List<Long> list) throws BadTypeException, BadValueException {
        this.setValueAsSimpleList(Long.class, list);
    }

    /**
     * Get the value as a list of integer.
     *
     * @return The value as a list of integer
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public List<Integer> getValueAsIntegerList() throws BadTypeException, BadValueException {
        return this.getValueAsSimpleList(Integer.class);
    }

    /**
     * Set the value from a list of integer.
     *
     * @param list The list of integer
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsIntegerList(final List<Integer> list) throws BadTypeException, BadValueException {
        this.setValueAsSimpleList(Integer.class, list);
    }

    /**
     * Get the value as a list of string.
     *
     * @return The value as a list of string
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public List<String> getValueAsStringList() throws BadTypeException, BadValueException {
        return this.getValueAsSimpleList(String.class);
    }

    /**
     * Set the value from a list of string.
     *
     * @param list The list of string
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public void setValueAsStringList(final List<String> list) throws BadTypeException, BadValueException {
        this.setValueAsSimpleList(String.class, list);
    }

    /**
     * Get the value as a list of U
     *
     * @param uClass The class of U
     * @param <U>    The type of the list
     * @return The value as a list of U
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <U> List<U> getValueAsSimpleList(final Class<U> uClass) throws BadTypeException, BadValueException {
        final ObjectMapper mapper = new ObjectMapper();
        final JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, uClass);

        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                try {
                    return mapper.readValue(this.value, type);
                } catch (final IOException e) {
                    try {
                        return mapper.readValue(optionDefaultValue.defaultValue(), type);
                    } catch (final IOException e2) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing the JSON of the default value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e2
                        );
                    }
                }
            } else {
                if (optionDefaultValue == null) {
                    return new ArrayList<U>();
                } else {
                    try {
                        return mapper.readValue(optionDefaultValue.defaultValue(), type);
                    } catch (final IOException e) {
                        throw new BadValueException(
                                String.format(
                                        "%s - Error occurred while parsing the JSON of the default value : %s -> %s",
                                        this.tClass.getName(),
                                        this.getRawKey(),
                                        optionDefaultValue.defaultValue()
                                ),
                                e
                        );
                    }
                }
            }
        }, List.class, uClass);
    }

    /**
     * Set the value from a list of U.
     *
     * @param uClass The class of U
     * @param list   The list of U
     * @param <U>    The type of the list
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    public <U> void setValueAsSimpleList(final Class<U> uClass, final List<U> list) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                this.value = mapper.writeValueAsString(list);
            } catch (final JsonProcessingException e) {
                throw new BadValueException(
                        String.format(
                                "%s - Error occurred while mapping List<%s> to json. Key : %s",
                                this.tClass.getName(),
                                uClass.getName(),
                                this.getRawKey()
                        ),
                        e
                );
            }
        }, List.class, uClass);
    }

    /**
     * Get the value as an enum U.
     *
     * @param uClass The class of U
     * @param <U>    The type of the enum
     * @return The enum U or null
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <U extends Enum<U>> U getValueAsEnum(final Class<U> uClass) throws BadTypeException, BadValueException {
        return this.safeGet(optionDefaultValue -> {
            if (this.value != null) {
                return GenericOption.enumFromValue(uClass, this.value);
            } else {
                if (optionDefaultValue == null) {
                    return null;
                } else {
                    return GenericOption.enumFromValue(uClass, optionDefaultValue.defaultValue());
                }
            }
        }, uClass);
    }

    /**
     * Set the value as an enum U.
     *
     * @param uClass      The class of U
     * @param valueAsEnum The value
     * @param <U>         The type of the enum
     * @throws BadTypeException  If the types doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <U extends Enum<U>> void setValueAsEnum(final Class<U> uClass,
                                                      final U valueAsEnum) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = GenericOption.valueFromEnum(uClass, valueAsEnum), uClass);
    }

    /**
     * Get the value as a instance of model from the given finder.
     *
     * @param uClass           the class of U
     * @param finder           the finder for U
     * @param keyParser        function used to parse the key from a string
     * @param idColumnName     name of the id column in the model
     * @param queryExtraFilter nullable consumer that let you add close to the Ebean query
     * @param <U>              the type of the model
     * @param <V>              the type of the key
     * @return the instance of the model or null.
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <U extends Model, V> U getValueAsEbeanModel(final Class<U> uClass,
                                                          final Finder<V, U> finder,
                                                          final Function<String, V> keyParser,
                                                          final String idColumnName,
                                                          final Consumer<ExpressionList<U>> queryExtraFilter) throws BadTypeException, BadValueException {
        final Function<String, U> solveModel = idStr -> {
            final V id = keyParser.apply(idStr);
            if (id == null) {
                return null;
            }
            final ExpressionList<U> query = finder.query().where()
                    .eq(idColumnName, id);
            if (queryExtraFilter != null) {
                queryExtraFilter.accept(query);
            }
            return query.findOne();
        };
        return this.safeGet(optionDefaultValue -> {
            if (this.value == null || this.value.isEmpty()) {
                return solveModel.apply(optionDefaultValue.defaultValue());
            } else {
                return solveModel.apply(this.value);
            }
        }, uClass);
    }

    /**
     * Get the value as a instance of model from the given finder.
     *
     * @param uClass       the class of U
     * @param valueAsModel the model
     * @param keyToStr     the function to translate the key to a string
     * @param <U>          the type of the model
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <U extends Model> void setValueAsEbeanModel(final Class<U> uClass,
                                                          final U valueAsModel, final Function<U, String> keyToStr) throws BadTypeException, BadValueException {
        this.safeSet(optionDefaultValue -> this.value = keyToStr.apply(valueAsModel), uClass);
    }

    /**
     * Get and check the context defined by the annotation OptionDefaultValue.
     *
     * @param getter The actual getter
     * @param types  The types to check
     * @param <R>    The type of the result
     * @return The result of the getter
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected <R> R safeGet(final Function<OptionDefaultValue, R> getter,
                            final Class<?>... types) throws BadTypeException, BadValueException {
        final OptionDefaultValue optionDefaultValue = this.getOptionDefaultValue();
        if (optionDefaultValue == null || this.isTypeCorrect(optionDefaultValue, types)) {
            return getter.apply(optionDefaultValue);
        } else {
            throw new BadTypeException();
        }
    }

    /**
     * Set and check the context defined by the annotation OptionDefaultValue.
     *
     * @param setter The actual getter
     * @param types  The types to check
     * @throws BadTypeException  If the type doesn't match
     * @throws BadValueException If the value is not valid of parsable
     */
    @Transient
    protected void safeSet(final Consumer<OptionDefaultValue> setter,
                           final Class<?>... types) throws BadTypeException, BadValueException {
        final OptionDefaultValue optionDefaultValue = this.getOptionDefaultValue();
        if (optionDefaultValue == null || this.isTypeCorrect(optionDefaultValue, types)) {
            setter.accept(optionDefaultValue);
        } else {
            throw new BadTypeException();
        }
    }

    /**
     * Get the annotation OptionDefaultValue of the current key.
     *
     * @return The annotation OptionDefaultValue
     * @see OptionDefaultValue
     */
    @Transient
    protected OptionDefaultValue getOptionDefaultValue() {
        try {
            return this.tClass.getField(this.getKeyEnum().name()).getAnnotation(OptionDefaultValue.class);
        } catch (final NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Check is the type match the option default value annotation type.
     *
     * @param optionDefaultValue The annotation
     * @param classes            The expected classes
     * @return {@code true} if it match
     */
    @Transient
    protected boolean isTypeCorrect(final OptionDefaultValue optionDefaultValue, final Class<?>... classes) {
        if (optionDefaultValue == null) {
            return false;
        }
        final Class<?>[] type = optionDefaultValue.type();

        return Arrays.equals(type, classes);
    }
}
