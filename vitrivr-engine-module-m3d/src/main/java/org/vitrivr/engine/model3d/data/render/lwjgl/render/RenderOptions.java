
package org.vitrivr.engine.model3d.data.render.lwjgl.render;

import java.io.Serializable;
import java.util.function.Function;
import org.joml.Vector4f;

/**
 * RenderOptions
 * <ul>
 * <li>Used to switch on or off the texture rendering</li>
 * <li>Used to switch on or off the coloring rendering</li>
 * <li>Returns the color for the given value</li>
 * <li>Can be used to colorize the model custom</li>
 * </ul>
 */
public class RenderOptions implements Serializable {

  /**
   * Used to switch on or off the texture rendering
   */
  public boolean showTextures = true;

  /**
   * Used to switch on or off the coloring rendering For future face coloring
   */
  @SuppressWarnings("unused")
  public boolean showColor = false;

  /**
   * Returns the color for the given value Can be used to colorize the model custom
   *
   * @TODO: This cannot be serialized!
   */
  public transient Function<Float, Vector4f> colorfunction = (v) -> new Vector4f(v, v, v, 1f);
}
