/*
 * Author: shiwenliang
 * Date: 2020/11/10 23:10
 * Description: 测试多个Activity使用自定义注解
 */
package com.leon.customannotation.second;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
import com.leon.customannotation.BaseActivity;
import com.leon.customannotation.R;
import com.leon.customannotation.inject.InjectActivity;

public class SecondActivity extends BaseActivity {

    @BindView(viewId = R.id.second_btn)
    Button btn;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_second;
    }


    @ViewOnClick(viewId = R.id.second_btn)
    public void onClick(int viewId) {
        if (viewId == R.id.second_btn) {
            Toast.makeText(this, "您点了第一个页面的按钮", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, InjectActivity.class);
            startActivity(intent);
        }
    }
}