package com.matthew.passwordinputview;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.matthew.passwordinput.lib.PasswordInputView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PasswordInputView passwordView = findViewById(R.id.passwordView);
        passwordView.setInputListener(new PasswordInputView.InputListener() {
            @Override
            public void onInputCompleted(String text) {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

    }


}
