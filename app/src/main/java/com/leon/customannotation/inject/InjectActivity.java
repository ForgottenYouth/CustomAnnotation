/*
 * Author: shiwenliang
 * Date: 2020/11/11 7:36
 * Description: 使用反射来进行解析注解，并完成相关的工作
 */
package com.leon.customannotation.inject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
import com.leon.cusinject.InjectUtils;
import com.leon.customannotation.R;

public class InjectActivity extends AppCompatActivity {

    @BindView(viewId = R.id.inject_btn)
    Button mInjectBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject);
        InjectUtils.injectAnnotation(this);
    }


    @ViewOnClick(viewId = R.id.inject_btn)
    public void onClick(@IdRes int viewId) {
        if (viewId == R.id.inject_btn) {
            Toast.makeText(this, mInjectBtn.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}