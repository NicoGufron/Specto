package io.agora.openlive.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.location.LocationListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.agora.openlive.R;
import io.agora.openlive.stats.LocalStatsData;
import io.agora.openlive.stats.RemoteStatsData;
import io.agora.openlive.stats.StatsData;
import io.agora.openlive.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.openlive.Constants.ERROR_DIALOG_REQUEST;
import static io.agora.openlive.Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;
import static io.agora.openlive.Constants.PERMISSION_REQUEST_ENABLE_GPS;
import static io.agora.openlive.Constants.PREF_NAME;

public class LiveActivity extends RtcBaseActivity implements OnMapReadyCallback, LocationListener {

    SharedPreferences sharedPreferences;
    private Geocoder geocoder;
    private int notificationId = 1;
    private static final int requestCode = 1000;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String currentPhotoPath;
    public static final String CHANNEL_ID = "misc";
    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn, mSwitchCamera;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private TextView Speed, roomName, addressst;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public double latitude, longitude, longawal,latawal;
    public static final float DEFAULT_ZOOM = 18.0f;
    private float speed;
    private LocationManager locationManager;
    private MapView mMapView;
    private GoogleMap mMap;
    private boolean MapsGranted = false;

    private List<Address> addresses;
    public static String firstAddress;
    private MarkerOptions firstMark,lastMark;
    private Polyline poly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        createNotifChannel();
        //bikin channel notif
        //ui dijalankan pertama kali
        initUI();
        initData();
        mMapView = (MapView) findViewById(R.id.gpsMap);
        initMap(savedInstanceState);
    }

    private void getAddressStreet(double latitude, double longitude) {
        geocoder = new Geocoder(this,Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);

            firstAddress = addresses.get(0).getAddressLine(0);

            addressst.setText(firstAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initUI() {
        addressst = (TextView) findViewById(R.id.address);

        geocoder = new Geocoder(this, Locale.getDefault());
        roomName = (TextView) findViewById(R.id.roomChannel);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        int role = getIntent().getIntExtra(io.agora.openlive.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);

        boolean isBroadcaster = (role == Constants.CLIENT_ROLE_BROADCASTER);

        mSwitchCamera = findViewById(R.id.live_btn_switch_camera);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if(isBroadcaster){
            startBroadcast();

        }else{
            mMuteAudioBtn.setVisibility(View.INVISIBLE);
            mSwitchCamera.setVisibility(View.INVISIBLE);
            mMapView.setVisibility(View.INVISIBLE);
            addressst.setVisibility(View.INVISIBLE);
        }
    }

    private void getLastKnownLocation(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 4, this);
        Task locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    getAddressStreet(latitude, longitude);
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
        //routes api
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
        sharedPreferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("roomName",config().getChannelName()).commit();

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

    //check map services
    private boolean checkMapServices(){
        if(isServicesOk())
            if(isMapsEnabled())
                return true;
        return false;
    }

    //JANGAN DIHAPUS BUAT NOTIF
    private void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notificationUserJoined() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

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
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("A user just joined your channel!")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setFullScreenIntent(fullScreenPendingIntent, true);

            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void Careful() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("You're going too fast! Slow down!")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setFullScreenIntent(fullScreenPendingIntent, true);

            notificationManager.notify(notificationId, builder.build());
        }
    }

    //ambil foto tapi pindah ke aplikasi kamera


    public void goBack(View View) {
        finish();
    }

    public void SettingsIntent(View view) {
        Intent intent = new Intent(LiveActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    //pengecekan jika google services udah nyala
    public boolean isServicesOk(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LiveActivity.this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LiveActivity.this, available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //pengecekan jika location nyala
    public boolean isMapsEnabled(){
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            NoGPS();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case PERMISSION_REQUEST_ENABLE_GPS:{
                if(MapsGranted){
                    Toast.makeText(this,"Thanks for confirming!",Toast.LENGTH_LONG).show();

                }else{
                    getPermission();
                }
            }
        }
    }

    private void NoGPS(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGPS, PERMISSION_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //ganti kamera
    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    private void getPermission(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            MapsGranted = true;
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
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

    //biar tetap bisa ngejalanin mapsnya
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        roomName.setText(sharedPreferences.getString("roomName",config().getChannelName()));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapsGranted = false;
        switch(requestCode){
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    MapsGranted = true;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(checkMapServices())
            getLastKnownLocation();
        else{
            getPermission();
        }
        mMap = googleMap;
//        getLastKnownLocation();
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Initial Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }


    @Override
    public void onLocationChanged(Location currLocation) {

//        speed = currLocation.getSpeed();
//        Speed.setText(speed+ " km/h");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currLocation.getLatitude(),
                        currLocation.getLongitude()), DEFAULT_ZOOM));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(currLocation.getLatitude(),currLocation.getLongitude())).title("Last Location"));

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


    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit().putString("roomName", config().getChannelName()).commit();
    }
}