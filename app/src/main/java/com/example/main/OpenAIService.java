package com.example.main;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Body;

public interface OpenAIService {
    @POST("v1/chat/completions")
    Call<ChatResponse> getChatCompletion(@Body ChatRequest request, @Header("Authorization") String authHeader);

}
