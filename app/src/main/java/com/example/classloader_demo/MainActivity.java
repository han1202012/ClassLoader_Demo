package com.example.classloader_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取当前 Activity 的 类加载器 ClassLoader
        ClassLoader classLoader = MainActivity.class.getClassLoader();

        // 打印当前 Activity 的 ClassLoader 类加载器
        Log.i(TAG, "MainActivity ClassLoader : " + classLoader);

        // 获取 类加载器 父类
        ClassLoader parentClassLoader = classLoader.getParent();

        // 打印当前 Activity 的 ClassLoader 类加载器 的父类
        Log.i(TAG, "MainActivity Parent ClassLoader : " + parentClassLoader);
    }
}