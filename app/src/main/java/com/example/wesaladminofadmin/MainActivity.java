package com.example.wesaladminofadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.wesaladminofadmin.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ButtonHandeler buttonHandeler=new ButtonHandeler(this);
        //ButtonHandlerName is the variable name we create in xml
        activityMainBinding.setButoonHandlerName(buttonHandeler);


    }

    public class ButtonHandeler {
        Context context;

        ButtonHandeler(Context context) {
            this.context = context;
        }
        public void addNewCharityButtonHandelr(View view){
            Intent intent = new Intent(MainActivity.this, EnterNewCharity.class);
            startActivity(intent);
        }
        public void editCharityButtonHandelr(View view){
            Intent intent = new Intent(MainActivity.this, EditCharity.class);
            startActivity(intent);
        }
    }

}
