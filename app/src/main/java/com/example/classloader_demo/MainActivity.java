package com.example.classloader_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * Dex 文件路径
     */
    private String mDexPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 打印类加载器及父节点
        classloaderLog();

        // 拷贝 dex 文件
        mDexPath = copyFile2();
        // 测试 DEX 文件中的方法
        testDex(this, mDexPath);

        // 拷贝 dex2 文件
        //mDexPath = copyFile2();
        // 启动 DEX 中的 Activity 组件 , 此处启动会失败
        startDexActivityWithoutClassLoader(this, mDexPath);
    }

    /**
     * 打印当前的类加载器及父节点
     */
    private void classloaderLog(){
        // 获取当前 Activity 的 类加载器 ClassLoader
        ClassLoader classLoader = MainActivity.class.getClassLoader();

        // 打印当前 Activity 的 ClassLoader 类加载器
        Log.i(TAG, "MainActivity ClassLoader : " + classLoader);

        // 获取 类加载器 父类
        ClassLoader parentClassLoader = classLoader.getParent();

        // 打印当前 Activity 的 ClassLoader 类加载器 的父类
        Log.i(TAG, "MainActivity Parent ClassLoader : " + parentClassLoader);
    }

    /**
     * 将 app\src\main\assets\classes.dex 文件 ,
     * 拷贝到 /data/user/0/com.example.classloader_demo/files/classes.dex 位置
     */
    private String copyFile() {
        // DEX 文件
        File dexFile = new File(getFilesDir(), "classes.dex");
        // DEX 文件路径
        String dexPath = dexFile.getAbsolutePath();

        Log.i(TAG, "开始拷贝文件 dexPath : " + dexPath);

        // 如果之前已经加载过 , 则退出
        if (dexFile.exists()) {
            Log.i(TAG, "文件已经拷贝 , 退出");
            return dexPath;
        }

        try {
            InputStream inputStream = getAssets().open("classes.dex");
            FileOutputStream fileOutputStream = new FileOutputStream(dexPath);

            byte[] buffer = new byte[1024 * 4];
            int readLen = 0;
            while ( (readLen = inputStream.read(buffer)) != -1 ) {
                fileOutputStream.write(buffer, 0, readLen);
            }

            inputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.i("HSL", "classes.dex 文件拷贝完毕");
        }
        return dexPath;
    }

    /**
     * 将 app\src\main\assets\classes2.dex 文件 ,
     * 拷贝到 /data/user/0/com.example.classloader_demo/files/classes2.dex 位置
     */
    private String copyFile2() {
        // DEX 文件
        File dexFile = new File(getFilesDir(), "classes2.dex");
        // DEX 文件路径
        String dexPath = dexFile.getAbsolutePath();

        Log.i(TAG, "开始拷贝文件 dexPath : " + dexPath);

        // 如果之前已经加载过 , 则退出
        if (dexFile.exists()) {
            Log.i(TAG, "文件已经拷贝 , 退出");
            return dexPath;
        }

        try {
            InputStream inputStream = getAssets().open("classes2.dex");
            FileOutputStream fileOutputStream = new FileOutputStream(dexPath);

            byte[] buffer = new byte[1024 * 4];
            int readLen = 0;
            while ( (readLen = inputStream.read(buffer)) != -1 ) {
                fileOutputStream.write(buffer, 0, readLen);
            }

            inputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.i("HSL", "classes2.dex 文件拷贝完毕");
        }
        return dexPath;
    }

    /**
     * 测试调用 Dex 字节码文件中的方法
     * @param context
     * @param dexFilePath
     */
    private void testDex(Context context, String dexFilePath) {
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");

        // 初始化 DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexFilePath,                    // Dex 字节码文件路径
                optFile.getAbsolutePath(),      // 优化目录
                libFile.getAbsolutePath(),      // 依赖库目录
                context.getClassLoader()        // 父节点类加载器
        );

        // 加载 com.example.dex_demo.DexTest 类
        // 该类中有可执行方法 test()
        Class<?> clazz = null;
        try {
            clazz = dexClassLoader.loadClass("com.example.dex_demo.DexTest");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 获取 com.example.dex_demo.DexTest 类 中的 test() 方法
        if (clazz != null) {
            try {
                // 获取 test 方法
                Method method = clazz.getDeclaredMethod("test");
                // 获取 Object 对象
                Object object = clazz.newInstance();
                // 调用 test() 方法
                method.invoke(object);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 不修改类加载器的前提下
     * @param context
     * @param dexFilePath
     */
    private void startDexActivityWithoutClassLoader(Context context, String dexFilePath) {
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");

        // 初始化 DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexFilePath,                    // Dex 字节码文件路径
                optFile.getAbsolutePath(),      // 优化目录
                libFile.getAbsolutePath(),      // 依赖库目录
                context.getClassLoader()        // 父节点类加载器
        );

        // 加载 com.example.dex_demo.DexTest 类
        // 该类中有可执行方法 test()
        Class<?> clazz = null;
        try {
            clazz = dexClassLoader.loadClass("com.example.dex_demo.MainActivity2");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 启动 com.example.dex_demo.MainActivity2 组件
        if (clazz != null) {
            context.startActivity(new Intent(context, clazz));
        }
    }
}