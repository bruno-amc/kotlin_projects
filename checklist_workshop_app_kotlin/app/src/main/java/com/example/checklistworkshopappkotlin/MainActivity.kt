package com.example.checklistworkshopappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        // Navegação para a tela de Configurações de Checklists
        val btnConfigChecklists: Button = findViewById(R.id.btn_checklist_configurar)
        btnConfigChecklists.setOnClickListener {
            val intent = Intent(this, ActivityConfigChecklists::class.java)
            startActivity(intent)
        }

        // Navegação para o Checklist de Saída
        val btnChecklistSaida: Button = findViewById(R.id.btn_preencher_checklist)
        btnChecklistSaida.setOnClickListener {
            val intent = Intent(this, ChecklistSaidaActivity::class.java)
            startActivity(intent)
        }

        // Navegação para 5W2H
        val btn5W2H: Button = findViewById(R.id.btn_analise_5w2h)
        btn5W2H.setOnClickListener {
            val intent = Intent(this, Analise5w2hActivity::class.java)
            startActivity(intent)
        }

        // Navegação para ishikawa
        val btnIshikawa: Button = findViewById(R.id.btn_analise_ishikawa)
        btnIshikawa.setOnClickListener {
            val intent = Intent(this, DiagramaishikawaActivity::class.java)
            startActivity(intent)
        }

        // Navegação para relatório fotográfico
        val btnRelatorioFotografico: Button = findViewById(R.id.btn_relatorio_fotografico)
        btnRelatorioFotografico.setOnClickListener {
            val intent = Intent(this, RelatorioFotograficoActivity::class.java)
            startActivity(intent)
        }

    }
}
