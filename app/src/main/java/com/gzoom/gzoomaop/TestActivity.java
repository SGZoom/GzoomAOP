package com.gzoom.gzoomaop;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);


        findViewById(R.id.btn_send_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            print();

            }
        });
    }


    private void print() {
        Caculater caculater = new Caculater();
        // 本来是9，实际上应该是27
        Toast.makeText(TestActivity.this,"结果是"+caculater.getResult(3,4),Toast.LENGTH_SHORT).show();
    }
}
