package com.example.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Calendar;

public class RecommendLocation extends AppCompatActivity implements View.OnClickListener {

    private TextView txtGpt;
    private Button btnHome;
    private Retrofit retrofit;
    private FirebaseFirestore db;
    private String preference = "";
    private int month;
    private static final String OPENAI_API_KEY = ""; // 보안처리 필요
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "SeenPlacesPrefs";
    private static final String KEY_SEEN_PLACES = "seen_places";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recommend_location);

        txtGpt = findViewById(R.id.txtGpt2);
        btnHome = findViewById(R.id.btnHome2);
        btnHome.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        prefs.edit().clear().apply(); //초기화

        month = Calendar.getInstance().get(Calendar.MONTH) + 1; // 0부터 시작하므로 +1

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        getDb();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchGPTResponse(String preference, Set<String> excludedPlaces) {
        OpenAIService service = retrofit.create(OpenAIService.class);

        // excludedPlaces를 문자열로 변환 (쉼표+공백 구분)
        String excludedPlacesStr = String.join(", ", excludedPlaces);

        String prompt = "아래 조건에 맞게 국내 여행지를 하나 추천해줘.\n"
                + "\n[조건]\n"
                + "1. 사용자의 취향: " + preference + "\n"
                + "2. 추천 기준:\n"
                + "- 사용자의 취향을 최우선으로 고려할 것\n"
                + "- 현재 월(" + month + "월)의 계절과 날씨에 맞는 쾌적한 지역일 것\n"
                + "- 너무 덥거나 추운 지역은 피할 것\n"
                + "- 사람들이 자주 찾는 인기 지역 중에서 고를 것\n"
                + "- 이미 추천된 지역은 제외할 것: " + excludedPlacesStr + "\n"
                + "- 만약 제외할 지역 때문에 추천할 곳이 없다면 '추천할 지역이 없습니다'라고 답해줘.\n"
                + "\n[응답 형식]\n"
                + "- 추천 지역 이름을 첫 줄에 단독으로 써줘\n"
                + "- 두 번째 줄부터 추천 이유를 간단히 설명해줘\n";

        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("system", "너는 여행 일정 계획가야. 사용자의 취향에 맞는 한국 여행지를 추천해줘."));
        messages.add(new ChatRequest.Message("user", prompt));
        ChatRequest request = new ChatRequest("gpt-4-turbo", messages, 3000);

        Call<ChatResponse> call = service.getChatCompletion(request, "Bearer " + OPENAI_API_KEY);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String gptReply = response.body().getChoices().get(0).getMessage().getContent();
                    runOnUiThread(() -> {
                        txtGpt.setText(gptReply);

                        // 추천 지역명(첫 줄 첫 단어) 추출 후 저장
                        String newPlace = extractFirstWordFromFirstLine(gptReply);
                        if (newPlace != null && !newPlace.isEmpty() && !excludedPlaces.contains(newPlace) && !newPlace.equals("추천할")) {
                            savePlaceToPrefs(newPlace);
                        }
                    });
                } else {
                    runOnUiThread(() -> txtGpt.setText("응답 실패: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                runOnUiThread(() -> txtGpt.setText("에러: " + t.getMessage()));
            }
        });
    }

    private String extractFirstWordFromFirstLine(String text) {
        if (text == null || text.isEmpty()) return null;

        String firstLine;
        if (text.contains("\n")) {
            firstLine = text.substring(0, text.indexOf("\n")).trim();
        } else {
            firstLine = text.trim();
        }

        if (firstLine.contains(" ")) {
            return firstLine.substring(0, firstLine.indexOf(" "));
        } else {
            return firstLine;
        }
    }

    private void savePlaceToPrefs(String place) {
        Set<String> seenPlaces = prefs.getStringSet(KEY_SEEN_PLACES, new HashSet<>());
        // SharedPreferences 내 StringSet는 mutable하지 않아서 복사본 만들기
        Set<String> newSet = new HashSet<>(seenPlaces);
        newSet.add(place);

        prefs.edit().putStringSet(KEY_SEEN_PLACES, newSet).apply();

        Toast.makeText(this, place + " 지역 저장 완료", Toast.LENGTH_SHORT).show();
    }

    private Set<String> getSeenPlacesFromPrefs() {
        return prefs.getStringSet(KEY_SEEN_PLACES, new HashSet<>());
    }

    public void getDb() {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user != null) {
            db.collection("preference")
                    .whereEqualTo("uid", user)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String pre1 = doc.getString("pre1");
                            String pre2 = doc.getString("pre2");

                            preference = "첫 번째 취향: " + pre1 + "\n두 번째 취향: " + pre2;
                            Toast.makeText(RecommendLocation.this, "취향 가져오기 성공", Toast.LENGTH_SHORT).show();

                            // 취향 가져왔으니 GPT 호출 (여기서 seenPlaces도 같이 넘김)
                            fetchGPTResponse(preference, getSeenPlacesFromPrefs());
                        } else {
                            Toast.makeText(RecommendLocation.this, "취향 가져오기 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void onClick(View view) {
        if (view == btnHome) {
            Intent intentHome = new Intent(RecommendLocation.this, MainActivity.class);
            startActivity(intentHome);
        }
    }
}
