package com.example.yoline.cameratest;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback,SurfaceHolder.Callback{

    ImageView iv;
    TextView tv;
    SurfaceView sView;
    Bitmap bitmap;
    Bitmap mDrawBmp;
    Camera camera;
    private int mWidth = 640;
    private int mHeight = 480;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Boolean isProcess;

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        tv = (TextView)findViewById(R.id.tv);
        iv = (ImageView)findViewById(R.id.iv);
        sView = (SurfaceView)findViewById(R.id.sView);
        sView.getHolder().addCallback(this);

        isProcess = false;
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera temp) {
        synchronized (isProcess) {
            if (isProcess) {
                return;
            }

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    isProcess = true;
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if (image != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            stream.close();
                            mDrawBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv.setImageBitmap(mDrawBmp);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isProcess = false;
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            camera = Camera.open(0);
            camera.setPreviewDisplay(holder);
            Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(mWidth, mHeight);
            camera.setParameters(params);
            //   camera.setDisplayOrientation(90);
            camera.startPreview();
            camera.setPreviewCallback(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera != null)  camera.release();
        camera = null;
    }
}
