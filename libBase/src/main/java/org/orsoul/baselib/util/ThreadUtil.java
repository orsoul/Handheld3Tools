package org.orsoul.baselib.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadUtil {
    private static ExecutorService threadPool;
    private static ExecutorService singleThreadPool;

    public static void execute(Runnable task) {
        if (threadPool == null) {
            synchronized (ThreadUtil.class) {
                if (threadPool == null) {
                    threadPool = Executors.newCachedThreadPool(
                            getNameFormatThreadFactory("CachePool"));
                }
            }
        }
        threadPool.execute(task);
    }

    public static void executeInSingleThread(Runnable task) {
        if (singleThreadPool == null) {
            synchronized (ThreadUtil.class) {
                if (singleThreadPool == null) {
                    singleThreadPool = Executors.newSingleThreadExecutor(
                            getNameFormatThreadFactory("SingleThread")
                    );
                }
            }
        }
        singleThreadPool.execute(task);
    }

    public static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static void interrupt(Thread t) {
        try {
            t.interrupt();
        } catch (Exception e) {
        }
    }

    public static void stop(Thread t, long millis) {
        interrupt(t);

        try {
            t.join(millis);
        } catch (Exception e) {
        }
    }

    public static void waitObject(Object obj) {
        try {
            obj.wait();
        } catch (InterruptedException e) {
        }
    }

    public static void waitObject(Object obj, long millis) {
        try {
            obj.wait(millis);
        } catch (InterruptedException e) {
        }
    }

    public static void syncWait(Object obj) {
        synchronized (obj) {
            try {
                obj.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public static void syncWait(Object obj, long millis) {
        synchronized (obj) {
            try {
                obj.wait(millis);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
        }
    }

    public static void sleepMilliseconds(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 获取格式化线程名的 ThreadFactory
     * @param namePrefix 线程名前缀，最终格式：namePrefix-number
     * @return
     */
    public static ThreadFactory getNameFormatThreadFactory(final String namePrefix) {
        return new ThreadFactory() {
            final AtomicLong count = (namePrefix != null) ? new AtomicLong(0) : null;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                if (null != namePrefix) {
                    thread.setName(String.format("%s-%d", namePrefix, count.getAndIncrement()));
                }
                return thread;
            }
        };
    }

    public static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }
}
