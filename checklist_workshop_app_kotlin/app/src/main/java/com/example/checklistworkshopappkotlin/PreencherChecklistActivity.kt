package com.example.checklistworkshopappkotlin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class PreencherChecklistActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerChecklists: Spinner
    private lateinit var layoutChecklist: LinearLayout
    private lateinit var buttonLimparCampos: Button
    private lateinit var buttonGerarPDF: Button

    private var checklistsSalvos = mutableListOf<String>()
    private var itensPorSecao = mutableMapOf<String, MutableList<String>>() // Para armazenar os itens de cada seção

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preencher_checklist)

        dbHelper = DatabaseHelper(this)

        // Inicializa os componentes
        spinnerChecklists = findViewById(R.id.spinner_checklists)
        layoutChecklist = findViewById(R.id.layout_checklist)
        buttonLimparCampos = findViewById(R.id.button_limpar_campos)
        buttonGerarPDF = findViewById(R.id.button_gerar_pdf)

        // Carrega os checklists salvos no Spinner
        carregarChecklistsSalvos()

        // Ação para selecionar um checklist salvo
        spinnerChecklists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedChecklist = checklistsSalvos[position]
                if (selectedChecklist != "Selecione um Checklist") {
                    carregarChecklistParaPreenchimento(selectedChecklist)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Ação para limpar os campos
        buttonLimparCampos.setOnClickListener {
            limparCampos()
        }

        // Ação para gerar o PDF
        buttonGerarPDF.setOnClickListener {
            gerarPDF()
        }
    }

    private fun carregarChecklistsSalvos() {
        checklistsSalvos = dbHelper.getChecklists().map { it.name }.toMutableList()
        checklistsSalvos.add(0, "Selecione um Checklist")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, checklistsSalvos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChecklists.adapter = adapter
    }

    private fun carregarChecklistParaPreenchimento(checklistName: String) {
        layoutChecklist.removeAllViews()

        val checklistItens = dbHelper.getChecklistByName(checklistName)
        if (checklistItens.isEmpty()) {
            Toast.makeText(this, "Nenhum dado encontrado para o checklist selecionado.", Toast.LENGTH_SHORT).show()
            return
        }

        var lastSection: String? = null
        for (entry in checklistItens) {
            val sectionName = entry.sectionName
            val itemName = entry.itemName

            // Adiciona a seção se for nova
            if (lastSection != sectionName) {
                val sectionTextView = TextView(this).apply {
                    text = sectionName
                    textSize = 20f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                layoutChecklist.addView(sectionTextView)
                lastSection = sectionName
            }

            // Adiciona o item com RadioButtons e EditText
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val textViewItem = TextView(this).apply {
                text = itemName
                textSize = 18f
            }

            val radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.HORIZONTAL
                val radioButtonVerificado = RadioButton(this@PreencherChecklistActivity).apply { text = "Verificado" }
                val radioButtonNaoVerificado = RadioButton(this@PreencherChecklistActivity).apply { text = "Não Verificado" }
                val radioButtonNaoSeAplica = RadioButton(this@PreencherChecklistActivity).apply { text = "Não se Aplica" }
                addView(radioButtonVerificado)
                addView(radioButtonNaoVerificado)
                addView(radioButtonNaoSeAplica)
            }

            val editTextObservacao = EditText(this).apply {
                hint = "Observações"
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            itemLayout.addView(textViewItem)
            itemLayout.addView(radioGroup)
            itemLayout.addView(editTextObservacao)

            layoutChecklist.addView(itemLayout)
        }
    }

    private fun limparCampos() {
        for (i in 0 until layoutChecklist.childCount) {
            val view = layoutChecklist.getChildAt(i)
            if (view is RadioGroup) {
                view.clearCheck()
            } else if (view is EditText) {
                view.text.clear()
            }
        }
    }

    private fun gerarPDF() {
        // Lógica para gerar PDF com base nos campos preenchidos
        Toast.makeText(this, "PDF gerado", Toast.LENGTH_SHORT).show()
    }
}
