/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ThreadUtils {

    public static final int THRESHOLD = 200000;
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static <T> T forkJoin(ForkJoinTask<T> task) {
        return FORK_JOIN_POOL.invoke(task);
    }

    public static int getAvailableProcessors() {
        return PROCESSORS;
    }

    public static int getThreadNumber(int width, int height, int step) {
        int max = PROCESSORS;
        int num = (int) (((long) width * (long) height) / ((long) step * (long) step));
        return (num < 1) ? 1 : Math.min(num, max);
    }

    public static void joinThreads(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
    }
}
