package com.pocket.knowledge.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.pocket.knowledge.R;
import com.pocket.knowledge.config.UiConfig;
import com.pocket.knowledge.utils.ThemePref;
import com.pocket.knowledge.utils.Tools;

public class SplashActivity extends AppCompatActivity {

    Boolean isCancelled = false;
    private ProgressBar progressBar;
    long nid = 0;
    String url = "";
    ThemePref themePref;
    ImageView img_splash;
    RelativeLayout rlParentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);

        themePref = new ThemePref(this);

        img_splash = findViewById(R.id.img_splash);
        rlParentView=findViewById(R.id.parent_view);
        if (themePref.getIsDarkTheme()) {
            //img_splash.setImageResource(R.drawable.bg_splash_dark);
            rlParentView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        } else {
            //img_splash.setImageResource(R.drawable.bg_splash_default);
            rlParentView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra("nid")) {
            nid = getIntent().getLongExtra("nid", 0);
            url = getIntent().getStringExtra("external_link");
        }

        new Handler().postDelayed(() -> {
            if (!isCancelled) {
                if (nid == 0) {
                    if (url.equals("") || url.equals("no_url")) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);

                        finish();
                    }
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    Intent intent = new Intent(getApplicationContext(), NotificationDetailActivity.class);
                    intent.putExtra("id", nid);
                    startActivity(intent);
                    finish();
                }
            }
        }, UiConfig.SPLASH_TIME);

    }
}