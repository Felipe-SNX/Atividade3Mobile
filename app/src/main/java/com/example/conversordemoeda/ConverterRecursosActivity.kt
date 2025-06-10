package com.example.conversordemoeda

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Spinner
import android.widget.TextView
import com.example.conversordemoeda.data.Moeda
import com.example.conversordemoeda.interfaces.MoedaApi
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

    }

    fun converterMoeda(view: View){

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

        /*val moeda: Moeda = buscarDadoApi();
    }

    fun buscarDadoApi(): Moeda{

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

                    if (decimalPart.length > 2) {
                        decimalPart = decimalPart.substring(0, 2);
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