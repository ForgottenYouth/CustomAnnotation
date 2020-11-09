package com.leon.customannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
import com.leon.custombutterknife.CustomButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(viewId = R.id.name)
    TextView mName;

    @BindView(viewId = R.id.school)
    TextView mSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mName != null) {
            mName.setText("张三");
        }

        if (mSchool != null) {
            mSchool.setText("学校");
        }
    }

    @ViewOnClick(viewId=R.id.name)
    public void onClick(int viewId){

    }
}