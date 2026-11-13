package com.pulse.ffvision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
        layout.setBackgroundColor(Color.parseColor("#0F0F12"));
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("PULSE VISION V3");
        title.setTextColor(Color.parseColor("#00FFCC"));
        title.setTextSize(34f);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        layout.addView(createButton("1. Dar Permissão de Overlay", () -> 
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())))));

        layout.addView(createButton("2. Ativar Acessibilidade (Aimbot)", () -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "Ative o Pulse Vision", Toast.LENGTH_LONG).show();
        }));

        layout.addView(createButton("3. INICIAR CHEAT", () -> {
            if (Settings.canDrawOverlays(this)) {
                startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            } else {
                Toast.makeText(this, "Dê a permissão de Overlay!", Toast.LENGTH_SHORT).show();
            }
        }));

        setContentView(layout);
    }

    private Button createButton(String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundColor(Color.parseColor("#1E1E24"));
        btn.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 25, 0, 25);
        btn.setLayoutParams(p);
        btn.setOnClickListener(v -> action.run());
        return btn;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Intent s = new Intent(this, OverlayService.class);
            s.putExtra("resultCode", resultCode);
            s.putExtra("data", data);
            startForegroundService(s);
            finish();
        }
    }
}
