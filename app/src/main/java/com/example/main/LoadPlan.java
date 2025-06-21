package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LoadPlan extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView recyclerView;
    private PlanAdapter adapter;
    private List<String> titleList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_load_plan);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // 세로 리스트
        btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        titleList = new ArrayList<>();
        adapter = new PlanAdapter(titleList, title -> {
            Intent intent = new Intent(LoadPlan.this, DetailPlan.class);
            intent.putExtra("planTitle", title);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        getPlans();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    public void getPlans() {
        db.collection("plans")
                .whereEqualTo("useruid", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> planTitles = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String location = document.getString("location");
                            if (location != null) {
                                planTitles.add(location);
                            }
                        }

                        adapter.setItems(planTitles);
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public void onClick(View view){
        if(view == btnBack){
            Intent intentGoHome = new Intent(LoadPlan.this, MainActivity.class);
            startActivity(intentGoHome);
        }
    }

}