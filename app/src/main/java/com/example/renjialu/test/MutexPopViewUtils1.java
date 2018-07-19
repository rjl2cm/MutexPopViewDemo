package com.example.renjialu.test;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class MutexPopViewUtils1 {

    static {
        mInstance = new MutexPopViewUtils1();
    }
    private static boolean isMutexRunning = false;

    private static MutexPopViewUtils1 mInstance;

    private Queue<BasePopTask> mQueue = new PriorityBlockingQueue<BasePopTask>(8, new Comparator<BasePopTask>() {
        @Override
        public int compare(BasePopTask o1, BasePopTask o2) {
            return o1.mPriority - o2.mPriority;
        }
    });

    public static synchronized boolean addPopViewTask(@NonNull BasePopTask mTask) {
        boolean result;
        result = mInstance.mQueue.add(mTask);
        if (!isMutexRunning && !mInstance.mQueue.isEmpty()){
            mInstance.start();
        }
//        Log.i("franer", "add a Task which priority is "+ mTask.mPriority + (result?"。 success":"。 fail"));
//        Log.w("franer", "Queue‘s size is "+ mInstance.mQueue.size());
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
//                int checkNum = 0;
                while (closeBool) {
                    BasePopTask task = mQueue.poll();
                    if (task == null){
                        continue;
                    }
                    if (task.finalCheck()){
                        task.mutexShow();
                        isShowing = true;
                        if (task.mDuring > 0) {
                            SystemClock.sleep(task.mDuring);
                        }
                    } else {
                        mQueue.remove(task);
                        continue;
                    }
                }
                isMutexRunning = false;
            }
        }).start();
    }


    public interface BasePopTask {
        int mPriority = 0;

        int mDuring = -1;

        boolean isCompulsory = false;

        boolean needJoinQueue = false;

        void mutexShow(Object... objs);

        void mutexDismiss();

        boolean finalCheck();
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
    }

    @Override
    public boolean finalCheck() {
        return true;
    }
}