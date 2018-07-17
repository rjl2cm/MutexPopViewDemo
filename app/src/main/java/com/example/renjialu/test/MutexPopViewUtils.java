package com.example.renjialu.test;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Comparator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexPopViewUtils {

    static {
        mInstance = new MutexPopViewUtils();
    }
    private static boolean isTaskRunning = false;

    private static MutexPopViewUtils mInstance;

    private Queue<PopViewTask> mQueue = new PriorityBlockingQueue<PopViewTask>(8, new Comparator<PopViewTask>() {
        @Override
        public int compare(PopViewTask o1, PopViewTask o2) {
            return o1.mPriority - o2.mPriority;
        }
    });

    public static synchronized boolean addPopViewTask(@NonNull PopViewTask mTask) {
        boolean result;
        result = mInstance.mQueue.add(mTask);
        if (!isTaskRunning && !mInstance.mQueue.isEmpty()){
            mInstance.start();
        }
//        Log.i("franer", "add a Task which priority is "+ mTask.mPriority + (result?"。 success":"。 fail"));
//        Log.w("franer", "Queue‘s size is "+ mInstance.mQueue.size());
        return result;
    }

    private boolean closeBool = true;

    private boolean isShowing = false;
    public void start() {
        final Lock mlock = new ReentrantLock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("franer", "started runing!");
                isTaskRunning = true;
                while (closeBool) {
                    if (isShowing) {
                        SystemClock.sleep(100);
                        checkNun++
                    }
                    PopViewTask task = mQueue.poll();

                    if (task == null || task.mRunStart == null) {
                        if (task != null) {
                            mQueue.remove(task);
                        }
                        continue;
                    }

                    task.mRunStart.run();
                    isShowing = true;


                    Log.e("franer","run a task which priority is "+ task.mPriority);
                }
                isTaskRunning = false;
            }
        }).start();
    }

    public static PopViewTask generateTask() {
        return generateTask(5);
    }

    public static PopViewTask generateTask(int priority) {
        if (priority >= 10) {
            priority = 10;
        }

        if (priority <= 0) {
            priority = 0;
        }

        PopViewTask task = mInstance.new PopViewTask(priority);
        return task;
    }

    public class PopViewTask {

        private int mPriority;
        private Runnable mRunStart;
        private Runnable mRunFinish;

        private PopViewTask(int priority) {
            mPriority = priority;
        }

        public PopViewTask setRun(final Runnable runStart, final long runTimeMil) {
            this.mRunStart = runStart;
            return this;
        }
    }
}
