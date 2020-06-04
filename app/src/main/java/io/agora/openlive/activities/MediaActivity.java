package io.agora.openlive.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import io.agora.openlive.R;

import static io.agora.openlive.activities.AccountActivity.hasLog;
import static io.agora.openlive.activities.AccountActivity.loademail;

public class MediaActivity extends BaseActivity {

    private String LOG = "hadeh";

    private String whose = loademail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        PostResponseAsyncTask task = new PostResponseAsyncTask(MediaActivity.this, new AsyncResponse() {
            @Override
            public void processFinish(String s) {
                Log.d(LOG,s);
            }
        });
        task.execute("https://opensource.petra.ac.id/~m26416094/DVR/android/getVideos.php");
    }
}
