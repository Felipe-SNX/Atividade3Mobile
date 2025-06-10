package com.example.conversordemoeda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvBRL: TextView
    private lateinit var tvUSD: TextView
    private lateinit var tvBTC: TextView
    private lateinit var btnConverter: Button

    private var saldos: MutableMap<String, Double> = mutableMapOf(
        "BRL" to 100_000.0,
        "USD" to 50_000.0,
        "BTC" to 0.5
    )

    private fun Double.fmt(decimals: Int): String =
        String.format(Locale("pt", "BR"), "%,.${decimals}f", this)

    private val conversionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentData = result.data
                val saldosRetornados = intentData?.getDoubleArrayExtra("saldosAtualizados")

                if (saldosRetornados != null && saldosRetornados.size == 3) {
                    saldos["BRL"] = saldosRetornados[0]
                    saldos["USD"] = saldosRetornados[1]
                    saldos["BTC"] = saldosRetornados[2]
                    atualizarTela()
                }
            } else {
                Log.d("MainActivity", "Conversão cancelada ou sem retorno de dados.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBRL = findViewById(R.id.tvSaldoBRL)
        tvUSD = findViewById(R.id.tvSaldoUSD)
        tvBTC = findViewById(R.id.tvSaldoBTC)
        btnConverter = findViewById(R.id.btnConverter)

        atualizarTela()

        btnConverter.setOnClickListener {
            val valores = doubleArrayOf(
                saldos.getOrDefault("BRL", 0.0),
                saldos.getOrDefault("USD", 0.0),
                saldos.getOrDefault("BTC", 0.0)
            )

            val intentParaConverter = Intent(this, ConverterRecursosActivity::class.java).apply {
                putExtra("saldos", valores)
            }

            conversionActivityResultLauncher.launch(intentParaConverter)
        }
    }

    private fun atualizarTela() {
        tvBRL.text = "R$ ${saldos.getOrDefault("BRL", 0.0).fmt(2)}"
        tvUSD.text = "US$ ${saldos.getOrDefault("USD", 0.0).fmt(2)}"
        tvBTC.text = "BTC ${saldos.getOrDefault("BTC", 0.0).fmt(4)}"
    }
}