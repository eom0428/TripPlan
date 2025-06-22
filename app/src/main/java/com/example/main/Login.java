package com.example.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

public class Login extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button Login;
    private EditText email0;
    private EditText password0;
    private TextView gojoin;
    private String userId,email;
    private static final String TAG = "Login";
    private static final String TAG1 = "Db";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //firebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Login = (Button)findViewById(R.id.LoginBtn);
        Login.setOnClickListener(this);
        email0 = (EditText)findViewById(R.id.editTextEmailLo);
        password0= (EditText)findViewById(R.id.editTextPasswordLo);
        gojoin = (TextView)findViewById(R.id.textView2);
        gojoin.setOnClickListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public void onClick(View view){
        if(view == Login){
            String password;
            email = email0.getText().toString();
            password = password0.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                getDb();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        }else{
            Intent intent = new Intent(Login.this, Join.class);
            startActivity(intent);
        }
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {

            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "로그인 성공! 사용자: " + userId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "로그인 실패...", Toast.LENGTH_SHORT).show();
        }
    }

    public void getDb() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG1, "DocumentSnapshot data: " + document.getData());

                        userId = document.getString("userId");
                        Log.d(TAG1, "유저ID: " + userId);

                        // 로그인 상태 저장
                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userId", userId);
                        editor.apply();

                        updateUI(FirebaseAuth.getInstance().getCurrentUser());
                    } else {
                        Log.d(TAG1, "No such document");
                        Toast.makeText(Login.this, "회원 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(Login.this, "회원 정보 조회 실패", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

}