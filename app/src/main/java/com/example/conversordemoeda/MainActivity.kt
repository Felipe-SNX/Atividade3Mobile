package com.example.conversordemoeda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val saldos = mapOf(
        "BRL" to 100_000.0,
        "USD" to 50_000.0,
        "BTC" to 0.5
    )

    private fun Double.fmt(decimals: Int): String =
        String.format(Locale("pt","BR"), "%,.${decimals}f", this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvBRL       = findViewById<TextView>(R.id.tvSaldoBRL)
        val tvUSD       = findViewById<TextView>(R.id.tvSaldoUSD)
        val tvBTC       = findViewById<TextView>(R.id.tvSaldoBTC)
        val btnConverter = findViewById<Button>(R.id.btnConverter)

        tvBRL.text = "R$ ${saldos["BRL"]!!.fmt(2)}"
        tvUSD.text = "US$ ${saldos["USD"]!!.fmt(2)}"
        tvBTC.text = "BTC ${saldos["BTC"]!!.fmt(4)}"

        btnConverter.setOnClickListener {
            val valores = doubleArrayOf(
                saldos["BRL"]!!,
                saldos["USD"]!!,
                saldos["BTC"]!!
            )
            Intent(this, ConverterRecursosActivity::class.java).apply {
                putExtra("saldos", valores)
                startActivity(this)
            }
        }
    }
}