package com.example.renjialu.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn1:
                final Toast toast1 = Toast.makeText(this, "this Toast‘s priority = 1", Toast.LENGTH_SHORT);
                MutexPopViewUtils.addPopViewTask(MutexPopViewUtils.generateTask(1).setRun(new Runnable() {
                    @Override
                    public void run() {
                        toast1.show();
                    }
                },2000));
                break;
            case R.id.btn2:
                final Toast toast2 = Toast.makeText(this, "this Toast‘s priority = 2", Toast.LENGTH_SHORT);
                MutexPopViewUtils.addPopViewTask(MutexPopViewUtils.generateTask(2).setRun(new Runnable() {
                    @Override
                    public void run() {
                        toast2.show();
                    }
                }, 2000));
                break;
            case R.id.btn3:


                Runnable run = new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(MainActivity.this, "this Toast‘s priority = 3", Toast.LENGTH_SHORT).show();




                    }
                };

                MutexPopViewUtils.PopViewTask task = MutexPopViewUtils.generateTask(3).setRun(run, 2000);

                MutexPopViewUtils.addPopViewTask(task);


                break;
            case R.id.btn4:
                Toast.makeText(this, "Toast4", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn5:
                Toast.makeText(this, "Toast5", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
