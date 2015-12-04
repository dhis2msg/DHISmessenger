package org.dhis2.messenger.model;

/**
 * Created by Vladislav on 12/4/15.
 * An interface to copy all necessary attributes from one object to another of the same type.
 * Without changing the object's id (equals).
 */
public interface CopyAttributes<T> {

    void copyAttributesFrom(T other);
}
