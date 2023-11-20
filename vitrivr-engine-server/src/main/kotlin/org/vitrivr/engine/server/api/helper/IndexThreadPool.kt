package org.vitrivr.engine.server.api.helper

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.Temporal
import java.util.*


private val logger: KLogger = KotlinLogging.logger {}

class IndexThreadPool {
    companion object {
        val MAX_DURATION = Duration.ofHours(1);
        private val threads = mutableMapOf<String, Pair<Thread, Temporal?>>()

        fun addThreadAndStart(threadId: String, thread: Thread): String {
            val id = addThread(threadId, thread);
            val sid = startThread(id);
            return sid;
        }

        fun addThread(threadId: String, thread: Thread): String {
            val started = LocalDateTime.now();
            threads[threadId] = Pair(thread, started);
            return threadId;
        }

        fun startThread(uuid: String): String {
            val thread = threads[uuid];
            if (thread != null) {
                val (t, s) = thread;
                val started = LocalDateTime.now();
                t.start();
                threads[uuid] = Pair(t, started);
                return uuid;
            }
            return "";
        }

        fun addThread(thread: Thread): String {
            val myUuid = UUID.randomUUID().toString();
            return addThread(myUuid, thread);
        }


        fun getThreadState(uuid: String): Thread.State? {
            val thread = threads[uuid];
            if (thread != null) {
                val (t, started) = thread;
                return t.state
            }
            return null;
        }

        fun getAllThreadIds(): List<String> {
            return threads.keys.toList();
        }


        fun terminateThread(uuid: String) {
            val thread = threads[uuid];
            if (thread != null) {
                val (t, started) = thread;
                t.interrupt();
                threads.remove(uuid);
            }
        }

        fun cleanThreadPool(): List<String> {

            val ids = terminateThreads(ThreadState.DEAD);
            val idx = terminateThreads(ThreadState.DEPRECATED);
            return ids + idx;
        }

        enum class ThreadState {
            ALL, DEAD, DEPRECATED
        }

        fun terminateThreads(terminate: ThreadState): List<String> {
            val ids = mutableListOf<String>();
            threads.forEach { (uuid, thread) ->
                when (terminate) {
                    ThreadState.ALL -> {
                        ids.add(uuid);
                    }
                    ThreadState.DEAD -> {
                        if (!thread.first.isAlive) {
                            ids.add(uuid); }
                    }
                    ThreadState.DEPRECATED -> {
                        if (Duration.between(thread.second,  LocalDateTime.now()).compareTo(MAX_DURATION) > 0) {
                            ids.add(uuid);
                        }
                    }
                }
            }
            ids.forEach() { id ->
                terminateThread(id);
            }
            return ids;
        }
    }
}