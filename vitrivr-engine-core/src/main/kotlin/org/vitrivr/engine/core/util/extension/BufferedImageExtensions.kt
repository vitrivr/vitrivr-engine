package org.vitrivr.engine.core.util.extension

import java.awt.image.BufferedImage

fun BufferedImage.getRGBArray(): IntArray = this.getRGB(0, 0, this.width, this.height, null, 0, this.width)

fun BufferedImage.setRGBArray(array: IntArray) = this.setRGB(0, 0, this.width, this.height, array, 0, this.width)