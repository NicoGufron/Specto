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
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.agora.openlive.R;
import io.agora.openlive.utils.ScanFile;

import static io.agora.openlive.Constants.ERROR_DIALOG_REQUEST;
import static io.agora.openlive.Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;
import static io.agora.openlive.Constants.PERMISSION_REQUEST_ENABLE_GPS;
import static io.agora.openlive.activities.LiveActivity.CHANNEL_ID;
import static io.agora.openlive.activities.LiveActivity.DEFAULT_ZOOM;

public class RecordMode extends BaseActivity implements OnMapReadyCallback, LocationListener {

    private CameraDevice mCameraDevice;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private GoogleMap mMap;
    private String mCameraId;
    private MediaRecorder recorder;
    private TextureView textureView;
    private Size mVideosize, mPreviewSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    public Double latitude, longitude, latawal,longawal;
    private String mVideoFileName;
    private ImageView startRecord, stopRecord;
    private int mTotalRotation;
    private File videoDir, videoFile;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private boolean mIsRecording = false;
    private boolean MapsGranted = false;
    private boolean stop = true;
    private CameraCaptureSession mCameraCaptureSession;
    private TextView alamat, speedometer;
    private int notificationId = 1;
    private List<Address> addressList;
    private MapView mMapView;
    private int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean once = false;

    //pengecekan map dan permission
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
//        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Initial Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
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
    private void getPermission(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            MapsGranted = true;
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    public boolean isMapsEnabled(){
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            NoGPS();
            return false;
        }
        return true;
    }

    private boolean checkMapServices(){
        if(isServicesOk())
            if(isMapsEnabled())
                return true;
        return false;
    }
    public boolean isServicesOk(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RecordMode.this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(RecordMode.this, available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
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
                    getAddress(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), DEFAULT_ZOOM));
                    once = true;
                } else {
                    Log.d("Debug", "Current location is null. Using defaults.");
                    Log.e("Error", "Exception: %s", task.getException());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(0, 0), DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.hasSpeed()){
            speedometer.setText(String.valueOf(location.getSpeed()));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), DEFAULT_ZOOM));
        if(stop){
//            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Last Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

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

    //MAP STOP DISINI

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
                    (long)(rhs.getWidth() * rhs.getHeight()));
        }
    }



    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            recorder = new MediaRecorder();

            if(mIsRecording){
                try {
                    createVideoFileName();
                    startRecord();
                    recorder.start();
                    stop = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                startPreview();
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mode);
        createNotifChannel();
        initUI();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createVideoFolder();

        initMap(savedInstanceState);

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRecording = true;
                checkWriteStoragePermission();
                startRecord.setEnabled(false);
                stopRecord.setEnabled(true);
                videoStart();
            }
        });
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsRecording){
                    mIsRecording = false;
                    recorder.stop();
                    recorder.reset();
                    startPreview();
                    startRecord.setEnabled(true);
                    stopRecord.setEnabled(false);
                    videoSaved();

                }
            }
        });
    }
    public void switchCamera(View view){
        Intent intent = new Intent(RecordMode.this, DVRmode.class);
        startActivity(intent);
    }
    public void toRole(View view){
        Intent intent = new Intent(RecordMode.this,RoleActivity.class);
        startActivity(intent);
    }
    public void toSettings(View view){
        Intent intent = new Intent(RecordMode.this, SettingsActivity.class);
        startActivity(intent);
    }
    private void getAddress(Double latitude, Double longitude){
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(latitude,longitude,1);
            String address = addressList.get(0).getAddressLine(0);
            alamat.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initUI(){
        mMapView = (MapView) findViewById(R.id.gpsMap1);
        textureView = (TextureView) findViewById(R.id.textureViewVid);
        startRecord = (ImageView) findViewById(R.id.startRecord);
        stopRecord = (ImageView) findViewById(R.id.stopRecord);
        alamat = (TextView) findViewById(R.id.alamatrecord);
        speedometer = (TextView) findViewById(R.id.speedometer);
        speedometer.setText("0.0 km/h");
    }
    private void createVideoFolder(){
        File moviefile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        videoDir = new File(moviefile, "Specto");
        if(!videoDir.exists()){
            videoDir.mkdirs();
        }
    }
    private void videoStart() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("Recording...")
                    .setPriority(NotificationManager.IMPORTANCE_HIGH);

            notificationManager.notify(notificationId, builder.build());
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("Recording...")
                    .setPriority(NotificationCompat.PRIORITY_MAX);
//                    .setFullScreenIntent(fullScreenPendingIntent, true);

            notificationManager.notify(notificationId, builder.build());
        }
    }
    private void videoSaved() {


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("Video saved in " + mVideoFileName)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH);

            notificationManager.notify(notificationId, builder.build());
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Intent fullScreenIntent = new Intent(this, LiveActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_eye)
                    .setContentTitle("Specto")
                    .setContentText("Video saved in " + mVideoFileName)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            notificationManager.notify(notificationId, builder.build());
        }
    }

    private File createVideoFileName() throws IOException{
        String timestamp = new SimpleDateFormat("ddMMyyyy").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        videoFile = File.createTempFile(prepend,".mp4",videoDir);
        mVideoFileName = videoFile.getAbsolutePath();
        new ScanFile(getApplicationContext(), videoFile);
        return videoFile;
    }

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


    private void setupRecorder() throws IOException{
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(mVideoFileName);
        recorder.setVideoEncodingBitRate(5000000);
        recorder.setVideoFrameRate(15);
        recorder.setVideoSize(mVideosize.getWidth(), mVideosize.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOrientationHint(mTotalRotation);
        recorder.prepare();
    }

    private void startRecord(){
        try {
            setupRecorder();
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mVideosize.getWidth(), mVideosize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = recorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
//                    mCameraCaptureSession = session;
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(RecordMode.this,"Failed",Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(io.agora.openlive.Constants.MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    private void startPreview(){
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mVideosize.getWidth(), mVideosize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(),null,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull  CameraCaptureSession session) {
                        Toast.makeText(RecordMode.this,"Failed",Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

   private void checkWriteStoragePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mIsRecording){
                    startRecord();
                    recorder.start();
                }
            }else{
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
            //versi yang lebih tua
        }else{
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }if(mIsRecording){
                startRecord();
                recorder.start();
            }
        }
   }


    private void connectCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupCamera(int width, int height){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideosize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrienatation + deviceOrientation + 360) % 360;
    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : choices) {
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        mMapView.onResume();
        super.onResume();
        if(textureView.isAvailable()){
            setupCamera(textureView.getWidth(),textureView.getHeight());
            connectCamera();
        }else{
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
//        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Initial Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2VideoImage");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}