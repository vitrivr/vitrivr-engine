package org.vitrivr.engine.core.resolver

import java.nio.file.Path;


/**
 * [Resolver] backed by a local filesystem directory.
 *
 * @author Andrina Geller
 * @version 1.0.0
 */
interface PathResolver : Resolver {
    val root: Path
}