package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailPlan extends AppCompatActivity implements View.OnClickListener{

    private TextView detailTextView;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private Button btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_plan);

        TextView textView = findViewById(R.id.txtDetailPlan);
        textView.setMovementMethod(new ScrollingMovementMethod());

        detailTextView = findViewById(R.id.txtDetailPlan);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        btnHome = (Button)findViewById(R.id.btnGoHome);
        btnHome.setOnClickListener(this);

        String title = getIntent().getStringExtra("planTitle");

        if (title != null && user != null) {
            db.collection("plans")
                    .whereEqualTo("useruid", user.getUid())
                    .whereEqualTo("location", title)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String plan = doc.getString("gptText");

                            String detail = "제목: " + title + "\n"
                                    + "여행 계획: " + plan;
                            detailTextView.setText(detail);
                        } else {
                            detailTextView.setText("상세 정보를 찾을 수 없습니다.");
                        }
                    });
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view){
        if(view == btnHome){
            Intent intentGoBack = new Intent(DetailPlan.this, LoadPlan.class);
            startActivity(intentGoBack);
        }
    }
}