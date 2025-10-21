package com.karimhosny.connection.http.requests;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.crypto.dto.FileMetadata;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class FileRequest {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public FileRequest() {
    }

    public void upload(InputStream fileStream, FileMetadata metadata, String token) throws IOException {
        // Convert metadata to JSON
        String metadataJson = mapper.writeValueAsString(metadata); // implement this method inside FileMetadata

        // Create request bodies
        RequestBody fileBody = new InputStreamRequestBody(MediaType.parse("application/octet-stream"), fileStream);
        RequestBody metadataBody = RequestBody.create(metadataJson, MediaType.parse("application/json"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "" + metadata.getFileId(), fileBody)
                .addFormDataPart("metadata", null, metadataBody)
                .build();

        Request request = new Request.Builder()
                .url("http:/localhost:8080" + "/api/file/upload")
                .header("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed: " + response);
            }
            System.out.println("✅ Uploaded " + metadata.getFileId());
        }
    }

    /**
     * Custom RequestBody to stream file data without loading all into memory
     */
    static class InputStreamRequestBody extends RequestBody {

        private final MediaType contentType;
        private final InputStream inputStream;

        public InputStreamRequestBody(MediaType contentType, InputStream inputStream) {
            this.contentType = contentType;
            this.inputStream = inputStream;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            try (inputStream) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }
            }
        }
    }

    public Response download(int fileId, String token) throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/file/download/" + fileId)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        Response response = client.newCall(request).execute(); // don't auto-close yet

        if (!response.isSuccessful()) {
            response.close();
            throw new IOException("Download failed: " + response);
        }

        System.out.println("✅ Downloaded " + fileId);
        System.out.println("Response: " + response);
        System.out.println("Response content type: " + response.body().contentType());
        System.out.println("Response length: " + response.body().contentLength());

        return response;
    }

}
