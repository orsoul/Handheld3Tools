package com.fanfull.libjava.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
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
          normalThreadPool = new ThreadPoolExecutor(8, 32,
              5000L, TimeUnit.MILLISECONDS,
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

  /**
   * 等待超时返回true，等待被中断或唤醒返回false.
   *
   * @param obj 以此对象为锁同步等待，
   */
  public static boolean waitTimeout(Object obj, long timeout) {
    if (obj == null) {
      return false;
    }
    long waitTime = 0;
    try {
      //LogUtils.v("waiting");
      synchronized (obj) {
        waitTime = System.currentTimeMillis();
        obj.wait(timeout);
      }
    } catch (InterruptedException e) {
      //LogUtils.v("wait Interrupt");
    }
    waitTime = System.currentTimeMillis() - waitTime;
    //LogUtils.v("waite time " + waitTime);
    return timeout <= waitTime;
  }

  public static void waitObject(Object obj) {
    try {
      obj.wait();
    } catch (InterruptedException e) {
    }
  }

  /** @return 被中断返回false */
  public static boolean waitObject(Object obj, long millis) {
    try {
      obj.wait(millis);
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }

  /** 与syncAwaken()配对使用，@return 被中断返回false */
  public static boolean syncWait(Object obj) {
    synchronized (obj) {
      try {
        obj.wait();
        return true;
      } catch (InterruptedException e) {
        return false;
      }
    }
  }

  /** 与syncAwaken()配对使用，@return 被中断返回false */
  public static boolean syncWait(Object obj, long millis) {
    synchronized (obj) {
      try {
        obj.wait(millis);
        return true;
      } catch (InterruptedException e) {
        return false;
      }
    }
  }

  /** 唤醒线程，syncWait()配对使用 */
  public static void syncAwaken(Object obj) {
    synchronized (obj) {
      try {
        obj.notifyAll();
      } catch (Exception e) {
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

    private boolean isRestart;

    protected Runnable onTaskFinishRunnable;

    /**
     * 停止当前任务，重新执行任务.
     *
     * @return 当前任务正在运行返回false；
     */
    public boolean restartThread() {
      if (isRunning()) {
        isRestart = true;
        stopThread(true);
        return false;
      }
      return startThread();
    }

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
        runningThread = Thread.currentThread();
        Thread.interrupted();
        setRunning(true);
        stopped = false;
        onTaskBefore();
        ThreadRunnable.this.run();
        onTaskFinish();
        setRunning(false);
        runningThread = null;

        if (isRestart) {
          isRestart = false;
          startThread();
        }
      });
      return true;
    }

    /** 设置标志位停止并中断当前线程，线程是否已经停止运行应用isRunning()判断. */
    public synchronized void stopThread() {
      stopThread(true);
    }

    /** 设置标志位停止当前线程，线程是否已经停止运行应用isRunning()判断. */
    public synchronized void stopThread(boolean interrupt) {
      stopped = true;
      if (interrupt) {
        interrupt();
      }
    }

    /**
     * 停止线程运行，线程停止后执行 onTaskFinishRunnable.run().
     *
     * @param onTaskFinishRunnable 线程停止后执行的动作.
     */
    public void stopThread(Runnable onTaskFinishRunnable) {
      this.onTaskFinishRunnable = onTaskFinishRunnable;
      stopThread(false);
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

    private CountDownLatch countDownLatch;

    /**
     * 当前线程正在运行，进入等待.
     *
     * @param millis 大于0等待时间，否则 无限等待.
     * @return 0:被唤醒，1：未进入等待，2：等待超时，3：等待被中断
     */
    public synchronized int await(long millis) {
      if (runningThread == null || !runningThread.getState().equals(Thread.State.RUNNABLE)) {
        // 当前线程 不处于 运行中，不进行等待
        return 1;
      }

      if (countDownLatch == null || countDownLatch.getCount() == 0) {
        countDownLatch = new CountDownLatch(1);
      }

      try {
        if (millis <= 0) {
          countDownLatch.await();
          return 0;
        } else if (countDownLatch.await(millis, TimeUnit.MILLISECONDS)) {
          return 0;
        } else {
          return 2;
        }
      } catch (InterruptedException e) {
        countDownLatch = null;
        return 3;
      }
    }

    /**
     * 唤醒等待的线程.
     */
    public synchronized void awaken() {
      if (countDownLatch != null) {
        countDownLatch.countDown();
        countDownLatch = null;
      }
    }

    /** run()开始前执行， */
    protected void onTaskBefore() {
    }

    /** 主动停止线程 回调. */
    protected void onStop() {
    }

    /** run()结束后执行. */
    protected void onTaskFinish() {
      if (onTaskFinishRunnable != null) {
        onTaskFinishRunnable.run();
        onTaskFinishRunnable = null;
      }
    }
  }

  /**
   * 携带停止标志的 任务，可查询执行任务的线程是否正在运行，可设置任务运行时间和执行次数.
   */
  public static abstract class TimeThreadRunnable extends ThreadRunnable {
    /** 开始运行的时间，单位毫秒. */
    private long startTime;
    /** 运行时间，单位毫秒，0或负数 表示不限时，默认值5秒. */
    private long runTime = 5000L;
    /** 执行的最大次数，0或负数 表示无限次. */
    private int total;

    /** handleOnce() 已执行次数. */
    protected int runCount;
    protected boolean isPause;
    protected long pauseTime;

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

    public synchronized boolean isPause() {
      return isPause;
    }

    /**
     * 线程 进入wait
     *
     * @param pauseTime 小于等于0 进入无限等待
     */
    public synchronized void pause(long pauseTime) {
      this.isPause = true;
      this.pauseTime = pauseTime;
    }

    public synchronized void resume() {
      this.isPause = false;
      ThreadUtil.syncAwaken(this);
    }

    /** 设置任务开始运行的时间为当前时间. */
    public void resetStartTime() {
      startTime = System.currentTimeMillis();
    }

    /** 线程离开 wait 后回调 */
    public void onResume() {}

    /** 线程进入 wait 前回调 */
    public void onPause() {}

    @Override public void run() {
      resetStartTime();
      runCount = 0;
      while (true) {
        if (isStopped()) {
          onStop();
          return;
        }

        if (isPause()) {
          onPause();
          boolean isNotify;
          if (0 < pauseTime) {
            isNotify = ThreadUtil.syncWait(this, pauseTime);
          } else {
            isNotify = ThreadUtil.syncWait(this);
          }
          onResume();
          if (!isNotify) {
            // 被中断
            continue;
          }
        }

        boolean finish = handleOnce();
        runCount++;
        long going = System.currentTimeMillis() - startTime;
        onHandleOnce(going, runCount);

        if (finish) {
          onHandleFinish();
          break;
        }
        if (0 < runTime && runTime <= going) {
          onTimeout(going, runCount);
          break;
        }
        if (0 < total && total <= runCount) {
          onTimeout(going, runCount);
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

    /**
     * @param goingTime 任务已运行时间
     * @param runCount 任务已执行次数
     */
    protected void onHandleOnce(long goingTime, int runCount) {
    }

    /** 业务处理结束（即handleOnce()返回true） 回调. */
    protected void onHandleFinish() {
    }

    /** 任务执行达到终止条件时 回调. */
    protected void onTimeout(long runTime, int total) {
    }
  }
}
