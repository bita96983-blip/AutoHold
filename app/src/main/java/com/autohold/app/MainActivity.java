package com.autohold.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 120, 60, 60);

        TextView title = new TextView(this);
        title.setText("تطبيق الضغطة المستمرة (AutoHold)\n\nاتبع الخطوتين بالترتيب لتشغيل الزر العائم:\n");
        title.setTextSize(18);
        layout.addView(title);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("1. تفعيل الظهور فوق التطبيقات");
        btnOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        layout.addView(btnOverlay);

        Button btnAccess = new Button(this);
        btnAccess.setText("2. تفعيل خدمة إمكانية الوصول (AutoHold)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        setContentView(layout);
    }
}
