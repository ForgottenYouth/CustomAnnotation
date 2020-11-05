package com.leon.customannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.leon.annotation.BindView;
import com.leon.custombutterknife.CustomButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(viewId = R.id.text2)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textView != null) {
            textView.setText("helloworld");
        }
    }
}