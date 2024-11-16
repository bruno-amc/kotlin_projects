package com.example.checklistworkshopappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityConfigChecklists : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_checklist)

        // Navegação para Gerenciar Seções
        val btnManageSections: Button = findViewById(R.id.btn_manage_sections)
        btnManageSections.setOnClickListener {
            val intent = Intent(this, GerenciarSecoesActivity::class.java)
            startActivity(intent)
        }

        // Navegação para Gerenciar Itens
        val btnManageItems: Button = findViewById(R.id.btn_manage_items)
        btnManageItems.setOnClickListener {
            val intent = Intent(this, GerenciarItensActivity::class.java)
            startActivity(intent)
        }

        // Navegação para Dados da Empresa
        val btnCompanyData: Button = findViewById(R.id.btn_company_data)
        btnCompanyData.setOnClickListener {
            val intent = Intent(this, DadosDaEmpresaActivity::class.java)
            startActivity(intent)
        }

        // Navegação para Criar Checklist (implementar depois)
        val btnCreateChecklist: Button = findViewById(R.id.btn_create_checklist)
        btnCreateChecklist.setOnClickListener {
            val intent = Intent(this, CriarChecklistActivity::class.java)
            startActivity(intent)
        }
    }
}
