package com.pulse.ffvision;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private MediaProjectionManager projectionManager;
    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("1. Ativar Permissao de Tela (Overlay)");
        btnOverlay.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));

        Button btnAim = new Button(this);
        btnAim.setText("2. Ativar Aimbot (Acessibilidade)");
        btnAim.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "Ative o FF Vision", Toast.LENGTH_LONG).show();
        });

        Button btnStart = new Button(this);
        btnStart.setText("3. INICIAR VISION");
        btnStart.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            } else {
                Toast.makeText(this, "De a permissao de Overlay primeiro!", Toast.LENGTH_SHORT).show();
            }
        });

        layout.addView(btnOverlay);
        layout.addView(btnAim);
        layout.addView(btnStart);
        setContentView(layout);
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
