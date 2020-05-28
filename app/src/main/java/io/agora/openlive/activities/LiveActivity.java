package io.agora.openlive.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.location.LocationListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import io.agora.openlive.R;
import io.agora.openlive.stats.LocalStatsData;
import io.agora.openlive.stats.RemoteStatsData;
import io.agora.openlive.stats.StatsData;
import io.agora.openlive.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class LiveActivity extends RtcBaseActivity implements OnMapReadyCallback, LocationListener {

    private int notificationId = 1;
    private static final int requestCode = 1000;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String currentPhotoPath;
    public static final String CHANNEL_ID = "misc";
    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn, mSwitchCamera;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private TextView Speed, roomName;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public double latitude, longitude;
    public static final float DEFAULT_ZOOM = 18.0f;
    private float speed;
    private LocationManager locationManager;
    private MapView mMapView;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        createNotifChannel();
        initUI();
        initData();
        mMapView = (MapView) findViewById(R.id.gpsMap);
        initMap(savedInstanceState);
    }


    private void initUI() {
        roomName = (TextView) findViewById(R.id.roomChannel);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);
        Speed = (TextView) findViewById(R.id.speed);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Speed.setText( 0 + "km/h");

        int role = getIntent().getIntExtra(io.agora.openlive.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster = (role == Constants.CLIENT_ROLE_BROADCASTER);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if (isBroadcaster) startBroadcast();
    }

    private void getLastKnownLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 10, this);
        Task locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), DEFAULT_ZOOM));
                } else {
                    Log.d("Debug", "Current location is null. Using defaults.");
                    Log.e("Error", "Exception: %s", task.getException());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(0, 0), DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });

        //googlemap api sudah ada harusnya
        //routes api
        //directions api

    }

    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
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


    private void notificationUserJoined() {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent fullScreenIntent = new Intent(this, LiveActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eye)
                .setContentTitle("Specto")
                .setContentText("A user just joined your channel!")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        notificationManager.notify(notificationId, builder.build());
    }

    private void Careful() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent fullScreenIntent = new Intent(this, LiveActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eye)
                .setContentTitle("You're going too fast! Slow down!")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        notificationManager.notify(notificationId, builder.build());
    }

    //ambil foto tapi pindah ke aplikasi kamera
    public void takeaPic(View view) {

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

    public void goBack(View View) {
        finish();
    }

    public void SettingsIntent(View view) {
        Intent intent = new Intent(LiveActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    //ganti kamera
    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    //kasih nama file dari foto yg diambil
    private File getImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("ddMMyyyy").format(new Date());
        String imageName = "Photo1_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

    }

    private boolean checkSelfPermission(String permission, int requestCode) {

        if (ContextCompat.checkSelfPermission(this, permission) !=
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
    private void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //biar tetap bisa ngejalanin mapsnya
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        getLastKnownLocation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        getLastKnownLocation();
        mMap.setMyLocationEnabled(true);
    }

    private LocationManager lastLocation;

    @Override
    public void onLocationChanged(Location currLocation) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currLocation.getLatitude(),
                        currLocation.getLongitude()), DEFAULT_ZOOM));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
//
//    private Location lastLocation;
//
//    @Override
//    public void onLocationChanged(Location location) {
//
////        getLastKnownLocation();
//        if(location != null){
//            speed = Math.sqrt(Math.pow(location.getLongitude()));
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
}
