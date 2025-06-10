package com.example.conversordemoeda.data

import java.time.LocalDate

data class Moeda(
    val code: String,
    val codein: String,
    val name: String,
    val high: Double,
    val low: Double,
    val varBid: Double,
    val pctChange: Double,
    val bid: Double,
    val ask: Double,
    val timestamp: Long,
    val create_date: String
)
