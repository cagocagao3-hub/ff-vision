package com.pulse.ffvision;

import android.app.*;
import android.content.Intent;
import android.graphics.*;
import android.hardware.display.*;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import java.nio.ByteBuffer;

public class OverlayService extends Service {
    private WindowManager wm;
    private LinearLayout menuLayout;
    private ESPView espView;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private boolean isEsp = false, isAim = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.parseColor("#AA000000"));
        
        Button btnEsp = new Button(this); btnEsp.setText("ESP: OFF");
        btnEsp.setOnClickListener(v -> { isEsp = !isEsp; btnEsp.setText(isEsp ? "ESP: ON" : "ESP: OFF"); });

        Button btnAim = new Button(this); btnAim.setText("Aimbot: OFF");
        btnAim.setOnClickListener(v -> { isAim = !isAim; btnAim.setText(isAim ? "Aimbot: ON" : "Aimbot: OFF"); });

        menuLayout.addView(btnEsp); menuLayout.addView(btnAim);
        
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START; p.x = 100; p.y = 100;
        wm.addView(menuLayout, p);

        espView = new ESPView(this);
        WindowManager.LayoutParams ep = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        wm.addView(espView, ep);

        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaProjection = manager.getMediaProjection(intent.getIntExtra("resultCode", -1), (Intent)intent.getParcelableExtra("data"));
        
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
        mediaProjection.createVirtualDisplay("Vision", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                if (isEsp || isAim) processImage(image, metrics.widthPixels, metrics.heightPixels);
                image.close();
            }
        }, null);
        return START_STICKY;
    }

    private void processImage(Image image, int w, int h) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        Bitmap bmp = Bitmap.createBitmap(w + image.getPlanes()[0].getRowStride() / image.getPlanes()[0].getPixelStride() - w, h, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);

        int cx = w / 2, cy = h / 2, cDist = Integer.MAX_VALUE, fX = -1, fY = -1;
        for (int y = 200; y < h - 200; y += 15) {
            for (int x = 0; x < w; x += 15) {
                int px = bmp.getPixel(x, y);
                if (Color.red(px) > 150 && Color.green(px) < 80 && Color.blue(px) < 80) {
                    int d = Math.abs(x - cx) + Math.abs(y - cy);
                    if (d < cDist) { cDist = d; fX = x; fY = y; }
                }
            }
        }
        final int tX = fX, tY = fY;
        new Handler(Looper.getMainLooper()).post(() -> {
            espView.setTarget(tX, tY, w, h);
            if (isAim && tX != -1 && AimService.instance != null) {
                AimService.instance.dragAim((tX - cx) * 0.5f, (tY - cy) * 0.5f);
            }
        });
    }

    private Notification createNotification() {
        NotificationChannel ch = new NotificationChannel("v", "Vision", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        return new Notification.Builder(this, "v").setContentTitle("FF Vision ON").setSmallIcon(android.R.drawable.ic_menu_view).build();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
