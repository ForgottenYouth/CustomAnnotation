/*
 * Author: shiwenliang
 * Date: 2020/11/10 23:16
 * Description: 抽象基类
 */
package com.leon.customannotation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.leon.custombutterknife.CustomButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        CustomButterKnife.bind(this);
    }

    protected abstract int getLayoutId();
}