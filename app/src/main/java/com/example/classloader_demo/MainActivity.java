package com.example.classloader_demo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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
        //startDexActivityWithoutClassLoader(this, mDexPath);

        // 替换 LoadedApk 中的 类加载器 ClassLoader
        // 然后使用替换的类加载器加载 DEX 字节码文件中的 Activity 组件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //startDexActivityWithReplacedClassLoader(this, mDexPath);
        }

        // 在类加载器的双亲委派机制中的 PathClassLoader 和 BootClassLoader 之间
        // 插入 DexClassLoader
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startDexActivityWithInsertedClassLoader(this, mDexPath);
        }
    }

    /**
     * 打印当前的类加载器及父节点
     */
    private void classloaderLog() {
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
            while ((readLen = inputStream.read(buffer)) != -1) {
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
            while ((readLen = inputStream.read(buffer)) != -1) {
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
     *
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
     * 不修改类加载器的前提下 , 运行 Dex 字节码文件中的组件
     *
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


    /**
     * 替换 LoadedApk 中的 类加载器 ClassLoader
     *
     * @param context
     * @param dexFilePath
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startDexActivityWithReplacedClassLoader(Context context, String dexFilePath) {
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

        //------------------------------------------------------------------------------------------
        // 下面开始替换 LoadedApk 中的 ClassLoader


        // I. 获取 ActivityThread 实例对象


        // 获取 ActivityThread 字节码类 , 这里可以使用自定义的类加载器加载
        // 原因是 基于 双亲委派机制 , 自定义的 DexClassLoader 无法加载 , 但是其父类可以加载
        // 即使父类不可加载 , 父类的父类也可以加载
        Class<?> ActivityThreadClass = null;
        try {
            ActivityThreadClass = dexClassLoader.loadClass(
                    "android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 获取 ActivityThread 中的 sCurrentActivityThread 成员
        // 获取的字段如下 :
        // private static volatile ActivityThread sCurrentActivityThread;
        // 获取字段的方法如下 :
        // public static ActivityThread currentActivityThread() {return sCurrentActivityThread;}
        Method currentActivityThreadMethod = null;
        try {
            currentActivityThreadMethod = ActivityThreadClass.getDeclaredMethod(
                    "currentActivityThread");

            // 设置可访问性 , 所有的 方法 , 字段 反射 , 都要设置可访问性
            currentActivityThreadMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 执行 ActivityThread 的 currentActivityThread() 方法 , 传入参数 null
        Object activityThreadObject = null;
        try {
            activityThreadObject = currentActivityThreadMethod.invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        // II. 获取 LoadedApk 实例对象


        // 获取 ActivityThread 实例对象的 mPackages 成员
        // final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
        Field mPackagesField = null;
        try {
            mPackagesField = ActivityThreadClass.getDeclaredField("mPackages");

            // 设置可访问性 , 所有的 方法 , 字段 反射 , 都要设置可访问性
            mPackagesField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // 从 ActivityThread 实例对象 activityThreadObject 中
        // 获取 mPackages 成员
        ArrayMap mPackagesObject = null;
        try {
            mPackagesObject = (ArrayMap) mPackagesField.get(activityThreadObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 获取 WeakReference<LoadedApk> 弱引用对象
        WeakReference weakReference = (WeakReference) mPackagesObject.get(this.getPackageName());
        // 获取 LoadedApk 实例对象
        Object loadedApkObject = weakReference.get();


        // III. 替换 LoadedApk 实例对象中的 mClassLoader 类加载器


        // 加载 android.app.LoadedApk 类
        Class LoadedApkClass = null;
        try {
            LoadedApkClass = dexClassLoader.loadClass("android.app.LoadedApk");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 通过反射获取 private ClassLoader mClassLoader; 类加载器对象
        Field mClassLoaderField = null;
        try {
            mClassLoaderField = LoadedApkClass.getDeclaredField("mClassLoader");

            // 设置可访问性
            mClassLoaderField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // 替换 mClassLoader 成员
        try {
            mClassLoaderField.set(loadedApkObject, dexClassLoader);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        //------------------------------------------------------------------------------------------

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

    /**
     * 在类加载器的父类子类节点中 , 插入自定义 DexClassLoader
     * 基于双亲 委派机制的解决方案
     *
     * @param context
     * @param dexFilePath
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startDexActivityWithInsertedClassLoader(Context context, String dexFilePath) {
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");

        //------------------------------------------------------------------------------------------
        // 下面开始 在 ClassLoader 的双亲委派体系中 , 插入自定义的 DexClassLoader


        // I. 创建 DexClassLoader , 并设置其 父类节点为 BootClassLoader


        // 获取 PathClassLoader
        ClassLoader pathClassloader = MainActivity.class.getClassLoader();
        // 获取 BootClassLoader
        ClassLoader bootClassloader = MainActivity.class.getClassLoader().getParent();

        /*
            注意原来的逻辑是 PathClassLoader 用于加载组件类 , 其父节点是 BootClassLoader
            现在将 PathClassLoader 父节点设置为 DexClassLoader ,
            DexClassLoader 父节点设置为 BootClassLoader
            相当于在 PathClassLoader 和 BootClassLoader 之间插入了一个 DexClassLoader
         */

        // 初始化 DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexFilePath,                    // Dex 字节码文件路径
                optFile.getAbsolutePath(),      // 优化目录
                libFile.getAbsolutePath(),      // 依赖库目录
                bootClassloader                 // 父节点类加载器
        );


        // II. 使用 DexClassLoader 实例对象作为 PathClassLoader 的父节点


        // 获取 ClassLoader 的 private final ClassLoader parent; 成员
        Field parentField = null;
        try {
            parentField = ClassLoader.class.getDeclaredField("parent");

            // 设置可访问性
            parentField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // 设置 PathClassLoader 的 parent 字段为 自定义的 DexClassLoader 实例对象
        try {
            parentField.set(pathClassloader, dexClassLoader);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        //------------------------------------------------------------------------------------------

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