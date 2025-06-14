package com.example.conversordemoeda.interfaces

import com.example.conversordemoeda.data.Moeda
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MoedaApi {
    //Requisição GET para obter uma moeda específica
    @GET("last/{moeda}")
    suspend fun getMoeda(@Path("moeda") moeda: String): Map<String, Moeda>;
}