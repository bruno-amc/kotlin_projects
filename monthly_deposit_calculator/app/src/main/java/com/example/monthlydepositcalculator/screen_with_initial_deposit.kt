package com.example.monthlydepositcalculator

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry

class screen_with_initial_deposit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_with_initial_deposit)

        //Campos de edit text
        val campoCapitalInicial = findViewById<EditText>(R.id.editTextCapitalInicial)
        val campoTaxaDeJuros = findViewById<EditText>(R.id.editTextTaxaRendimentoMensal)
        val campoAporteMensal = findViewById<EditText>(R.id.editTextAporteMensal)
        val campoTotalDeMeses = findViewById<EditText>(R.id.editTextTotaldeMeses)

        //Text View do resultado final
        val campoResultadoFinal = findViewById<TextView>(R.id.textViewCampoResultadoFinal)
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        //Botão calcular
        val btnCalcularTotal = findViewById<Button>(R.id.buttonCalcularTotal)

        //Botão limpar campos
        val btnLimparCampos = findViewById<Button>(R.id.buttonLimparCampos)

        //Botão exibir a equação utilizada
        val btnExibirEquacaoUtilizada = findViewById<Button>(R.id.buttonEquacaoUtilizada)


        // Formato monetário para BR
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // Instancia o ChartHelper
        val chartHelper = ChartHelper(this)

        btnExibirEquacaoUtilizada.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Equação Usada")
            builder.setMessage("M = P(1 + r)^n + Aporte * [((1 + r)^n - 1) / r]\n\n" +
                    "Onde:\n" +
                    "P é o capital inicial\n" +
                    "r é a taxa de juros por período\n" +
                    "n é o número total de períodos\n" +
                    "Aporte é o valor dos aportes mensais")
            builder.setPositiveButton("OK", null)
            builder.show()
        }

        btnCalcularTotal.setOnClickListener {
            val capInicial = campoCapitalInicial.text.toString().toDoubleOrNull()
            val taxaJuros = campoTaxaDeJuros.text.toString().toDoubleOrNull()?.div(100)
            val aporteMensal = campoAporteMensal.text.toString().toDoubleOrNull()
            val totMeses = campoTotalDeMeses.text.toString().toDoubleOrNull()

            if (capInicial != null && taxaJuros != null && aporteMensal != null && totMeses != null){
                val jurosMensais = (1 + taxaJuros).pow(totMeses) // (1 + taxaJuros)^totMeses
                val resultadoFinal = capInicial * jurosMensais + aporteMensal * ((jurosMensais - 1) / taxaJuros)
                // Formatar o resultado com divisores de milhar e moeda
                val resultadoFormatado = formatador.format(resultadoFinal)
                campoResultadoFinal.text = resultadoFormatado


                // Configura o gráfico
                val dataPoints = mutableListOf<Entry>()
                val dataPointsSemJuros = mutableListOf<Entry>()

                for (mes in 1..totMeses.toInt()) {
                    // Com Juros
                    val jurosMes = (1 + taxaJuros).pow(mes)
                    val valorAcumulado = capInicial * jurosMes + aporteMensal * ((jurosMes - 1) / taxaJuros)
                    dataPoints.add(Entry(mes.toFloat(), valorAcumulado.toFloat()))

                    // Sem juros
                    val valorSemJuros = capInicial + (aporteMensal * mes)
                    dataPointsSemJuros.add(Entry(mes.toFloat(), valorSemJuros.toFloat()))
                }
                chartHelper.configureChart(lineChart, dataPoints, dataPointsSemJuros)
            } else {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }




        btnLimparCampos.setOnClickListener {
            //limpa os campos
            campoCapitalInicial.setText("")
            campoTaxaDeJuros.setText("")
            campoAporteMensal.setText("")
            campoTotalDeMeses.setText("")
            campoResultadoFinal.setText("")
            // Limpa o foco dos campos, ou seja, remove o cursor do campo
            campoCapitalInicial.clearFocus()
            campoTaxaDeJuros.clearFocus()
            campoAporteMensal.clearFocus()
            campoTotalDeMeses.clearFocus()

            // Limpa o gráfico
            lineChart.clear()
            lineChart.invalidate() // Atualiza a visualização do gráfico

            Toast.makeText(this, "Campos limpos", Toast.LENGTH_SHORT).show()
        }

    }


}