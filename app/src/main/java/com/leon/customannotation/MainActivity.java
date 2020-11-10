package com.leon.customannotation;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
import com.leon.customannotation.second.SecondActivity;

public class MainActivity extends BaseActivity {

    @BindView(viewId = R.id.name)
    TextView mName;

    @BindView(viewId = R.id.school)
    TextView mSchool;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mName != null) {
            mName.setText(mName.getText().toString() + "张三");
        }

        if (mSchool != null) {
            mSchool.setText(mSchool.getText().toString() + "学校");
        }
    }

    @ViewOnClick(viewId = {R.id.name, R.id.school})
    public void onClick(int viewId) {
        if (viewId == R.id.name) {
            Toast.makeText(this, mName.getText().toString(), Toast.LENGTH_SHORT).show();
        }
        if (viewId == R.id.school) {
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
        }
    }
}