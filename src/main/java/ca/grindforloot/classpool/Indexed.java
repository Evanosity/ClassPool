package ca.grindforloot.classpool;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This marks classes that can be discovered with a ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {

}
