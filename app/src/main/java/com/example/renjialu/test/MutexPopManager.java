package com.example.renjialu.test;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;

public class MutexPopManager {
    /** 互斥线程是否运行状态 */
    private static boolean isMutexRunning = false;
    /** 静态实例对象 */
    private static MutexPopManager mInstance;
    /** 同步锁 */
    private static Lock mLock;
    /** 用来存放Task的队列 */
    private Queue<BasePopTask> mQueue;
    /** 当前正在执行的Task */
    private BasePopTask mCurrentRunningTask;
    /** 内循环的布尔值 */
    private boolean mCloseLoopBool;

    private boolean isShowing = false;

    private static synchronized void ensureInstance() {
        // 如果实例未初始化
        if (mInstance == null) {
            mInstance = new MutexPopManager();
        }
    }

    private static synchronized void ensureQueue() {
        ensureInstance();
        // 如果队列未初始化
        if (mInstance.mQueue == null) {
            mInstance.mQueue = new PriorityBlockingQueue<BasePopTask>(4);
        }
    }

    /**
     * @return true执行成功或已加入队列，否则返回false
     * @description: 执行PopTask的调用方法
     * @author renjialu
     */
    public static synchronized boolean exePopTask(@NonNull BasePopTask mTask) {
        boolean result;
        // 确保实例对象不为空
        ensureInstance();
        // 可以直接显示或满足抢占当前正在显示的展示条件
        if (mInstance.mCurrentRunningTask == null || mTask.isEnforce && mTask.compareTo(mInstance.mCurrentRunningTask) < 0) {
            if (mInstance.mCurrentRunningTask != null) {
                mInstance.mCurrentRunningTask.mutexDismiss();
            }
            // 进行一波显示
            mInstance.mCurrentRunningTask = mTask;
            if (mTask.mutexFinalCheck()) {
                mTask.mutexShow();
            }
            result = true;
        } else {
            // 不可直接显示或抢占条件未满足的
            if (mTask.isEnqueue) {
                // 需要添加队列的
                ensureQueue();
                result = mInstance.mQueue.add(mTask);
                mInstance.start();
            } else {
                result = false;
            }
        }

        return result;
    }

    public void start() {
        // 如果互斥机制正在运行
        if (isMutexRunning) {
            return;
        }
        mCloseLoopBool = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("franer", "started runing!");
                isMutexRunning = true;
                while (mCloseLoopBool) {
                    mLock.lock();
                    Object object = mQueue.poll();
                    MutexPopManager.BasePopTask task = null;
                    if (object == null) {
                        mLock.unlock();
                        continue;
                    } else if (!(object instanceof MutexPopManager.BasePopTask)) {
                        mQueue.remove(object);
                        mLock.unlock();
                        continue;
                    } else {
                        task = (MutexPopManager.BasePopTask) object;
                    }
                    if (task.mutexFinalCheck()) {
                        task.mutexShow();
                        isShowing = true;

                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        mQueue.remove(task);
                        mLock.unlock();
                        continue;
                    }
                }
                isMutexRunning = false;
            }
        }).start();
    }

    public static synchronized void finish() {
        if (mInstance == null){
            return;
        }
        mInstance.mCloseLoopBool = false;
        if (!mInstance.mQueue.isEmpty()){
            mInstance.mQueue.clear();
        }
        if (mInstance.mCurrentRunningTask != null){
            mInstance.mCurrentRunningTask.mutexDismiss();
        }
    }

    public static void doNextTask() {

        try {
            if (!isMutexRunning || mInstance.mQueue.isEmpty()) {
                return;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        mInstance.mLock.unlock();

    }

    public class BasePopTask implements PopItem, Comparable<BasePopTask> {

        public static final int MAX_PRIORITY = 0;

        public static final int MIN_PRIORITY = 10;

        /** 弹框Item实际内容 */
        private PopItem mItem;
        /** 弹框优先级 */
        private int mPriority;
        /** 是否入队列 */
        private boolean isEnqueue;
        /** 是否强制 */
        private boolean isEnforce;

        public int getPriority() {
            return mPriority;
        }

        /**
         * @Description: set优先级方法需要在规定范围内
         */
        public void setPriority(int priority) {
            // 确保priority 的大小返回在规定范围内
            priority = priority > MIN_PRIORITY ? MIN_PRIORITY : priority;
            priority = priority < MAX_PRIORITY ? MAX_PRIORITY : priority;
            this.mPriority = priority;
        }

        public PopItem getItem() {
            return mItem;
        }

        public void setItem(PopItem mItem) {
            this.mItem = mItem;
        }

        public boolean isEnqueue() {
            return isEnqueue;
        }

        public void setEnqueue(boolean enqueue) {
            isEnqueue = enqueue;
        }

        public boolean isEnforce() {
            return isEnforce;
        }

        public void setEnforce(boolean enforce) {
            isEnforce = enforce;
        }

        public BasePopTask(PopItem item, int priority, boolean isenqueue, boolean isforce) {
            this.mItem = item;
            this.isEnqueue = isenqueue;
            this.isEnforce = isforce;
            this.setPriority(priority);
        }

        public BasePopTask(PopItem item) {
            this(item, MIN_PRIORITY, false, false);
        }

        @Override
        public void mutexShow(Object... objs) {
            if (mItem != null) {
                mItem.mutexShow(objs);
            }
        }

        @Override
        public void mutexDismiss() {
            if (mItem != null) {
                mItem.mutexDismiss();
            }
        }

        @Override
        public boolean mutexFinalCheck() {
            if (mItem != null) {
                return mItem.mutexFinalCheck();
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(@NonNull BasePopTask o) {
            return o.mPriority - this.mPriority;
        }
    }

    interface PopItem {

        void mutexShow(Object... objs);

        void mutexDismiss();

        boolean mutexFinalCheck();

    }
}

//class MyToast extends Toast implements MutexPopManager.BasePopTask {
//
//    /**
//     * Construct an empty Toast object.  You must call {@link #setView} before you
//     * can call {@link #show}.
//     *
//     * @param context The context to use.  Usually your {@link Application}
//     *                or {@link Activity} object.
//     */
//    public MyToast(Context context) {
//        super(context);
//    }
//
//    @Override
//    public void mutexShow(Object... objs) {
//        this.show();
//    }
//
//    @Override
//    public void mutexDismiss() {
//        this.cancel();
//        MutexPopManager.doNextTask();
//    }
//
//    @Override
//    public boolean mutexFinalCheck() {
//        return true;
//    }
//
//}