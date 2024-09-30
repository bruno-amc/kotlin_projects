package com.example.checklistworkshopappkotlin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GerenciarSecoesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sectionListLayout: LinearLayout
    private lateinit var editTextNewSection: EditText
    private lateinit var buttonAddSection: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_secoes)

        // Inicializar o DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Referências aos elementos da interface
        sectionListLayout = findViewById(R.id.section_list)
        editTextNewSection = findViewById(R.id.editText_new_section)
        buttonAddSection = findViewById(R.id.button_add_section)

        // Carregar seções salvas
        loadSections()

        // Ação para adicionar nova seção
        buttonAddSection.setOnClickListener {
            val sectionName = editTextNewSection.text.toString().trim()
            if (sectionName.isNotEmpty()) {
                // Adicionar ao banco de dados
                dbHelper.addSection(sectionName)
                // Limpar o campo e recarregar a lista
                editTextNewSection.text.clear()
                loadSections()
            } else {
                Toast.makeText(this, "Por favor, insira o nome da seção", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para carregar seções do banco de dados
    private fun loadSections() {
        sectionListLayout.removeAllViews()

        val sections = dbHelper.getSections()
        for (section in sections) {
            val sectionLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

            val textViewSection = TextView(this).apply {
                text = section.name
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val buttonEdit = Button(this).apply {
                text = "Editar"
                setOnClickListener { showEditSectionDialog(section) }
            }

            val buttonDelete = Button(this).apply {
                text = "Excluir"
                setOnClickListener { showDeleteSectionDialog(section) }
            }

            sectionLayout.addView(textViewSection)
            sectionLayout.addView(buttonEdit)
            sectionLayout.addView(buttonDelete)
            sectionListLayout.addView(sectionLayout)
        }
    }

    // Função para mostrar o diálogo de edição da seção
    private fun showEditSectionDialog(section: Section) {
        val editText = EditText(this).apply { setText(section.name) }
        AlertDialog.Builder(this)
            .setTitle("Editar Seção")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    dbHelper.updateSection(section.id, newName)
                    loadSections()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função para mostrar o diálogo de confirmação para excluir a seção
    private fun showDeleteSectionDialog(section: Section) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Seção")
            .setMessage("Tem certeza que deseja excluir esta seção?")
            .setPositiveButton("Sim") { _, _ ->
                dbHelper.deleteSection(section.id)
                loadSections()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
