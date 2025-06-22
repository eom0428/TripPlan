package com.example.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Plan extends AppCompatActivity implements View.OnClickListener{

    private String number;
    private String locationPeriod;
    private String period;
    private String preference;
    private TextView txtGpt;
    private  String parsedGpt;
    private String locationName;
    private List<ScheduleItem> scheduleItems;
    private static final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;
    private Retrofit retrofit;
    private Button main;
    private Button save;
    private FirebaseFirestore db;
    private static final String TAG = "PlanActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan);

        txtGpt = findViewById(R.id.txtGpt);
        txtGpt.setMovementMethod(new ScrollingMovementMethod());
        main = (Button)findViewById(R.id.btnMain);
        main.setOnClickListener(this);
        save = (Button)findViewById(R.id.btnSave);
        save.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();

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

        Intent intent = getIntent();
        locationName = intent.getStringExtra("location_name");
        period = intent.getStringExtra("period");
        number = intent.getStringExtra("people_count");

        fetchGPTResponse(locationName, period, number); // retrofit 초기화 후에 호출

        main.setEnabled(false);
        main.setBackgroundColor(Color.GRAY);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    // 클래스 멤버 변수로 정의된 retrofit 사용
    private void fetchGPTResponse(String location, String period, String people) {
        OpenAIService service = retrofit.create(OpenAIService.class);
        getDb();

        String prompt = "아래 조건에 맞춰 현실적으로 실행 가능한 여행 일정을 작성해줘:\n"
                + "1. 여행지: " + location + "\n"
                + "2. 여행 기간: " + period + "\n"
                + "3. 여행 인원: " + people + "명\n"
                + "4. 사용자의 취향: " + preference + "\n\n"
                + "요구사항:\n"
                + "- 사용자의 취향을 최우선으로 고려해서 동선이나 할 일을 계획해줘.\n"
                + "- 일정은 하루 단위로 나눠줘.\n"
                + "- 각 장소 간 이동은 대중교통(지하철, 버스 등)을 알려줘. 택시 사용 시 예상 요금도 포함해줘.\n"
                + "- 각 날마다 추천할 만한 맛집 1~2곳 포함해줘. 반드시 실제 존재하는 가게만 사용해.\n"
                + "- 일정은 현실적으로 가능해야 하며, 과도한 이동은 피해야 해.\n"
                + "- 불필요한 말은 생략하고, 바로 일정만 깔끔하게 작성해줘.";

        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("system", "너는 여행 일정 계획가야. 현실적으로 수행 가능 한 여행계획을 사용자에게 만들어줘야해."));
        messages.add(new ChatRequest.Message("user", prompt));
        ChatRequest request = new ChatRequest("gpt-4-turbo", messages,3000);

        Call<ChatResponse> call = service.getChatCompletion(request, "Bearer " + OPENAI_API_KEY);

        // call 비동기 실행
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String gptReply = response.body().getChoices().get(0).getMessage().getContent();
                    runOnUiThread(() -> {
                        txtGpt.setText(gptReply);

                        // 여기서 바로 2차 요약 요청 실행
                        fetchParsedSchedule(gptReply);
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

    private void fetchParsedSchedule(String originalReply) {
        OpenAIService service = retrofit.create(OpenAIService.class);

        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("assistant", originalReply));
        messages.add(new ChatRequest.Message("user",
                "위 여행 일정을 바탕으로, 각 날짜별로 시간과 장소, 여행지역을 다음과 같은 JSON 배열 형식으로 정리해줘:\n\n" +
                        "[\n" +
                        "  { \"day\": 1, \"time\": \"09:00\", \"place\": \"서울역\", \"region\": \"location\" },\n" +
                        "  { \"day\": 1, \"time\": \"11:00\", \"place\": \"경복궁\", \"region\": \"location\" },\n" +
                        "  { \"day\": 2, \"time\": \"10:00\", \"place\": \"인천차이나타운\", \"region\": \"location\" }\n" +
                        "]\n\n" +
                        "설명 없이 JSON 데이터만 응답해줘."));

        ChatRequest request = new ChatRequest("gpt-3.5-turbo", messages,2048);

        Call<ChatResponse> call = service.getChatCompletion(request, "Bearer " + OPENAI_API_KEY);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parsedGpt = response.body().getChoices().get(0).getMessage().getContent();
                    Toast.makeText(Plan.this, "Json 데이터 응답 완료" , Toast.LENGTH_SHORT).show();
                    // 응답 받자마자 JSON 파싱 수행
                    parseScheduleJson(parsedGpt);
                } else {
                    Toast.makeText(Plan.this, "요약실패" , Toast.LENGTH_SHORT).show();
                    Log.e("GPT", "요약 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e("GPT", "에러: " + t.getMessage());
                parsedGpt = "에러: " + t.getMessage();
            }
        });
    }


    // 응답 JSON 파싱
    private void parseScheduleJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ScheduleItem>>() {}.getType();
        scheduleItems = gson.fromJson(json, listType);

        for (ScheduleItem item : scheduleItems) {
            String log = item.getDay() + "일차 - " + item.getTime() + " - " + item.getPlace() + " (" + item.getRegion() + ")";
            Log.d("일정", log);
            Toast.makeText(Plan.this, "일정 불러오기 성공", Toast.LENGTH_SHORT).show();
        }
        main.setEnabled(true);
        main.setBackgroundColor(Color.parseColor("#7C6DEA"));
    }


    @Override
    public void onClick(View view){
        Intent mainIntent = new Intent(Plan.this, MapsActivity.class);
        mainIntent.putExtra("schedule_list", (Serializable) scheduleItems); // 여기서 list를 넘김

        String txt = txtGpt.getText().toString();
        if(view == main)
            startActivity(mainIntent);
        else if(view == save){
            setDb(txt);
        }

    }

    public void setDb(String txtGpt){
        locationPeriod = locationName + period + number;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> plan = new HashMap<>();
        plan.put("useruid", uid);
        plan.put("location",locationPeriod);
        plan.put("gptText", txtGpt);

        db.collection("plans").document(locationPeriod)
                .set(plan)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(Plan.this, "성공적으로 저장되었습니다." , Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Plan.this, "실패!!!!!!!!" , Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void getDb(){
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user != null) {
            db.collection("preference").document(user)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String pre1 = documentSnapshot.getString("preference1");
                            String pre2 = documentSnapshot.getString("preference2");

                            if (pre1 != null && pre2 != null) {
                                preference = "첫 번째 취향: " + pre1 + "\n두 번째 취향: " + pre2;
                                Toast.makeText(Plan.this, "취향 가져오기 성공", Toast.LENGTH_SHORT).show();
                            } else {
                                preference = "취향 정보 없음";
                                Toast.makeText(Plan.this, "등록된 취향이 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            preference = "취향 정보 없음";
                            Toast.makeText(Plan.this, "취향 데이터가 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        preference = "취향 정보 없음";
                        Toast.makeText(Plan.this, "취향 가져오기 실패", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting preference", e);
                    });
        }
    }

}