# ClassLoader_Demo
@[TOC]

<br>
<br>
<br>
<br>

# 一、加载 Android 组件的类加载器

---

<br>


打印 <font color=blue>Activity 组件类 </font>的 <font color=red>类加载器</font> 及 该 <font color=purple>类加载器 的 <font color=orange>父类类加载器 : 

```cpp
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
```





**执行结果 :** 
![在这里插入图片描述](https://img-blog.csdnimg.cn/36e633bbe3ff4e5abe19ccd9af9b7bfb.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

**日志打印结果 :** 

```cpp
2021-12-07 19:03:19.295 15050-15050/com.example.classloader_demo 
I/MainActivity: MainActivity ClassLoader : 
dalvik.system.PathClassLoader
[DexPathList[
[zip file "/data/app/com.example.classloader_demo-
wBls1CbThiHbSEKLGKgS7w==/base.apk"],
nativeLibraryDirectories=[/data/app/com.example.classloader_demo-
wBls1CbThiHbSEKLGKgS7w==/lib/arm64, /system/lib64]]]

2021-12-07 19:03:19.295 15050-15050/com.example.classloader_demo 
I/MainActivity: MainActivity Parent ClassLoader : 
java.lang.BootClassLoader@3d6cf66
```

完整日志 : 

```cpp
2021-12-07 19:03:19.295 15050-15050/com.example.classloader_demo I/MainActivity: MainActivity ClassLoader : dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.example.classloader_demo-wBls1CbThiHbSEKLGKgS7w==/base.apk"],nativeLibraryDirectories=[/data/app/com.example.classloader_demo-wBls1CbThiHbSEKLGKgS7w==/lib/arm64, /system/lib64]]]
2021-12-07 19:03:19.295 15050-15050/com.example.classloader_demo I/MainActivity: MainActivity Parent ClassLoader : java.lang.BootClassLoader@3d6cf66
```

<br>
<br>
<br>
<br>

# 二、加载 Android 组件的类的双亲委派机制实例

---

<br>

**Activity 类加载分析 :** 

Activity 组件类的类加载器是 `dalvik.system.PathClassLoader` , 

`dalvik.system.PathClassLoader` 类加载器的 父类节点是 `java.lang.BootClassLoader` 类加载器 ,

 `java.lang.BootClassLoader` 是根节点的 类加载器 ; 

<br>

**双亲委派机制 :** 

在双亲委派机制中 , <font color=blue>类加载任务</font> , 先被分配给 `PathClassLoader` , `PathClassLoader` 不处理 , <font color=green>将任务委派给父类节点 `BootClassLoader `; 

`BootClassLoader` 是根节点的类加载器 , 其尝试加载 `Activity` 类 , 发现加载不了 , 没有这个能力 , 然后 `BootClassLoader` 将任务 <font color=blue>委派给子节点 `PathClassLoader` </font>, `PathClassLoader` 尝试加载 Activity 类 ,<font color=purple> 加载成功 , <font color=orange>返回该类 ; 
