package io.agora.openlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.agora.openlive.R;

public class RegisterActivity extends RtcBaseActivity {

    private EditText fname, email, pass1,pass2;
    private String name, emails, passw1,passw2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fname = (EditText)findViewById(R.id.accountName);
        email = (EditText) findViewById(R.id.accountEmail);
        pass1 = (EditText)findViewById(R.id.password1);
        pass2 = (EditText)findViewById(R.id.password2);

        name = fname.getText().toString();
        emails = email.getText().toString();
        passw1 = pass1.getText().toString();
        passw2 = pass2.getText().toString();



    }

    public void Register(View view){
        if(name.isEmpty()){
            if(emails.isEmpty()){
                if(passw1.isEmpty() && passw2.isEmpty()){
                    if(passw1.equals(passw2)){
                        Toast.makeText(getApplicationContext(),"Registering your account..",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Your password cannot be empty!",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Your email cannot be empty!",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Your name cannot be empty!",Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed(View view){
        Intent intent = new Intent(RegisterActivity.this,SettingsActivity.class);
        startActivity(intent);
    }
}
