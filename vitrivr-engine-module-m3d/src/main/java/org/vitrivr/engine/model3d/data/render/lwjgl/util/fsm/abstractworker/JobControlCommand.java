package org.vitrivr.engine.model3d.data.render.lwjgl.util.fsm.abstractworker;


/**
 * JobControlCommand are used to control the worker thread.
 */
public enum JobControlCommand {
  /**
   * Is used to return the end of the job
   */
  JOB_DONE,

  /**
   * Is used to return an error on the job
   */
  JOB_FAILURE,


  /**
   * Is used to shut down the worker
   */
  SHUTDOWN_WORKER,
}
