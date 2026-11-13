package com.pulse.ffvision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private MediaProjectionManager projectionManager;
    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#121212"));
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("FF VISION V2");
        title.setTextColor(Color.parseColor("#BB86FC"));
        title.setTextSize(32f);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        Button btnOverlay = createButton("1. Permitir Tela (Overlay)");
        btnOverlay.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));

        Button btnAim = createButton("2. Ativar Acessibilidade");
        btnAim.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "Ative o FF Vision", Toast.LENGTH_LONG).show();
        });

        Button btnStart = createButton("INICIAR CHEAT");
        btnStart.setBackgroundColor(Color.parseColor("#03DAC6"));
        btnStart.setTextColor(Color.BLACK);
        btnStart.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            } else {
                Toast.makeText(this, "De a permissao de Overlay!", Toast.LENGTH_SHORT).show();
            }
        });

        layout.addView(btnOverlay);
        layout.addView(btnAim);
        layout.addView(btnStart);
        setContentView(layout);
    }

    private Button createButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundColor(Color.parseColor("#333333"));
        btn.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 30, 0, 30);
        btn.setLayoutParams(params);
        return btn;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, OverlayService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            startForegroundService(serviceIntent);
            finish();
        }
    }
}
