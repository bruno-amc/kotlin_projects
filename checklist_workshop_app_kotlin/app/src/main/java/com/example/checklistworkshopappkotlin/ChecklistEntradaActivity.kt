package com.example.checklistworkshopappkotlin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class ChecklistEntradaActivity : AppCompatActivity() {

    private lateinit var layoutSections: LinearLayout
    private var currentSectionLayout: LinearLayout? = null
    private var currentSectionName: EditText? = null
    private lateinit var dbHelper: DatabaseHelper
    private var isChecklistCreated: Boolean = false  // Para checar se o checklist já existe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist_entrada)

        // Inicializa o helper do banco de dados
        dbHelper = DatabaseHelper(this)

        // Verifica se o checklist de entrada já foi criado
        isChecklistCreated = dbHelper.isChecklistCreated()  // Verifica se o checklist existe

        // Referência ao layout onde seções serão adicionadas
        layoutSections = findViewById(R.id.layout_sections)

        if (isChecklistCreated) {
            // Se o checklist já foi criado, exibir para preenchimento
            loadChecklistForFilling()
        } else {
            // Caso contrário, permitir que o usuário crie um novo checklist
            showChecklistCreationOptions()
        }
    }

    // Função para carregar o checklist salvo para preenchimento
    private fun loadChecklistForFilling() {
        val sections = dbHelper.getSections()  // Retorna as seções salvas
        for (section in sections) {
            // Exibir o nome da seção
            val sectionTextView = TextView(this).apply {
                text = section.name
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layoutSections.addView(sectionTextView)

            // Carregar os itens dessa seção
            val items = dbHelper.getItemsBySection(section.id)  // Retorna os itens da seção
            for (item in items) {
                // Exibir o nome do item
                val itemTextView = TextView(this).apply {
                    text = item.name
                }
                layoutSections.addView(itemTextView)

                // Criar o grupo de RadioButtons
                val radioGroup = RadioGroup(this).apply {
                    orientation = RadioGroup.HORIZONTAL
                }

                val radioButtonVerified = RadioButton(this).apply {
                    text = "Verificado"
                }
                val radioButtonNotVerified = RadioButton(this).apply {
                    text = "Não Verificado"
                }
                val radioButtonNotApplicable = RadioButton(this).apply {
                    text = "Não se Aplica"
                }

                radioGroup.addView(radioButtonVerified)
                radioGroup.addView(radioButtonNotVerified)
                radioGroup.addView(radioButtonNotApplicable)
                layoutSections.addView(radioGroup)

                // Observações (visualização, mas não salva)
                val editTextObservacoes = EditText(this).apply {
                    hint = "Observações"
                }
                layoutSections.addView(editTextObservacoes)
            }
        }
    }

    // Função para mostrar o layout de criação de checklist
    private fun showChecklistCreationOptions() {
        val btnAddSection = Button(this).apply {
            text = "+SEÇÃO"
            setOnClickListener {
                addNewSection()
            }
        }
        layoutSections.addView(btnAddSection)
    }

    // Função para mostrar o DatePickerDialog e atualizar o campo de data
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Atualiza o campo de data com a data selecionada
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // Função para adicionar uma nova seção e salvar no banco de dados
    private fun addNewSection() {
        if (currentSectionName != null && currentSectionName!!.text.isEmpty()) {
            Toast.makeText(this, "Por favor, nomeie a seção antes de adicionar outra.", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar um novo layout para a seção
        val newSectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // EditText para o nome da seção
        val editTextSectionName = EditText(this).apply {
            hint = "Nome da Seção"
            id = View.generateViewId()
            textSize = 25f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        newSectionLayout.addView(editTextSectionName)

        // Botão para adicionar itens à seção
        val btnAddItem = Button(this).apply {
            text = "+ITEM"
            setOnClickListener {
                // Salva a seção no banco de dados
                val sectionId = dbHelper.addSection(editTextSectionName.text.toString())
                addNewItem(newSectionLayout, editTextSectionName, sectionId)
            }
        }

        newSectionLayout.addView(btnAddItem)
        layoutSections.addView(newSectionLayout)

        currentSectionLayout = newSectionLayout
        currentSectionName = editTextSectionName
    }

    // Função para adicionar um novo item à seção e salvar no banco de dados
    private fun addNewItem(sectionLayout: LinearLayout, sectionName: EditText, sectionId: Long) {
        if (sectionName.text.isEmpty()) {
            Toast.makeText(this, "Por favor, nomeie a seção antes de adicionar itens.", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar um EditText para o item de checklist
        val newItemEditText = EditText(this).apply {
            hint = "Nome do Item de Checklist"
            id = View.generateViewId()
        }

        // Criar um grupo de RadioButtons para o status (somente visual, não salva)
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }

        val radioButtonVerified = RadioButton(this).apply {
            text = "Verificado"
        }
        val radioButtonNotVerified = RadioButton(this).apply {
            text = "Não Verificado"
        }
        val radioButtonNotApplicable = RadioButton(this).apply {
            text = "Não se Aplica"
        }

        // Adicionar os RadioButtons ao RadioGroup
        radioGroup.addView(radioButtonVerified)
        radioGroup.addView(radioButtonNotVerified)
        radioGroup.addView(radioButtonNotApplicable)

        // Adicionar o EditText e os RadioButtons ao layout da seção
        sectionLayout.addView(newItemEditText)
        sectionLayout.addView(radioGroup)

        // Salvar apenas o nome do item no banco de dados
        //dbHelper.addItem(newItemEditText.text.toString(), sectionId)
    }
}
