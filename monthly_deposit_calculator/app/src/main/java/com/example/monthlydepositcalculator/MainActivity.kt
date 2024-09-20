package com.example.monthlydepositcalculator

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import kotlin.math.pow
import kotlin.math.ln

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val btnAjuda = findViewById<Button>(R.id.buttonEquacao)



        // Referências aos campos de entrada
        val campoNumeroMeses = findViewById<EditText>(R.id.editTextNumeroMeses)
        val campoJTaxaDeJuros = findViewById<EditText>(R.id.editTextJTaxaDeJuros)
        val campoDepositosMensais = findViewById<EditText>(R.id.editTextDepositosMensais)
        val campoValorFinal = findViewById<EditText>(R.id.editTextSn)
        val btnCalcular = findViewById<Button>(R.id.buttonCalcular)
        val btnLimparCampos = findViewById<Button>(R.id.buttonLimpar)

        btnAjuda.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Equação Usada")
            builder.setMessage("Sn = p * (1 + j) * (((1 + j)^n - 1) / j)\n\n" +
                    "Onde:\n" +
                    "n = Número de Meses\n" +
                    "j = Taxa de Juros Mensal\n" +
                    "p = Valor do Depósito Regular\n" +
                    "Sn = Valor Final Obtido")
            builder.setPositiveButton("OK", null)
            builder.show()
        }

        btnCalcular.setOnClickListener {
            val n = campoNumeroMeses.text.toString().toDoubleOrNull()
            val j = campoJTaxaDeJuros.text.toString().toDoubleOrNull()?.div(100)
            val p = campoDepositosMensais.text.toString().toDoubleOrNull()
            val Sn = campoValorFinal.text.toString().toDoubleOrNull()

            // Identifica o campo que está vazio
            when {
                n == null -> {
                    if (j != null && p != null && Sn != null) {
                        // Cálculo para encontrar n
                        val resultadoN = calcularN(j, p, Sn)
                        campoNumeroMeses.setText(String.format("%.6f", resultadoN))
                    } else {
                        mostrarErro()
                    }
                }
                j == null -> {
                    if (n != null && p != null && Sn != null) {
                        // Cálculo para encontrar j
                        val resultadoJ = calcularJ(n, p, Sn)
                        campoJTaxaDeJuros.setText(String.format("%.2f", resultadoJ * 100))
                    } else {
                        mostrarErro()
                    }
                }
                p == null -> {
                    if (n != null && j != null && Sn != null) {
                        // Cálculo para encontrar p
                        val resultadoP = calcularP(n, j, Sn)
                        campoDepositosMensais.setText(String.format("%.2f", resultadoP))
                    } else {
                        mostrarErro()
                    }
                }
                Sn == null -> {
                    if (n != null && j != null && p != null) {
                        // Cálculo para encontrar Sn
                        val resultadoSn = calcularSn(n, j, p)
                        campoValorFinal.setText(String.format("%.2f", resultadoSn))
                    } else {
                        mostrarErro()
                    }
                }
            }
        }



        btnLimparCampos.setOnClickListener {
            campoNumeroMeses.setText("")
            campoDepositosMensais.setText("")
            campoJTaxaDeJuros.setText("")
            campoValorFinal.setText("")
            // Limpa o foco dos campos, ou seja, remove o cursor do campo
            campoNumeroMeses.clearFocus()
            campoDepositosMensais.clearFocus()
            campoJTaxaDeJuros.clearFocus()
            campoValorFinal.clearFocus()

            Toast.makeText(this, "Campos limpos", Toast.LENGTH_SHORT).show()
        }
    }

    // Funções para os cálculos
    private fun calcularSn(n: Double, j: Double, p: Double): Double {
        return (1 + j) * (((1 + j).pow(n) - 1) / j) * p
    }

    private fun calcularN(j: Double, p: Double, Sn: Double): Double {
        return (ln(1+((Sn*j)/(p*(1+j)))) / ln(1+j))

    }

    private fun calcularJ(n: Double, p: Double, Sn: Double): Double {
        val tolerancia = 1e-6
        var j = 0.05 // Estimativa inicial
        var diferenca: Double

        do {
            val f = p * (1 + j) * (((1 + j).pow(n) - 1) / j) - Sn
            val fDerivada = p * (((1 + j).pow(n) - 1) / j) + p * (1 + j) * (n * (1 + j).pow(n - 1)) / j - p * (1 + j) * (((1 + j).pow(n) - 1) / j.pow(2))

            val novoJ = j - f / fDerivada
            diferenca = Math.abs(novoJ - j)
            j = novoJ
        } while (diferenca > tolerancia)

        return j
    }

    private fun calcularP(n: Double, j: Double, Sn: Double): Double {
        return Sn / ((1 + j) * (((1 + j).pow(n) - 1) / j))
    }

    private fun mostrarErro() {
        Toast.makeText(this, "Preencha três campos para calcular o valor faltante", Toast.LENGTH_SHORT).show()
    }
}
