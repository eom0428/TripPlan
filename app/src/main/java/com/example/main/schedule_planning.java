package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class schedule_planning extends AppCompatActivity implements View.OnClickListener {

    private Button btnBack;
    private Button btnNext;
    private EditText location;
    private EditText month1;
    private EditText day1;
    private EditText month2;
    private EditText day2;
    private String locationName;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_planning);

        location = findViewById(R.id.editTextLocation);
        btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        btnNext = (Button)findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        month1 = findViewById(R.id.editTextM1);
        day1 = findViewById(R.id.editTextD1);
        month2 = findViewById(R.id.editTextM2);
        day2 = findViewById(R.id.editTextD2);

        Spinner spinnerPeople = findViewById(R.id.spinner);

        // 예시 데이터 (인원 수)
        String[] peopleOptions = {"1명", "2명", "3명", "4명", "5명", "6명", "7명", "8명 이상"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, peopleOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPeople.setAdapter(adapter);
        number = spinnerPeople.getSelectedItem().toString();

        // 선택 이벤트 처리
        spinnerPeople.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                number = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택 안했을 때
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view){
        Intent backIntent = new Intent(schedule_planning.this, MainActivity.class);
        Intent nextIntent = new Intent(schedule_planning.this, Plan.class);

        if(view == btnBack) {
            startActivity(backIntent);
        } else {
            // 사용자가 입력한 전체 location 텍스트를 전달
            String fullLocation = location.getText().toString().trim();

            // 버튼 클릭 시점에만 period 생성
            String mStr1 = month1.getText().toString().trim();
            String dStr1 = day1.getText().toString().trim();
            String mStr2 = month2.getText().toString().trim();
            String dStr2 = day2.getText().toString().trim();

            String period = "";
            if (!mStr1.isEmpty() && !dStr1.isEmpty() && !mStr2.isEmpty() && !dStr2.isEmpty()) {
                try {
                    int mVal1 = Integer.parseInt(mStr1);
                    int dVal1 = Integer.parseInt(dStr1);
                    int mVal2 = Integer.parseInt(mStr2);
                    int dVal2 = Integer.parseInt(dStr2);
                    period = mVal1 + "월" + dVal1 + "일 부터 " + mVal2 + "월" + dVal2 + "일 까지";
                } catch (NumberFormatException e) {
                    // 숫자가 아닌 값이 입력된 경우 빈 문자열로 처리
                    period = "";
                }
            }

            nextIntent.putExtra("location_name", fullLocation);
            nextIntent.putExtra("period", period);
            nextIntent.putExtra("people_count", number);
            startActivity(nextIntent);
        }
    }
}