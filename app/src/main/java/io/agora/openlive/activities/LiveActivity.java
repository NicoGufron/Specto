package io.agora.openlive.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agora.openlive.R;
import io.agora.openlive.stats.LocalStatsData;
import io.agora.openlive.stats.RemoteStatsData;
import io.agora.openlive.stats.StatsData;
import io.agora.openlive.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class LiveActivity extends RtcBaseActivity implements OnMapReadyCallback {
//    private static final String TAG = LiveActivity.class.getSimpleName();

    private int notificationId = 1;
    private static final int requestCode = 1000;
    private final int REQUEST_IMAGE_CAPTURE  =1;
    String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String currentPhotoPath;
    public static final String CHANNEL_ID = "misc";
    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn, mSwitchCamera;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private TextView Speed, roomName;

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        mMapView = (MapView) findViewById(R.id.gpsMap);
        initUI();
        initMap(savedInstanceState);
        initData();
        createNotifChannel();
    }

    private void initUI() {
        roomName = (TextView) findViewById(R.id.roomChannel);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);
        Speed = (TextView) findViewById(R.id.speed);

        int role = getIntent().getIntExtra(io.agora.openlive.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster =  (role == Constants.CLIENT_ROLE_BROADCASTER);

//        initLocation();


//        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
//        mMuteVideoBtn.setActivated(isBroadcaster);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

//        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
//        beautyBtn.setActivated(true);
//        rtcEngine().setBeautyEffectOptions(beautyBtn.isActivated(),
//                io.agora.openlive.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        //mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if (isBroadcaster) startBroadcast();
    }

    private void initMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(io.agora.openlive.Constants.MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    private void initData() {
        mVideoDimension = io.agora.openlive.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

    private void startBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surface = prepareRtcVideo(0, true);
        mVideoGridContainer.addUserVideoSurface(0, surface, true);
        rtcEngine().switchCamera();
        mMuteAudioBtn.setActivated(true);
    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        mMuteAudioBtn.setActivated(false);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        notificationUserJoined();
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    private void notificationUserJoined(){

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent fullScreenIntent = new Intent(this, LiveActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this,0,fullScreenIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eye)
                .setContentTitle("Specto")
                .setContentText("A user just joined your channel!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent,true);

        notificationManager.notify(notificationId, builder.build());
    }

    private void Careful(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent fullScreenIntent = new Intent(this,LiveActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this,0,fullScreenIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eye)
                .setContentTitle("You're going too fast! Slow down!")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent,true);

        notificationManager.notify(notificationId,builder.build());
    }

    //ambil foto tapi pindah ke aplikasi kamera
    public void takeaPic(View view) {

        Toast.makeText(getApplicationContext(), "Under testing", Toast.LENGTH_SHORT).show();
        Log.e("error_pic","Error masuk sini");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "io.agora.openlive.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void goBack(View View){
        finish();
    }

    public void SettingsIntent(View view){
        Intent intent = new Intent(LiveActivity.this,SettingsActivity.class);
        startActivity(intent);
    }

    //ganti kamera
    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }
    //kasih nama file dari foto yg diambil
    private File getImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("ddMMyyyy").format(new Date());
        String imageName = "Photo1_"+timestamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg",storageDir);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

    }

    private boolean checkSelfPermission(String permission, int requestCode){

        if( ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSION, requestCode);
            return false;
        }

        return true;
    }
    //mute audio
    public void onMuteAudioClicked(View view) {
        rtcEngine().muteLocalAudioStream(view.isActivated());
        view.setActivated(!view.isActivated());
    }

    //JANGAN DIHAPUS
    private void createNotifChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void updateSpeed(LocationClass location){
        float currentSpeed = 0;

        if(location != null){
            currentSpeed = location.getSpeed();
            Speed.setText(currentSpeed + "km/h");
            if(currentSpeed >= 45){
                Speed.setTextColor(Color.RED);
                Careful();
            }
        }
    }

    //biar tetap bisa ngejalanin mapsnya
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
        googleMap.setMyLocationEnabled(true);

    }


//    public void onMuteVideoClicked(View view) {
//        if (view.isActivated()) {
//            stopBroadcast();
//        } else {
//            startBroadcast();
//        }
//        view.setActivated(!view.isActivated());
//    }
}
