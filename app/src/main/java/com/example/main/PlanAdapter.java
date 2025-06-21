package com.example.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    private List<String> titles;
    private OnItemClickListener listener;

    // 1. 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(String title);
    }

    // 2. 생성자 - 클릭 리스너를 외부에서 주입받음
    public PlanAdapter(List<String> items, OnItemClickListener listener) {
        this.titles = new ArrayList<>(items);
        this.listener = listener;
    }

    // 3. 아이템 새로 갱신할 때 사용
    public void setItems(List<String> items) {
        titles.clear();
        titles.addAll(items);
        notifyDataSetChanged();
    }

    // 4. ViewHolder 생성 (item_post.xml을 inflate)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    // 5. 데이터를 뷰에 바인딩
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = titles.get(position);
        holder.textView.setText(title);

        // 아이템 클릭 이벤트 연결
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    // 6. ViewHolder 클래스 (한 항목에 대한 뷰 참조)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textViewTitle);
        }
    }
}
