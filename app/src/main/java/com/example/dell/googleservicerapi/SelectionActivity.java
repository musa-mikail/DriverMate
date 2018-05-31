package com.example.dell.googleservicerapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class SelectionActivity extends AppCompatActivity {

    private Button driverLoginBtn;
    private Button customerLoginBtn;
    private Button managerLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.selection_activity);

        driverLoginBtn = (Button) findViewById(R.id.driverLoginBtn);
        customerLoginBtn = (Button) findViewById(R.id.customerLoginBtn);
        managerLoginBtn = (Button) findViewById(R.id.managerLoginBtn);
        driverLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectionActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });

    }
}
