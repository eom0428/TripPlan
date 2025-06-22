package com.example.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton menuBtn;
    private Button btnMakePlan;
    private Button btnRecommend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        btnMakePlan = (Button)findViewById(R.id.btnMakePlan);
        btnMakePlan.setOnClickListener(this);
        btnRecommend = (Button)findViewById(R.id.btnReco);
        btnRecommend.setOnClickListener(this);

        /*if (isLoggedIn) {
            // 로그인된 상태
            finish();
        } else {
            // 로그인 화면으로 이동
            startActivity(new Intent(this, Login.class));
            finish();
        }*/

        menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

            MenuItem loginItem = popupMenu.getMenu().findItem(R.id.menu_login);
            if (isLoggedIn) {
                loginItem.setTitle("로그아웃");
            } else {
                loginItem.setTitle("로그인");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_login) {
                    if (isLoggedIn) {
                        // 로그아웃 처리
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                        recreate();


                    } else {
                        // 로그인 화면으로 이동
                        Intent intentLogin = new Intent(MainActivity.this, Login.class);
                        startActivity(intentLogin);
                    }
                    return true;

                } else if (id == R.id.menu_history) {
                    // 저장한 계획 화면으로 이동
                    Intent intentLoadPlan = new Intent(MainActivity.this, LoadPlan.class);
                    startActivity(intentLoadPlan);
                    return true;

                } else if (id == R.id.menu_preference) {
                    // 취향작성 화면으로 이동
                    Intent intentPreference = new Intent(MainActivity.this, preference.class);
                    startActivity(intentPreference);
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view){
        if(view == btnMakePlan){
            Intent intentPlan = new Intent(MainActivity.this, schedule_planning.class);
            startActivity(intentPlan);
        }else{
            Intent intentReco = new Intent(MainActivity.this, RecommendLocation.class);
            startActivity(intentReco);
        }
    }
}