package com.example.monthlydepositcalculator

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ChartHelper(private val context: Context) {

    // Método para configurar e exibir o gráfico
    fun configureChart(lineChart: LineChart, dataPoints: List<Entry>, dataPointsSemJuros: List<Entry>) {
        // Configura os dados do gráfico para os valores COM juros
        val lineDataSet = LineDataSet(dataPoints, "Progresso do Investimento (com juros)")
        lineDataSet.color = context.getColor(R.color.black)
        lineDataSet.valueTextSize = 10f
        lineDataSet.setDrawCircles(false) // mostra os círculos nos pontos
        // Usar cor sólida para o preenchimento
        lineDataSet.fillColor = ContextCompat.getColor(context, R.color.green)
        // Habilitar o preenchimento abaixo da linha
        lineDataSet.setDrawFilled(true)

        // Configura o conjunto de dados para o valor sem juros
        val lineDataSetSemJuros = LineDataSet(dataPointsSemJuros, "Sem Juros")
        lineDataSetSemJuros.color = ContextCompat.getColor(context, R.color.azulEscuro)
        lineDataSetSemJuros.valueTextSize = 10f
        lineDataSetSemJuros.setDrawCircles(false)
        lineDataSetSemJuros.setDrawFilled(true)
        lineDataSetSemJuros.fillColor = ContextCompat.getColor(context, R.color.cinzaClaro)

        // Adiciona ambos os conjuntos de dados ao gráfico
        val lineData = LineData(lineDataSet, lineDataSetSemJuros)
        lineChart.data = lineData

        // Configurações do eixo X
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.setDrawGridLines(false)

        // Configurações gerais do gráfico
        lineChart.axisRight.isEnabled = false // Desativa o eixo direito
        lineChart.description.text = "Valor ao longo do tempo"
        lineChart.invalidate() // Atualiza o gráfico


    }
}
