package io.agora.openlive.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.openlive.R;

public class AccountActivity extends BaseActivity {


    private Button Login;
    private EditText email, password;
    private TextView register;
    private String emailcek, passwordcek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        register = (TextView) findViewById(R.id.register);
        register.setPaintFlags(register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Login = (Button) findViewById(R.id.login);

        email = (EditText)findViewById(R.id.accountEmail);
        password = (EditText) findViewById(R.id.password);

    }

    public void Login(View view){
        emailcek = email.getText().toString();
        passwordcek = password.getText().toString();

        if(emailcek.isEmpty()){
            if(passwordcek.isEmpty()){
                Toast.makeText(getApplicationContext(),"Logging in..",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Your password is empty!",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Your email is empty!",Toast.LENGTH_SHORT).show();
        }
    }

    public void toRegister(View view){
        Intent intent = new Intent(AccountActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    public void onBackArrowPressed(View view){
        Intent intent = new Intent(AccountActivity.this,SettingsActivity.class);
        startActivity(intent);
    }
}
