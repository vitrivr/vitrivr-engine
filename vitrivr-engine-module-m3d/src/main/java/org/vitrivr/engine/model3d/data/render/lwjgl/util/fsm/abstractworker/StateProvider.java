package org.vitrivr.engine.model3d.data.render.lwjgl.util.fsm.abstractworker;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation a class can be marked as a state provider.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StateProvider {
}

