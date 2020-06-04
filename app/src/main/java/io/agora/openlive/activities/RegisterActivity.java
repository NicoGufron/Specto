package io.agora.openlive.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import java.util.HashMap;

import io.agora.openlive.R;

public class RegisterActivity extends RtcBaseActivity implements AsyncResponse {

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
    }

    public void RegisterAccount(View view){
        name = fname.getText().toString();
        emails = email.getText().toString();
        passw1 = pass1.getText().toString();
        passw2 = pass2.getText().toString();
        if(name.isEmpty()){
            if(emails.isEmpty()){
                if(passw1.isEmpty() && passw2.isEmpty()){
                    if(passw1.equals(passw2)){
                        HashMap postData = new HashMap();
                        postData.put("name",name);
                        postData.put("email",emails);
                        postData.put("password",passw1);
                        PostResponseAsyncTask task = new PostResponseAsyncTask(RegisterActivity.this,postData,RegisterActivity.this);
                        task.execute("https://opensource.petra.ac.id/~m26416094/DVR/android/register.php");
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

    public void onBackArrowPressed(View view){
        Intent intent = new Intent(RegisterActivity.this,SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void processFinish(String s) {
        if(s.equals("success")){
            Toast.makeText(this,"Registered successfully, directing you to Login.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegisterActivity.this,AccountActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Register failed, please try again.", Toast.LENGTH_LONG).show();
        }
    }
}
