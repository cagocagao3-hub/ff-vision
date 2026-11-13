package com.pulse.ffvision;

import android.app.*;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
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
        
        // --- CRIAÇÃO DO MENU MODERNO ---
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(30, 30, 30, 30);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E6000000"));
        bg.setCornerRadius(30f);
        bg.setStroke(3, Color.parseColor("#BB86FC"));
        menuLayout.setBackground(bg);
        
        TextView title = new TextView(this);
        title.setText("FF VISION V2");
        title.setTextColor(Color.parseColor("#BB86FC"));
        title.setTextSize(18f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        menuLayout.addView(title);

        Button btnEsp = createMenuButton("ESP: OFF");
        btnEsp.setOnClickListener(v -> { isEsp = !isEsp; btnEsp.setText(isEsp ? "ESP: ON" : "ESP: OFF"); btnEsp.setTextColor(isEsp ? Color.GREEN : Color.WHITE); });

        Button btnAim = createMenuButton("AIMBOT: OFF");
        btnAim.setOnClickListener(v -> { isAim = !isAim; btnAim.setText(isAim ? "AIMBOT: ON" : "AIMBOT: OFF"); btnAim.setTextColor(isAim ? Color.GREEN : Color.WHITE); });

        menuLayout.addView(btnEsp); menuLayout.addView(btnAim);
        
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START; p.x = 50; p.y = 200;
        
        // Permite arrastar o menu
        menuLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = p.x; initialY = p.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        p.x = initialX + (int) (event.getRawX() - initialTouchX);
                        p.y = initialY + (int) (event.getRawY() - initialTouchY);
                        wm.updateViewLayout(menuLayout, p);
                        return true;
                }
                return false;
            }
        });
        wm.addView(menuLayout, p);

        // --- CRIAÇÃO DO ESP ---
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

    private Button createMenuButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14f);
        return btn;
    }

    private void processImage(Image image, int w, int h) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        Bitmap bmp = Bitmap.createBitmap(w + image.getPlanes()[0].getRowStride() / image.getPlanes()[0].getPixelStride() - w, h, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);

        int cx = w / 2, cy = h / 2, cDist = Integer.MAX_VALUE, fX = -1, fY = -1;
        
        // Otimização: Varrer a tela em blocos maiores para não travar
        for (int y = 200; y < h - 200; y += 12) {
            for (int x = 0; x < w; x += 12) {
                int px = bmp.getPixel(x, y);
                int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                
                // Filtro de Vermelho do Free Fire (Nome/Barra de vida)
                if (r > 150 && g < 80 && b < 80) {
                    int d = Math.abs(x - cx) + Math.abs(y - cy);
                    if (d < cDist) { cDist = d; fX = x; fY = y; }
                }
            }
        }
        
        final int tX = fX, tY = fY;
        new Handler(Looper.getMainLooper()).post(() -> {
            espView.setTarget(tX, tY, w, h);
            
            // SNAP AIMBOT: Puxa com força total para a cabeça
            if (isAim && tX != -1 && AimService.instance != null) {
                // Multiplicador 1.0 = Puxa exatamente para o centro do alvo
                AimService.instance.dragAim((tX - cx) * 1.0f, (tY - cy) * 1.0f);
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
