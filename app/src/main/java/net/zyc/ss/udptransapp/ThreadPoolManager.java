package net.zyc.ss.udptransapp;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static final String TAG = ThreadPoolManager.class.getName();
    private static ThreadPoolManager instance;
    private final int shortThreadCoreNum = 20;
    private int shortThreadMaxNum;
    private int longThreadCoreNum;
    private int longThreadMaxNum;
    private RejectedExecutionHandler mExecutionHandler;

    private ThreadPoolManager() {
        shortThreadMaxNum = shortThreadCoreNum * 2;
        longThreadCoreNum = Runtime.getRuntime().availableProcessors() * 2 + 1;
        longThreadMaxNum = longThreadCoreNum * 2;
        mExecutionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                executor.execute(r);
            }
        };
    }

    public static ThreadPoolManager getInstance() {
        if (null == instance) {
            synchronized (ThreadPoolManager.class) {
                if (null == instance) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    private ThreadPoolExecutor longExecutor;
    private ThreadPoolExecutor shortExecutor;

    public void executeShortTask(Runnable runnable) {
        if (null == shortExecutor) {
            shortExecutor = new ThreadPoolExecutor(shortThreadCoreNum, shortThreadMaxNum, 1,
                    TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(1024),
                    new DefaultThreadFactory(), mExecutionHandler);
        }
        shortExecutor.execute(runnable);
    }

    public void executeLongTask(Runnable runnable) {
        if (null == longExecutor) {
            longExecutor = new ThreadPoolExecutor(longThreadCoreNum, longThreadMaxNum, 1,
                    TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>(1024),
                    new DefaultThreadFactory(), mExecutionHandler);
        }
        longExecutor.execute(runnable);
    }

    public void cancelLongTask(Runnable runnable) {
        if (longExecutor != null && !longExecutor.isShutdown() && !longExecutor.isTerminated()) {
            longExecutor.remove(runnable);
        }
    }

    public static class DefaultThreadFactory implements ThreadFactory {
        int threadNum = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            final Thread result = new Thread(runnable, "gzx-pool-thread-" + threadNum) {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    super.run();
                }
            };
            threadNum++;
            return result;
        }
    }
}
