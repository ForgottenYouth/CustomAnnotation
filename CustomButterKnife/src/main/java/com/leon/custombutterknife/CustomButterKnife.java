/*
 * Author: shiwenliang
 * Date: 2020/11/4 14:23
 * Description:
 */
package com.leon.custombutterknife;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomButterKnife {

    public static void bind(Activity activity) {
        String activityName = activity.getClass().getName();
        String genrateClass = activityName + "_customButterKnife";
        try {
            //调用构造器来实现bind
            Object inject = Class.forName(genrateClass).getConstructor().newInstance();

            /*
             * TODO 调用生成的java类进行onclick事件的注解绑定
             */
            if (null != inject) {
                Method bindViewByIdMethond = inject.getClass().getDeclaredMethod("BindViewById", activity.getClass());
                bindViewByIdMethond.invoke(inject, activity);
            }

            /*
             * TODO 调用生成的java类进行onclick事件的注解绑定
             */
            if (null != inject) {
                Method bindViewOnClickMethod = inject.getClass().getDeclaredMethod("BindViewOnClick", activity.getClass());
                bindViewOnClickMethod.invoke(inject, activity);
            }
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}