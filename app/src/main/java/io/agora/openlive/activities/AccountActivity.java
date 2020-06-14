package io.agora.openlive.activities;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import io.agora.openlive.R;
import static io.agora.openlive.Constants.SHARED_PREFS;

// gak dipake ini activity, ignore activity

public class AccountActivity extends BaseActivity implements AsyncResponse, View.OnClickListener {

    private Button Login,Logout;
    private EditText email, password;
    private TextView register, textRegister, explain, hello, logged, fullname, emaillog;
    private String emailcek, passwordcek;

    public static final String emaildef = "email1";
    public static final String namedef = "name";

    public static String loademail;
    private String loadname;

    private String JSON_STRING;

    public static boolean hasLog = false;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initUI();
        loadData();
        updateViews();
        Login.setOnClickListener(this);

    }
    private void initUI(){

        //kalo udh login pake ini
        logged = (TextView) findViewById(R.id.logged);
        emaillog = (TextView) findViewById(R.id.emaillog);
        //buat yg sbelom login
        register = (TextView) findViewById(R.id.register);
        explain = (TextView) findViewById(R.id.explain);
        textRegister = (TextView) findViewById(R.id.textRegister);
        //ini signup
        register.setPaintFlags(register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Login = (Button) findViewById(R.id.login);
        Logout = (Button) findViewById(R.id.logout);

        email = (EditText)findViewById(R.id.accountEmail);
        password = (EditText) findViewById(R.id.password);

        logged.setVisibility(View.INVISIBLE);
        emaillog.setVisibility(View.INVISIBLE);

    }



    public void toRegister(View view){
        Intent intent = new Intent(AccountActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    public void onBackArrowPressed(View view){
        Intent intent = new Intent(AccountActivity.this,SettingsActivity.class);
        startActivity(intent);
    }

    public void saveData(String emaildeh){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(emaildef,loademail);

        editor.apply();
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loademail = sharedPreferences.getString(emaildef,"");
    }
    public void updateViews(){
        //        belom login
        if(hasLog == false) {
            register.setVisibility(View.VISIBLE);
            textRegister.setVisibility(View.VISIBLE);
            explain.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            Login.setVisibility(View.VISIBLE);

            Logout.setVisibility(View.INVISIBLE);
            logged.setVisibility(View.INVISIBLE);
            emaillog.setVisibility(View.INVISIBLE);
        }else{
            //invisible pas udh login
            register.setVisibility(View.INVISIBLE);
            textRegister.setVisibility(View.INVISIBLE);
            explain.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
            Login.setVisibility(View.INVISIBLE);

            //buat visible kalo udh login
            Logout.setVisibility(View.VISIBLE);
            logged.setVisibility(View.VISIBLE);
            emaillog.setVisibility(View.VISIBLE);
            emaillog.setText(loademail);
        }
    }
    @Override
    public void processFinish(String result) {
        if(result.equals("success")){
            Toast.makeText(this,"Logged in successfully!",Toast.LENGTH_SHORT).show();
            loademail = email.getText().toString();
            saveData(loademail);
            Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Login failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        HashMap postData = new HashMap();
        postData.put("email",email.getText().toString());
        postData.put("password",password.getText().toString());
        PostResponseAsyncTask task = new PostResponseAsyncTask(AccountActivity.this, postData, AccountActivity.this);
        task.execute("https://opensource.petra.ac.id/~m26416094/DVR/android/login.php");
        hasLog = true;
    }
}
