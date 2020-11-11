/*
 * Author: shiwenliang
 * Date: 2020/11/11 7:23
 * Description: 放射工具
 */
package com.leon.cusinject;

import android.app.Activity;
import android.view.View;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InjectUtils {

    /*
     * TODO 反射绑定view
     */
    public static void injectAnnotation(final Activity activity) {
        Class<? extends Activity> aClass = activity.getClass();

        /*
         * TODO 获取所有的自己的fields,不包括父类
         */
        Field[] declaredFields = aClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            //遍历所有的field
            Field field = declaredFields[i];
            if (field.isAnnotationPresent(BindView.class)) {
                //找到使用BindView注解的属性，并取出对应的注解
                BindView annotation = field.getAnnotation(BindView.class);
                if (annotation != null) {
                    //获取到注解的参数值
                    int viewId = annotation.viewId();
                    //找到Id对应的视图
                    View view = activity.findViewById(viewId);

                    //设置field是可以访问的，否则则不能操作
                    field.setAccessible(true);
                    try {
                        //将找到的视图赋值给field
                        field.set(activity, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /*
        * TODO 获取该类所有的方法，不包括父类
        */
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            final Method declaredMethod = declaredMethods[i];
            if (declaredMethod.isAnnotationPresent(ViewOnClick.class)) {
                ViewOnClick annotation = declaredMethod.getAnnotation(ViewOnClick.class);
                if (annotation != null) {
                    final int[] viewIds = annotation.viewId();
                    for (int i1 = 0; i1 < viewIds.length; i1++) {
                        final int finalI = i1;
                        activity.findViewById(viewIds[i1]).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //反射方法的调用使用invoke
                                    declaredMethod.invoke(activity, viewIds[finalI]);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }
            }
        }
    }
}