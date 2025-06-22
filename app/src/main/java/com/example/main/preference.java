package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class preference extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button btnOk;
    private Button btnBack;
    private EditText editPre1;
    private EditText editPre2;
    private String pre1;
    private String pre2;
    private static final String TAG1 = "db";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preference);

        // 로그인 여부 확인
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // 로그인 안 되어 있으면 로그인 화면으로 이동
            Intent intent = new Intent(preference.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        btnBack = findViewById(R.id.btnHome);
        btnBack.setOnClickListener(this);
        editPre1 = findViewById(R.id.editPre1);
        editPre2 = findViewById(R.id.editPre2);

        db = FirebaseFirestore.getInstance();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pre1 = editPre1.getText().toString().trim();
                pre2 = editPre2.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editPre1.addTextChangedListener(textWatcher);
        editPre2.addTextChangedListener(textWatcher);

        loadExistingPreferences();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view){
        if(view == btnOk){
            setDb(pre1,pre2);
        }else{
            Intent intentback = new Intent(preference.this, MainActivity.class);
            startActivity(intentback);
        }
    }

    public void setDb(String pre1, String pre2){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);       // Firebase 고유 사용자 ID 추가
        data.put("preference1", pre1);
        data.put("preference2", pre2);     // 사용자가 입력한 ID (닉네임 등)

        db.collection("preference").document(uid) // UID 기준으로 문서 생성
            .set(data)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "저장 성공", Toast.LENGTH_SHORT).show();
                    Log.d(TAG1, "DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_SHORT).show();
                    Log.w(TAG1, "Error writing document", e);
                }
            });
    }

    private void loadExistingPreferences() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("preference").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 기존 데이터가 있는 경우 - EditText에 기존 값 설정
                        String existingPre1 = documentSnapshot.getString("preference1");
                        String existingPre2 = documentSnapshot.getString("preference2");

                        if (existingPre1 != null) {
                            editPre1.setText(existingPre1);
                        }
                        if (existingPre2 != null) {
                            editPre2.setText(existingPre2);
                        }

                        Log.d(TAG1, "기존 취향 데이터를 불러왔습니다.");
                    } else {
                        // 기존 데이터가 없는 경우 - EditText는 빈 상태로 유지
                        Log.d(TAG1, "첫 사용자입니다. 빈 상태로 유지합니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG1, "취향 데이터 불러오기 실패", e);
                    // 실패해도 EditText는 빈 상태로 유지됨
                });
    }
}