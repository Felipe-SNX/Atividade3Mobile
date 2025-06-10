package com.example.conversordemoeda

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
import androidx.activity.enableEdgeToEdge
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

class ConverterRecursosActivity : AppCompatActivity() {

    private lateinit var comboOrigem: Spinner;
    private lateinit var comboDestino: Spinner;
    private lateinit var editTextValor: EditText;
    private lateinit var textViewMoedaOrigem: TextView;
    private lateinit var textViewMoedaDestino: TextView;
    private lateinit var btnConverter: Button;
    private lateinit var progressBar: ProgressBar;
    private lateinit var moedaApi: MoedaApi;
    private lateinit var textViewResult: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_converter_recursos)

        editTextValor = findViewById(R.id.editTextValor);
        textViewMoedaOrigem = findViewById(R.id.textViewMoedaOrigem);
        textViewMoedaDestino = findViewById(R.id.textViewMoedaDestino);
        btnConverter = findViewById(R.id.btnConverter);
        progressBar = findViewById(R.id.progressBar);
        comboOrigem = findViewById(R.id.spinnerMoedaOrigem);
        comboDestino = findViewById(R.id.spinnerMoedaDestino);
        textViewResult = findViewById(R.id.textViewResult);

        val opcoesFiltro = listOf("Bitcoin", "Dólar", "Real");
        val comboAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesFiltro);
        comboOrigem.adapter = comboAdapter;
        comboDestino.adapter = comboAdapter;

        adicionaListenerValor();

        //Configura retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        moedaApi = retrofit.create(MoedaApi::class.java);

        comboOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               editTextValor.setText("");
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun converterMoeda(view: View){
        lifecycleScope.launch {
            showLoading()
            val moedas = buscarDadoApi();
            System.out.println(moedas);
        }

        val moedaOrigem = spinnerMoedaOrigem.selectedItem.toString()


        val moedaDestino = spinnermoedaDestino.selectedItem.toString()


        val valorParaConverter = editTextValor.text.toString().replace(",", ".").toDoubleOrNull()


        if (valorParaConverter == null || valorParaConverter <= 0) {

            Toast.makeText(this, "Por favor, insira um valor válido.", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        var temSaldoSuficiente = false


        when (moedaOrigem) {
            "BRL" -> if (saldoBRL >= valorParaConverter) temSaldoSuficiente = true
            "USD" -> if (saldoUSD >= valorParaConverter) temSaldoSuficiente = true
            "BTC" -> if (saldoBTC >= valorParaConverter) temSaldoSuficiente = true
        }


        if (!temSaldoSuficiente) {
            Toast.makeText(this, "Saldo insuficiente!", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }


        val valorConvertido = valorParaConverter * taxaCambio


        when (moedaOrigem) {
            "BRL" -> saldoBRL -= valorParaConverter
            "USD" -> saldoUSD -= valorParaConverter
            "BTC" -> saldoBTC -= valorParaConverter
        }

        when (moedaDestino) {
            "BRL" -> saldoBRL += valorConvertido
            "USD" -> saldoUSD += valorConvertido
            "BTC" -> saldoBTC += valorConvertido
        }


        val resultadoFormatado = String.format(Locale.US, "%.2f", valorConvertido)
        textViewResultado.text = "Valor convertido: $resultadoFormatado $moedaDestino"


        Toast.makeText(this, "Conversão realizada com sucesso!", Toast.LENGTH_SHORT).show()

        val moeda: Moeda = buscarDadoApi();
    }

    private suspend fun buscarDadoApi(): Map<String, Moeda>? {

        val moedaOrigem = comboOrigem.selectedItem.toString()
        val moedaDestino = comboDestino.selectedItem.toString()

        if (moedaOrigem == moedaDestino) {
            textViewResult.text = "As moedas precisam ser diferentes"
        }
        else {
            textViewResult.text = "";

            val stringPesquisa = when {
                moedaDestino == "Bitcoin" && moedaOrigem == "Dólar" -> "BTC-USD"
                moedaDestino == "Bitcoin" && moedaOrigem == "Real" -> "BTC-BRL"
                moedaOrigem == "Dólar" -> "USD-BRL"
                moedaOrigem == "Real" -> "BRL-USD"
                else -> "BTC-BRL"
            }

            try {
                return withContext(Dispatchers.IO) {
                    moedaApi.getMoeda(stringPesquisa)
                }
            }
            catch (e: Exception) {
                Log.e("ConverterRecursosActivity", "Erro ao buscar a moeda: $stringPesquisa", e)
                textViewResult.text = "Erro ao buscar a moeda inserida."
                null
            }
            finally {
                hideLoading();
            }
        }
        hideLoading()
        return null;
    }

    private fun showLoading(){
        progressBar.visibility = View.VISIBLE;
        textViewResult.visibility = View.GONE;
    }

    private fun hideLoading(){
        progressBar.visibility = View.GONE;
        textViewResult.visibility = View.VISIBLE;
    }
    }*/

    fun adicionaListenerValor(){
        //Formata o valor para duas casas decimais
        editTextValor.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                editTextValor.removeTextChangedListener(this);

                val stringOriginal = editable.toString();
                val separadorDecimal = DecimalFormat().decimalFormatSymbols.decimalSeparator;
                var textoLimpo = stringOriginal.replace(".", separadorDecimal.toString());

                val temVirgula = textoLimpo.indexOf(separadorDecimal);

                if (temVirgula >= 0) {
                    val integerPart = textoLimpo.substring(0, temVirgula);
                    var decimalPart = textoLimpo.substring(temVirgula + 1);

                    val moedaOrigem = comboOrigem.selectedItem.toString()

                    when {
                        moedaOrigem == "Bitcoin" && decimalPart.length > 4 -> {
                            decimalPart = decimalPart.substring(0, 4);
                        }
                        moedaOrigem != "Bitcoin" && decimalPart.length > 2 -> {
                            decimalPart = decimalPart.substring(0, 2);
                        }
                    }

                    val newText = "$integerPart$separadorDecimal$decimalPart";
                    editTextValor.setText(newText);
                    editTextValor.setSelection(newText.length);
                } else {
                    editTextValor.setText(textoLimpo);
                    editTextValor.setSelection(textoLimpo.length);
                }

                editTextValor.addTextChangedListener(this)
            }
        });
    }

}