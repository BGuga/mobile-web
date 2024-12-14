package com.example.helloandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    ImageView imgView;
    TextView textView;
//    String site_url = "https://tmdwns9912.pythonanywhere.com/";
    String site_url = "http://10.0.2.2:8000";
    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;
    CloadImage taskDownload;

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // imgView = (ImageView) findViewById(R.id.imgView);
        textView = (TextView)findViewById(R.id.textView);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void onClickDownload(View v) {
        // ...생략...
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickCheckStatus(View v) {
        new CheckStatusTask().execute(site_url + "/api_root/status/");
    }

    public void onClickViewDogs(View v) {
        // 옵션 배열 정의
        final String[] options = {"노는 사진", "자는 사진", "먹는 사진"};
        final String[] urls = {site_url + "/api_root/search/Playing", site_url + "/api_root/search/Sleeping", site_url + "/api_root/search/Eating"};

        // MaterialAlertDialogBuilder로 다이얼로그 생성
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("강아지 사진 보기");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 선택된 옵션에 따라 API 요청 실행
                String selectedUrl = urls[which];
                new CloadImage().execute(selectedUrl);
                Toast.makeText(MainActivity.this, options[which] + " 불러오는 중...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public void onClickUpload(View v) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Data");
        builder.setView(dialogView);

        final EditText editTitle = dialogView.findViewById(R.id.editTitle);
        final EditText editText = dialogView.findViewById(R.id.editText);
        final EditText editCreatedAt = dialogView.findViewById(R.id.editCreatedAt);
        final EditText editPublishedAt = dialogView.findViewById(R.id.editPublishedAt);
        final Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        // 이미지 선택 버튼 클릭 시 갤러리 열기
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String title = editTitle.getText().toString();
                String text = editText.getText().toString();
                String author = "1";
                String createdAt = editCreatedAt.getText().toString();
                String publishedAt = editPublishedAt.getText().toString();

                // 이미지가 선택되었을 경우 Base64로 인코딩
                String encodedImage = null;
                if (bmImg != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bmImg.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] imageBytes = outputStream.toByteArray();
                    encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                }

                JSONObject postData = new JSONObject();
                try {
                    postData.put("author", author);
                    postData.put("title", title);
                    postData.put("text", text);
                    postData.put("created_date", createdAt);
                    postData.put("published_date", publishedAt);
                    postData.put("image", encodedImage);

                    new PutPost().execute(postData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();
            try {
                String apiUrl = urls[0];
                String token = "4bdce80c35b857798f5e37222181105c9e016bd9";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();
                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);
                    // 배열 내 모든 이미지 다운로드
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        imageUrl = post_json.getString("image");
                        if (!imageUrl.equals("")) {
                            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                                imageUrl = site_url + imageUrl; // 서버 주소와 결합
                            }
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            bitmapList.add(imageBitmap); // 이미지 리스트에 추가
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmapList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmapList) {
            if (bitmapList != null && !bitmapList.isEmpty()) {
                List<ImageItem> imageItemList = new ArrayList<>();
                for (int i = 0; i < bitmapList.size(); i++) {
                    String title = "Post" + (i + 1); // 각 이미지에 대한 제목 설정 (예제)
                    imageItemList.add(new ImageItem(bitmapList.get(i), title));
                }
                imageAdapter = new ImageAdapter(imageItemList);
                recyclerView.setAdapter(imageAdapter);
            } else {
                Toast.makeText(MainActivity.this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PutPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String jsonString = params[0];
            String serverUrl = "http://10.0.2.2:8000/api_root/post/"; // 서버 URL로 수정

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Token 4bdce80c35b857798f5e37222181105c9e016bd9");
                conn.setDoOutput(true);

                // JSON 데이터 보내기
                OutputStream os = conn.getOutputStream();
                os.write(jsonString.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Upload Successful";
                } else {
                    return "Error: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 서버 응답 결과에 따라 UI 업데이트
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    private class CheckStatusTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String apiUrl = urls[0];
            String token = "4bdce80c35b857798f5e37222181105c9e016bd9";
            try {
                // API 호출
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 응답 읽기
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    // JSON 파싱
                    JSONObject responseJson = new JSONObject(result.toString());
                    return responseJson.getString("text");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String text) {
            if (text != null) {
                if (text.contains("Eating")) {
                    Toast.makeText(MainActivity.this, "강아지가 식사 중입니다.", Toast.LENGTH_SHORT).show();
                } else if (text.contains("Sleeping")) {
                    Toast.makeText(MainActivity.this, "강아지가 수면 중입니다.", Toast.LENGTH_SHORT).show();
                } else if (text.contains("Playing")) {
                    Toast.makeText(MainActivity.this, "강아지가 놀고 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "강아지 상태를 알 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "API 요청에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

