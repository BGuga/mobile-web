package com.example.helloandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<ImageItem> imageItemList;

    // 생성자에서 이미지 항목 리스트 입력
    public ImageAdapter(List<ImageItem> imageItemList) {
        this.imageItemList = imageItemList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 아이템 레이아웃 설정
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        // 해당 위치의 데이터 가져오기
        ImageItem imageItem = imageItemList.get(position);

        // 이미지와 제목 설정
        holder.imageView.setImageBitmap(imageItem.getImage());
        holder.textViewTitle.setText(imageItem.getTitle());

        // 삭제 버튼 클릭 이벤트 처리
        holder.btnDelete.setOnClickListener(v -> {
            imageItemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageItemList.size());
        });
    }

    @Override
    public int getItemCount() {
        return imageItemList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle;
        Button btnDelete;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
