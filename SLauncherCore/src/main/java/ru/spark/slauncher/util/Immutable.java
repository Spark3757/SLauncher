package ru.spark.slauncher.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Mark if instances of the class are immutable.
 *
 * @author Spark1337
 */
@Target(ElementType.TYPE)
public @interface Immutable {
}
