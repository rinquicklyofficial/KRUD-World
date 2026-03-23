/**
 * KRUD World — World Generator
 * Copyright (C) 2026 Krud Studio
 *
 * Based on KrudWorld World Generator:
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 * https://github.com/VolmitSoftware/KrudWorld
 * License: GPL-3.0
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License.
 */

package dev.krud.world.util.scheduling;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.function.NastyRunnable;
import dev.krud.world.util.math.M;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

public class TaskExecutor {
    private final ExecutorService service;
    private int xc;

    public TaskExecutor(int threadLimit, int priority, String name) {
        xc = 1;

        if (threadLimit == 1) {
            service = Executors.newSingleThreadExecutor((r) ->
            {
                Thread t = new Thread(r);
                t.setName(name);
                t.setPriority(priority);

                return t;
            });
        } else if (threadLimit > 1) {
            final ForkJoinWorkerThreadFactory factory = pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName(name + " " + xc++);
                worker.setPriority(priority);
                return worker;
            };

            service = new ForkJoinPool(threadLimit, factory, null, false);
        } else {
            service = Executors.newCachedThreadPool((r) ->
            {
                Thread t = new Thread(r);
                t.setName(name + " " + xc++);
                t.setPriority(priority);

                return t;
            });
        }
    }

    public TaskGroup startWork() {
        return new TaskGroup(this);
    }

    public void close() {
        J.a(() ->
        {
            J.sleep(10000);
            service.shutdown();
        });
    }

    public void closeNow() {
        service.shutdown();
    }

    public enum TaskState {
        QUEUED,
        RUNNING,
        COMPLETED,
        FAILED
    }

    public static class TaskGroup {
        private final KList<AssignedTask> tasks;
        private final TaskExecutor e;

        public TaskGroup(TaskExecutor e) {
            tasks = new KList<>();
            this.e = e;
        }

        public TaskGroup queue(NastyRunnable... r) {
            for (NastyRunnable i : r) {
                tasks.add(new AssignedTask(i));
            }

            return this;
        }

        public TaskGroup queue(KList<NastyRunnable> r) {
            for (NastyRunnable i : r) {
                tasks.add(new AssignedTask(i));
            }

            return this;
        }

        public TaskResult execute() {
            double timeElapsed = 0;
            int tasksExecuted = 0;
            int tasksFailed = 0;
            int tasksCompleted = 0;
            tasks.forEach((t) -> t.go(e));
            long msv = M.ns();

            waiting:
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(0);
                } catch (InterruptedException ignored) {

                }

                for (AssignedTask i : tasks) {
                    if (i.state.equals(TaskState.QUEUED) || i.state.equals(TaskState.RUNNING)) {
                        continue waiting;
                    }
                }

                timeElapsed = (double) (M.ns() - msv) / 1000000D;

                for (AssignedTask i : tasks) {
                    if (i.state.equals(TaskState.COMPLETED)) {
                        tasksCompleted++;
                    } else {
                        tasksFailed++;
                    }

                    tasksExecuted++;
                }

                break;
            }

            return new TaskResult(timeElapsed, tasksExecuted, tasksFailed, tasksCompleted);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @ToString
    public static class TaskResult {
        public final double timeElapsed;
        public final int tasksExecuted;
        public final int tasksFailed;
        public final int tasksCompleted;

        public TaskResult(double timeElapsed, int tasksExecuted, int tasksFailed, int tasksCompleted) {
            this.timeElapsed = timeElapsed;
            this.tasksExecuted = tasksExecuted;
            this.tasksFailed = tasksFailed;
            this.tasksCompleted = tasksCompleted;
        }
    }

    public static class AssignedTask {
        @Getter
        private final NastyRunnable task;
        @Getter
        @Setter
        private TaskState state;

        public AssignedTask(NastyRunnable task) {
            this.task = task;
            state = TaskState.QUEUED;
        }

        public void go(TaskExecutor e) {
            e.service.execute(() ->
            {
                state = TaskState.RUNNING;
                try {
                    task.run();
                    state = TaskState.COMPLETED;
                } catch (Throwable ex) {
                    KrudWorld.reportError(ex);
                    ex.printStackTrace();
                    KrudWorld.reportError(ex);
                    state = TaskState.FAILED;
                }
            });
        }
    }
}
