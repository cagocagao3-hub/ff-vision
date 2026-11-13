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
import java.util.ArrayList;
import java.util.List;

public class OverlayService extends Service {
    private WindowManager wm;
    private LinearLayout menuLayout, minimizedLayout;
    private ESPView espView;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private boolean isEsp = false, isAim = false;
    private int screenW, screenH;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;

        setupMenu();
        setupESP();

        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaProjection = manager.getMediaProjection(intent.getIntExtra("resultCode", -1), (Intent)intent.getParcelableExtra("data"));
        
        imageReader = ImageReader.newInstance(screenW, screenH, PixelFormat.RGBA_8888, 2);
        mediaProjection.createVirtualDisplay("Vision", screenW, screenH, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                if (isEsp || isAim) processImage(image);
                image.close();
            }
        }, null);
        return START_STICKY;
    }

    private void setupMenu() {
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(30, 30, 30, 30);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E60F0F12"));
        bg.setCornerRadius(30f);
        bg.setStroke(4, Color.parseColor("#00FFCC"));
        menuLayout.setBackground(bg);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        
        TextView title = new TextView(this);
        title.setText("PULSE V3");
        title.setTextColor(Color.parseColor("#00FFCC"));
        title.setTextSize(18f);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        
        Button btnMin = new Button(this);
        btnMin.setText("_");
        btnMin.setTextColor(Color.WHITE);
        btnMin.setBackgroundColor(Color.TRANSPARENT);
        btnMin.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            minimizedLayout.setVisibility(View.VISIBLE);
        });

        header.addView(title);
        header.addView(btnMin);
        menuLayout.addView(header);

        Button btnEsp = createButton("ESP: OFF");
        btnEsp.setOnClickListener(v -> { isEsp = !isEsp; btnEsp.setText(isEsp ? "ESP: ON" : "ESP: OFF"); btnEsp.setTextColor(isEsp ? Color.GREEN : Color.WHITE); });

        Button btnAim = createButton("AIMBOT: OFF");
        btnAim.setOnClickListener(v -> { isAim = !isAim; btnAim.setText(isAim ? "AIMBOT: ON" : "AIMBOT: OFF"); btnAim.setTextColor(isAim ? Color.GREEN : Color.WHITE); });

        menuLayout.addView(btnEsp); menuLayout.addView(btnAim);
        
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START; p.x = 50; p.y = 200;
        makeDraggable(menuLayout, p);
        wm.addView(menuLayout, p);

        // Layout Minimizado (A bolinha)
        minimizedLayout = new LinearLayout(this);
        GradientDrawable bgMin = new GradientDrawable();
        bgMin.setColor(Color.parseColor("#E60F0F12"));
        bgMin.setCornerShape(50);
        bgMin.setStroke(4, Color.parseColor("#00FFCC"));
        minimizedLayout.setBackground(bgMin);
        
        Button btnOpen = new Button(this);
        btnOpen.setText("V");
        btnOpen.setTextColor(Color.parseColor("#00FFCC"));
        btnOpen.setBackgroundColor(Color.TRANSPARENT);
        btnOpen.setOnClickListener(v -> {
            minimizedLayout.setVisibility(View.GONE);
            menuLayout.setVisibility(View.VISIBLE);
        });
        minimizedLayout.addView(btnOpen);
        
        WindowManager.LayoutParams pMin = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        pMin.gravity = Gravity.TOP | Gravity.START; pMin.x = 50; pMin.y = 200;
        makeDraggable(minimizedLayout, pMin);
        wm.addView(minimizedLayout, pMin);
        minimizedLayout.setVisibility(View.GONE);
    }

    private void setupESP() {
        espView = new ESPView(this);
        WindowManager.LayoutParams ep = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        wm.addView(espView, ep);
    }

    private Button createButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextColor(Color.WHITE);
        return btn;
    }

    private void makeDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initX, initY;
            private float initTouchX, initTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initX = params.x; initY = params.y;
                    initTouchX = event.getRawX(); initTouchY = event.getRawY();
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = initX + (int) (event.getRawX() - initTouchX);
                    params.y = initY + (int) (event.getRawY() - initTouchY);
                    wm.updateViewLayout(view, params);
                    return true;
                }
                return false;
            }
        });
    }

    private void processImage(Image image) {
        // Pega a imagem da tela
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        Bitmap bmp = Bitmap.createBitmap(screenW + image.getPlanes()[0].getRowStride() / image.getPlanes()[0].getPixelStride() - screenW, screenH, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);

        List<PointF> foundTargets = new ArrayList<>();
        int cx = screenW / 2, cy = screenH / 2;
        int cDist = Integer.MAX_VALUE;
        PointF bestTarget = null;

        // Varre a tela toda procurando os nomes/barra vermelha
        for (int y = 200; y < screenH - 200; y += 15) {
            for (int x = 0; x < screenW; x += 15) {
                int px = bmp.getPixel(x, y);
                int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                
                if (r > 140 && g < 70 && b < 70) { // Filtro de vermelho
                    // Agrupa pixels próximos para não criar 500 quadrados no mesmo cara
                    boolean isNewTarget = true;
                    for (PointF t : foundTargets) {
                        if (Math.abs(t.x - x) < 100 && Math.abs(t.y - y) < 150) {
                            isNewTarget = false;
                            break;
                        }
                    }
                    
                    if (isNewTarget) {
                        PointF target = new PointF(x, y);
                        foundTargets.add(target);
                        
                        // Lógica do Aimbot: Achar o mais perto do centro da mira
                        int d = Math.abs(x - cx) + Math.abs(y - cy);
                        if (d < cDist) {
                            cDist = d;
                            bestTarget = target;
                        }
                    }
                }
            }
        }
        
        final List<PointF> finalTargets = foundTargets;
        final PointF finalBest = bestTarget;

        new Handler(Looper.getMainLooper()).post(() -> {
            espView.updateTargets(finalTargets);
            
            // Se o Aimbot estiver ligado e tiver um alvo na tela
            if (isAim && finalBest != null && AimService.instance != null) {
                // Só puxa se o cara estiver num raio de 500px do centro
                if (Math.abs(finalBest.x - cx) < 500 && Math.abs(finalBest.y - cy) < 500) {
                    AimService.instance.dragAim((finalBest.x - cx) * 1.5f, (finalBest.y - cy) * 1.5f);
                }
            }
        });
    }

    private Notification createNotification() {
        NotificationChannel ch = new NotificationChannel("v", "Vision", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        return new Notification.Builder(this, "v").setContentTitle("Pulse Vision ON").setSmallIcon(android.R.drawable.ic_menu_view).build();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
