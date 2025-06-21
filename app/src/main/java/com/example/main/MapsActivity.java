package com.example.main;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.main.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Button btnHome;
    private List<ScheduleItem> scheduleItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnHome = (Button)findViewById(R.id.btnHome3);
        btnHome.setOnClickListener(this);

        //이 타이밍에 받아야 마커 표시가 가능함
        Intent intent = getIntent();
        scheduleItemList = (List<ScheduleItem>) intent.getSerializableExtra("schedule_list");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /*private void showCustomDialog(String locationName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(locationName);
        builder.setMessage("무엇을 하시겠습니까?");
        builder.setPositiveButton("추가", (dialog, which) -> {
            Toast.makeText(getApplicationContext(), "장소가 추가되었습니다", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("일정 등록", (dialog, which) -> {
            Toast.makeText(getApplicationContext(), "일정 등록 화면으로 이동", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MapsActivity.this, schedule_planning.class);
            intent.putExtra("location_name", locationName);  // 선택한 지역 이름 전달
            startActivity(intent);

            // Intent로 화면 전환 가능
        });
        builder.show();
    }*/

    /*private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String addressText = "주소를 가져올 수 없습니다";

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1 // 최대 결과 개수
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // 시/도 + 시/군/구 조합
                if(address.getLocality() != null)
                    addressText = address.getAdminArea() + " " + address.getLocality();
                else
                    addressText = address.getAdminArea();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressText;
    }*/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double latitude = 37.5665;
        double longitude = 126.9780;
        LatLng korea = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 7.5f));

        // 1. 지도 클릭 시 동작은 그대로 유지
        /*mMap.setOnMapClickListener(latLng -> {
            String locationName = getAddressFromLatLng(latLng);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("선택한 위치"));
            showCustomDialog(locationName);
        });

        if (scheduleItemList == null || scheduleItemList.isEmpty()) {
            Log.w("MapsActivity", "일정 리스트가 비어 있음");
            return;
        }*/

        // 2. 만약 일정 데이터가 있다면 지도에 마커 및 선 표시
        if (scheduleItemList != null && !scheduleItemList.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            PolylineOptions polylineOptions = new PolylineOptions();

            Map<Integer, PolylineOptions> polylineMap = new HashMap<>();

            int[] colors = {
                    Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA,
                    Color.CYAN, Color.YELLOW, Color.BLACK
            };

            for (ScheduleItem item : scheduleItemList) {
                try {
                    String region = item.getRegion() != null ? item.getRegion().trim() : "";
                    String place = item.getPlace() != null ? item.getPlace().trim() : "";
                    String fullPlaceName = "대한민국 " + region + " " + place;
                    List<Address> addresses = geocoder.getFromLocationName(fullPlaceName, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        // 마커 추가: 일차 정보 포함
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(item.getDay() + "일차: " +fullPlaceName)
                                .snippet(item.getTime() + " 방문 예정")); // 마커 클릭 시 보여질 설명

                        // PolylineOptions: 일차별로 관리
                        int day = item.getDay(); // 예: 1, 2, 3...
                        PolylineOptions dayPolyline = polylineMap.get(day);
                        if (dayPolyline == null) {
                            dayPolyline = new PolylineOptions();
                            dayPolyline.color(colors[(day - 1) % colors.length]); // 색상 반복
                            polylineMap.put(day, dayPolyline);
                        }
                        dayPolyline.add(latLng);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //선연결
            for (PolylineOptions options : polylineMap.values()) {
                mMap.addPolyline(options);
            }
        }

    }

    @Override
    public void onClick(View view){
        if(view == btnHome){
            Intent intentHome = new Intent(MapsActivity.this,MainActivity.class);
            startActivity(intentHome);
        }
    }

}