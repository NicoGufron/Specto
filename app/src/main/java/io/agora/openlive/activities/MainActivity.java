package io.agora.openlive.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.openlive.R;

public class MainActivity extends BaseActivity {

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private EditText mChannel;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkSelfPermission(PERMISSIONS[0], PERMISSION_REQ_CODE) &&
                checkSelfPermission(PERMISSIONS[1], PERMISSION_REQ_CODE) &&
                checkSelfPermission(PERMISSIONS[2], PERMISSION_REQ_CODE)){
            permissionGranted();
        }else{
            Toast.makeText(getApplicationContext(),"Please accept all permissions before continue.",Toast.LENGTH_SHORT).show();
        }

        mChannel = (EditText) findViewById(R.id.roomChannel);
        btn = (Button)findViewById(R.id.startbtn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RoleActivity.class);
                String room = mChannel.getText().toString();
                config().setChannelName(room);
                startActivity(intent);
            }
        });


    }
    private void permissionGranted(){

    }
    private boolean checkSelfPermission(String permission, int requestCode){
        if( ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    public void onSettingClicked(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}