package com.example.renjialu.test;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexPopViewUtils1 {

    static {
        mInstance = new MutexPopViewUtils1();
        mLock = new ReentrantLock();
    }
    private static boolean isMutexRunning = false;

    private static MutexPopViewUtils1 mInstance;

    private static Lock mLock;

    private Queue<Object> mQueue = new PriorityBlockingQueue<Object>(/*8, new Comparator<BasePopTask>() {
        @Override
        public int compare(BasePopTask o1, BasePopTask o2) {
            return o1.mPriority - o2.mPriority;
        }
    }*/);

    public static synchronized boolean addPopViewTask(@NonNull Object mTask) {
        boolean result;
        result = mInstance.mQueue.add(mTask);
        if (!isMutexRunning && !mInstance.mQueue.isEmpty()){
            mInstance.start();
        }
        return result;
    }

    private boolean closeBool = true;

    private boolean isShowing = false;

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("franer", "started runing!");
                isMutexRunning = true;
                while (closeBool) {
                    mLock.lock();
                    Object object = mQueue.poll();
                    MutexPopViewUtils1.BasePopTask task = null;
                    if (object == null){
                        mLock.unlock();
                        continue;
                    } else if (!(object instanceof MutexPopViewUtils1.BasePopTask)){
                        mQueue.remove(object);
                        mLock.unlock();
                        continue;
                    } else {
                        task = (MutexPopViewUtils1.BasePopTask) object;
                    }
                    if (task.mutexFinalCheck()){
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

    public static void doNextTask(){

        try{
            if (!isMutexRunning || mInstance.mQueue.isEmpty()){
                return;
            }

        } catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

        mInstance.mLock.unlock();

    }

    public interface BasePopTask {
//        int mPriority = 0;
//        int mDuring = -1;
//        boolean isCompulsory = false;
//        boolean needJoinQueue = false;

        void mutexShow(Object... objs);

        void mutexDismiss();

        boolean mutexFinalCheck();

    }
}

class MyToast extends Toast implements MutexPopViewUtils1.BasePopTask{

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public MyToast(Context context) {
        super(context);
    }

    @Override
    public void mutexShow(Object... objs) {
        this.show();
    }

    @Override
    public void mutexDismiss() {
        this.cancel();
        MutexPopViewUtils1.doNextTask();
    }

    @Override
    public boolean mutexFinalCheck() {
        return true;
    }

}