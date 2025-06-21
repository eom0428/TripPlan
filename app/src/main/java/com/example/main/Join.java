package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Join extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button join;
    private EditText email0;
    private EditText password0;
    private EditText id1;
    private String id;
    private static final String TAG = "test";
    private static final String TAG1 = "db";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join);

        //firebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        join = (Button)findViewById(R.id.btnJoin);
        join.setOnClickListener(this);
        email0 = (EditText)findViewById(R.id.editTextEmailLo);
        password0= (EditText)findViewById(R.id.editTextPassword);
        id1 = findViewById(R.id.editTextId);


        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view){
        String email, password;
        email = email0.getText().toString();
        password = password0.getText().toString();
        id = id1.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            setDb(email,id);
                            //Toast.makeText(Test.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Join.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Join.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(Join.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "회원가입 성공! 사용자: " + id, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "회원가입 실패...", Toast.LENGTH_SHORT).show();
        }
    }

    //활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }
    }

    public void setDb(String email, String id){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);       // Firebase 고유 사용자 ID 추가
        data.put("email", email);
        data.put("userId", id);     // 사용자가 입력한 ID (닉네임 등)

        db.collection("users").document(uid) // UID 기준으로 문서 생성
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG1, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG1, "Error writing document", e);
                    }
                });


    }

}