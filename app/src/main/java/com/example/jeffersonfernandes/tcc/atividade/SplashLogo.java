package com.example.jeffersonfernandes.tcc.atividade;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jeffersonfernandes.tcc.R;

public class SplashLogo extends AppCompatActivity {

    ImageView logoProjeto, logoIfce;
    TextView textMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_logo);

        logoProjeto = findViewById(R.id.imageProjeto);
        logoIfce = findViewById(R.id.imageIfce);
        //textMonitor = findViewById(R.id.textMonitor);

        Animation animacao = AnimationUtils.loadAnimation(this, R.anim.animation_splash);
        logoProjeto.setAnimation(animacao);
        logoIfce.setAnimation(animacao);
        //textMonitor.setAnimation(animacao);

        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(5000);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        myThread.start();

    }
}
