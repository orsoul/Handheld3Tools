package org.orsoul.baselib.util;

import com.apkfuns.logutils.LogUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadUtil {
  private static ExecutorService normalThreadPool;
  private static ExecutorService singleThreadPool;

  private static void createNormalThreadPool() {
    if (normalThreadPool == null) {
      synchronized (ThreadUtil.class) {
        if (normalThreadPool == null) {
          normalThreadPool = new ThreadPoolExecutor(3, 32,
              3000L, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(), getNameFormatThreadFactory("FixedThread"));
        }
      }
    }
  }

  public static void execute(Runnable task) {
    createNormalThreadPool();
    normalThreadPool.execute(task);
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

  public static <T> Future<T> submit(Callable<T> task) {
    createNormalThreadPool();
    return normalThreadPool.submit(task);
  }

  /**
   * 线程休眠.
   *
   * @return 被中断返回false
   */
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
   *
   * @param namePrefix 线程名前缀，最终格式：namePrefix-number
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

  /**
   * 携带停止标志的 任务，可查询执行任务的线程是否正在运行.
   */
  public static abstract class ThreadRunnable implements Runnable {
    /** 线程运行状态. */
    private boolean isRunning;
    /** 线程停止标志. */
    protected boolean stopped;

    /**
     * 使用线程池 执行 当前任务.
     *
     * @return 当前任务正在运行返回false；
     */
    public synchronized boolean startThread() {
      if (isRunning()) {
        return false;
      }
      execute(() -> {
        LogUtils.i("%s run", ThreadRunnable.this.getClass().getSimpleName());
        setRunning(true);
        stopped = false;
        ThreadRunnable.this.run();
        onTaskFinish();
        setRunning(false);
        LogUtils.i("%s end", ThreadRunnable.this.getClass().getSimpleName());
      });
      return true;
    }

    /** 设置标志位停止当前线程，线程是否已经停止运行应用isRunning()判断. */
    public synchronized void stopThread() {
      stopped = true;
    }

    /** 获取 当前线程停止标志. */
    public synchronized boolean isStopped() {
      return stopped;
    }

    /** 当前线程正在运行 返回true. */
    public synchronized boolean isRunning() {
      return isRunning;
    }

    private synchronized void setRunning(boolean running) {
      isRunning = running;
    }

    protected void onTaskFinish() {
    }
  }

  public static abstract class TimeThreadRunnable extends ThreadRunnable {
    /** 运行时间，单位毫秒. */
    private long runTime = 5000L;
    /** 运执行次数. */
    private int runTimes = 1024;

    public long getRunTime() {
      return runTime;
    }

    public void setRunTime(long runTime) {
      this.runTime = runTime;
    }

    public int getRunTimes() {
      return runTimes;
    }

    public void setRunTimes(int runTimes) {
      this.runTimes = runTimes;
    }

    @Override public void run() {
      long start = System.currentTimeMillis();
      int count = 0;
      while (!isStopped()) {
        boolean finish = handleOnce();
        count++;
        if (finish) {
          onHandleFinish();
          break;
        }
        long gonging = System.currentTimeMillis() - start;
        if (runTime <= gonging) {
          onTimeout(gonging, count);
          break;
        }
        if (runTimes <= count) {
          onTimeout(gonging, count);
          break;
        }
      } // end while()
    } // end run

    /**
     * 执行一次任务.
     *
     * @return 执行完成 返回true，继续执行返回false.
     */
    protected abstract boolean handleOnce();

    protected void onHandleFinish() {
    }

    protected void onTimeout(long runTime, int runTimes) {
    }
  }
}
