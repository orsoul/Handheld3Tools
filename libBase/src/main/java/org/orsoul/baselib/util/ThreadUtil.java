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

public final class ThreadUtil {
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

  public static ExecutorService getNormalThreadPool() {
    createNormalThreadPool();
    return normalThreadPool;
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
  public static boolean sleep(long milliseconds) {
    try {
      //Thread.sleep(milliseconds);
      TimeUnit.MILLISECONDS.sleep(milliseconds);
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }

  public static boolean sleepSeconds(int seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
      return true;
    } catch (InterruptedException e) {
      return false;
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
     * 当前 运行任务的 线程，任务运行时赋值，任务结束时置为null.
     */
    private Thread runningThread;

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
        runningThread = Thread.currentThread();
        setRunning(true);
        stopped = false;
        onTaskBefore();
        ThreadRunnable.this.run();
        onTaskFinish();
        setRunning(false);
        runningThread = null;
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

    public synchronized void interrupt() {
      if (runningThread != null) {
        runningThread.interrupt();
      }
    }

    public synchronized Thread getRunningThread() {
      return runningThread;
    }

    private synchronized void setRunningThread(Thread runningThread) {
      this.runningThread = runningThread;
    }

    /** run()开始前执行， */
    protected void onTaskBefore() {
    }

    /** run()结束后执行. */
    protected void onTaskFinish() {
    }
  }

  public static abstract class TimeThreadRunnable extends ThreadRunnable {
    /** 开始运行的时间，单位毫秒. */
    private long startTime;
    /** 运行时间，单位毫秒. */
    private long runTime = 5000L;
    /** 运执行次数. */
    private int total = 1024;

    public long getRunTime() {
      return runTime;
    }

    public void setRunTime(long runTime) {
      this.runTime = runTime;
    }

    public int getTotal() {
      return total;
    }

    public void setTotal(int total) {
      this.total = total;
    }

    /** 设置任务开始运行的时间为当前时间. */
    public void resetStartTime() {
      startTime = System.currentTimeMillis();
    }

    @Override public void run() {
      resetStartTime();
      int count = 0;
      while (true) {
        if (isStopped()) {
          onStop();
          return;
        }

        boolean finish = handleOnce();
        count++;
        long gonging = System.currentTimeMillis() - startTime;
        onHandleOnce(gonging, total);

        if (finish) {
          onHandleFinish();
          break;
        }
        if (runTime <= gonging) {
          onTimeout(gonging, count);
          break;
        }
        if (total <= count) {
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

    protected void onHandleOnce(long goingTime, int total) {
    }

    /** 主动停止线程 回调. */
    protected void onStop() {
    }

    /** 业务处理结束（即handleOnce()返回true） 回调. */
    protected void onHandleFinish() {
    }

    /** 任务执行达到终止条件时 回调. */
    protected void onTimeout(long runTime, int total) {
    }
  }
}
