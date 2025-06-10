package com.example.conversordemoeda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.conversordemoeda.data.Moeda
import com.example.conversordemoeda.interfaces.MoedaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.util.Locale

class ConverterRecursosActivity : AppCompatActivity() {

    private lateinit var comboOrigem: Spinner
    private lateinit var comboDestino: Spinner
    private lateinit var editTextValor: EditText
    private lateinit var textViewMoedaOrigem: TextView
    private lateinit var textViewMoedaDestino: TextView
    private lateinit var btnConverter: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var moedaApi: MoedaApi
    private lateinit var textViewResult: TextView
    private lateinit var arraySaldos: DoubleArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        editTextValor = findViewById(R.id.editTextValor)
        textViewMoedaOrigem = findViewById(R.id.textViewMoedaOrigem)
        textViewMoedaDestino = findViewById(R.id.textViewMoedaDestino)
        btnConverter = findViewById(R.id.btnConverter)
        progressBar = findViewById(R.id.progressBar)
        comboOrigem = findViewById(R.id.spinnerMoedaOrigem)
        comboDestino = findViewById(R.id.spinnerMoedaDestino)
        textViewResult = findViewById(R.id.textViewResult)

        val opcoesFiltro = listOf("Bitcoin", "Dólar", "Real")
        val comboAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesFiltro)
        comboOrigem.adapter = comboAdapter
        comboDestino.adapter = comboAdapter

        adicionaListenerValor()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        moedaApi = retrofit.create(MoedaApi::class.java)

        comboOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                editTextValor.setText("")
                textViewResult.text = ""
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val bundle = intent.extras
        if (bundle != null) {
            val array = bundle.getDoubleArray("saldos")
            if (array != null) {
                arraySaldos = array
            } else {
                Log.e("ConverterRecursos", "O extra 'saldos' é nulo ou não é um DoubleArray.")
                finish()
            }
        } else {
            Log.e("ConverterRecursos", "O bundle de extras está nulo.")
            finish()
        }
    }

    private fun obterCodigoMoeda(nomeExibicao: String): String {
        return when (nomeExibicao) {
            "Dólar" -> "USD"
            "Real" -> "BRL"
            "Bitcoin" -> "BTC"
            else -> ""
        }
    }

    private fun obterCodigoPesquisa(moedaDestino: String, moedaOrigem: String): String {
        return when {
            moedaDestino == "Bitcoin" && moedaOrigem == "Dólar" -> "BTC-USD"
            moedaDestino == "Bitcoin" && moedaOrigem == "Real" -> "BTC-BRL"
            moedaOrigem == "Dólar" -> "USD-BRL"
            moedaOrigem == "Real" -> "BRL-USD"
            else -> "BTC-BRL"
        }
    }

    fun converterMoeda(view: View) {
        lifecycleScope.launch {
            val nomeMoedaOrigem = comboOrigem.selectedItem.toString()
            val nomeMoedaDestino = comboDestino.selectedItem.toString()

            if (nomeMoedaOrigem == nomeMoedaDestino) {
                textViewResult.text = "As moedas de origem e destino devem ser diferentes."
                return@launch
            }

            showLoading()

            val stringBusca = obterCodigoPesquisa(nomeMoedaDestino, nomeMoedaOrigem)
            //System.out.println(stringBusca);
            val codigoOrigem = obterCodigoMoeda(nomeMoedaOrigem)
            val codigoDestino = obterCodigoMoeda(nomeMoedaDestino)

            val moedas = buscarDadoApi(stringBusca);
            if (moedas == null) {
                return@launch
            }

            val valorParaConverter = editTextValor.text.toString().replace(",", ".").toDoubleOrNull()
            if (valorParaConverter == null || valorParaConverter <= 0) {
                textViewResult.text = "Por favor, insira um valor válido para conversão."
                hideLoading()
                return@launch
            }

            var temSaldoSuficiente = false
            when (codigoOrigem) {
                "BRL" -> if (arraySaldos[0] >= valorParaConverter) temSaldoSuficiente = true
                "USD" -> if (arraySaldos[1] >= valorParaConverter) temSaldoSuficiente = true
                "BTC" -> if (arraySaldos[2] >= valorParaConverter) temSaldoSuficiente = true
            }

            if (!temSaldoSuficiente) {
                textViewResult.text = "Saldo insuficiente para realizar a operação."
                hideLoading()
                return@launch
            }

            var stringPesquisaApi: String;
            var bid: Double;

            if(nomeMoedaDestino.equals("Bitcoin")) {
                stringPesquisaApi = "$codigoDestino$codigoOrigem"
                bid = moedas[stringPesquisaApi]?.bid ?: 0.0
            }
            else{
                stringPesquisaApi = "$codigoOrigem$codigoDestino"
                bid = moedas[stringPesquisaApi]?.bid ?: 0.0
            }

            var valorConvertido = 0.0;

            if (bid != null && bid > 0) {
                if(nomeMoedaDestino == "Bitcoin"){
                    valorConvertido = valorParaConverter / bid;
                }
                else{
                    valorConvertido = valorParaConverter * bid;
                }
                println("Valor convertido: $valorConvertido")
            } else {
                println("Não foi possível realizar a conversão. Cotação não encontrada ou inválida.")
            }

            when (codigoOrigem) {
                "BRL" -> arraySaldos[0] -= valorParaConverter
                "USD" -> arraySaldos[1] -= valorParaConverter
                "BTC" -> arraySaldos[2] -= valorParaConverter
            }
            when (codigoDestino) {
                "BRL" -> arraySaldos[0] += valorConvertido
                "USD" -> arraySaldos[1] += valorConvertido
                "BTC" -> arraySaldos[2] += valorConvertido
            }

            val resultadoFormatado = String.format(Locale.US, "%.4f", valorConvertido)
            textViewResult.text = "Valor convertido: $resultadoFormatado $codigoDestino"
            hideLoading()
        }
    }

    fun voltar(view: View) {
        val returnIntent = Intent()
        returnIntent.putExtra("saldosAtualizados", arraySaldos)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private suspend fun buscarDadoApi(codigoPesquisa: String): Map<String, Moeda>? {
        val stringPesquisa = codigoPesquisa;

        return try {
            withContext(Dispatchers.IO) {
                moedaApi.getMoeda(stringPesquisa)
            }
        } catch (e: Exception) {
            Log.e("ConverterRecursos", "Erro ao buscar a moeda: $stringPesquisa", e)
            textViewResult.text = "Erro ao buscar a cotação."
            hideLoading()
            null
        }
    }

    private fun showLoading(){
        progressBar.visibility = View.VISIBLE
        textViewResult.visibility = View.GONE
    }

    private fun hideLoading(){
        progressBar.visibility = View.GONE
        textViewResult.visibility = View.VISIBLE
    }

    fun adicionaListenerValor(){
        editTextValor.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                editTextValor.removeTextChangedListener(this)
                val stringOriginal = editable.toString()
                val separadorDecimal = DecimalFormat().decimalFormatSymbols.decimalSeparator
                var textoLimpo = stringOriginal.replace("[^\\d$separadorDecimal]".toRegex(), "")
                val temVirgula = textoLimpo.indexOf(separadorDecimal)

                if (temVirgula >= 0) {
                    val integerPart = textoLimpo.substring(0, temVirgula)
                    var decimalPart = textoLimpo.substring(temVirgula + 1)
                    val moedaOrigem = comboOrigem.selectedItem.toString()

                    val maxDecimales = if (moedaOrigem == "Bitcoin") 8 else 2
                    if (decimalPart.length > maxDecimales) {
                        decimalPart = decimalPart.substring(0, maxDecimales)
                    }

                    val newText = "$integerPart$separadorDecimal$decimalPart"
                    editTextValor.setText(newText)
                    editTextValor.setSelection(newText.length)
                }
                editTextValor.addTextChangedListener(this)
            }
        })
    }
}