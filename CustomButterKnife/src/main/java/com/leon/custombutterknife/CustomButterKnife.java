/*
 * Author: shiwenliang
 * Date: 2020/11/4 14:23
 * Description:
 */
package com.leon.custombutterknife;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;

public class CustomButterKnife {

    public static void bind(Activity activity) {
        String activityName = activity.getClass().getName();
        String genrateClass = activityName + "_ViewBinding";
        try {
            Class.forName(genrateClass).getConstructor(activity.getClass()).newInstance(activity);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}