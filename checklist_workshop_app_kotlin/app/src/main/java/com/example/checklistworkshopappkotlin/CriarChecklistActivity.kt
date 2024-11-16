package com.example.checklistworkshopappkotlin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CriarChecklistActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerChecklistsSalvos: Spinner
    private lateinit var spinnerSecoes: Spinner
    private lateinit var spinnerItens: Spinner
    private lateinit var layoutItensSelecionados: LinearLayout
    private lateinit var editTextNomeChecklist: EditText
    private lateinit var buttonAddSection: Button
    private lateinit var buttonAddItem: Button
    private lateinit var buttonSaveChecklist: Button
    private lateinit var textSelecioneUmChecklist: TextView
    private lateinit var textNomeDoChecklist: TextView
    private lateinit var toggleNovoOuEditar: ToggleButton

    // Variável para armazenar os checklists salvos
    private var checklistsSalvos = mutableListOf<String>()
    private var secoesEItens = mutableMapOf<Long, MutableList<Long>>() // Map para armazenar seções e itens
    private var currentSectionId: Long? = null // Para associar itens à seção correta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_checklist)

        dbHelper = DatabaseHelper(this)

        // Inicializa os componentes
        textSelecioneUmChecklist = findViewById(R.id.text_view_selecione_um_checklist)
        textNomeDoChecklist = findViewById(R.id.text_view_nome_do_checklist)
        spinnerChecklistsSalvos = findViewById(R.id.spinner_checklists_salvos)
        spinnerSecoes = findViewById(R.id.spinner_secoes)
        spinnerItens = findViewById(R.id.spinner_itens)
        layoutItensSelecionados = findViewById(R.id.layout_itens_selecionados)
        editTextNomeChecklist = findViewById(R.id.editText_nome_checklist)
        buttonAddSection = findViewById(R.id.button_add_section)
        buttonAddItem = findViewById(R.id.button_add_item)
        buttonSaveChecklist = findViewById(R.id.button_save_checklist)
        toggleNovoOuEditar = findViewById(R.id.toggle_novo_ou_editar)



        // Verifica o estado inicial do ToggleButton ao abrir a tela
        if (toggleNovoOuEditar.isChecked) {
            editTextNomeChecklist.visibility = View.GONE
            textSelecioneUmChecklist.visibility = View.VISIBLE
            textNomeDoChecklist.visibility = View.GONE
            spinnerChecklistsSalvos.visibility = View.VISIBLE
        } else {
            editTextNomeChecklist.visibility = View.VISIBLE
            textSelecioneUmChecklist.visibility = View.GONE
            textNomeDoChecklist.visibility = View.VISIBLE
            spinnerChecklistsSalvos.visibility = View.GONE
        }





        // Lógica para o botão de alternância
        toggleNovoOuEditar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Editar Checklist

                editTextNomeChecklist.visibility = View.GONE
                textSelecioneUmChecklist.visibility = View.VISIBLE
                textNomeDoChecklist.visibility = View.GONE
                spinnerChecklistsSalvos.visibility = View.VISIBLE
                Toast.makeText(this, "Modo: Editar Checklist", Toast.LENGTH_SHORT).show()
            } else {
                // Novo Checklist
                editTextNomeChecklist.visibility = View.VISIBLE
                textSelecioneUmChecklist.visibility = View.GONE
                textNomeDoChecklist.visibility = View.VISIBLE
                spinnerChecklistsSalvos.visibility = View.GONE
                Toast.makeText(this, "Modo: Novo Checklist", Toast.LENGTH_SHORT).show()
            }
        }

        // Carrega checklists salvos
        carregarChecklistsSalvos()

        // Carrega seções e itens
        carregarSecoes()
        carregarItens()

        // Ação para selecionar um checklist salvo
        spinnerChecklistsSalvos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedChecklist = checklistsSalvos[position]
                // Verifica se o checklist selecionado é o texto padrão (vazio)
                if (selectedChecklist != "Selecione um Checklist") {
                    carregarChecklistParaEdicao(selectedChecklist)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Ação para adicionar uma seção
        buttonAddSection.setOnClickListener {
            val selectedSection = spinnerSecoes.selectedItem as String
            adicionarSeção(selectedSection)
        }

        // Ação para adicionar item
        buttonAddItem.setOnClickListener {
            val selectedItem = spinnerItens.selectedItem as String
            adicionarItem(selectedItem)
        }

        // Ação para salvar o checklist (novo ou editado)
        buttonSaveChecklist.setOnClickListener {
            val checklistName = editTextNomeChecklist.text.toString().trim()
            if (toggleNovoOuEditar.isChecked) {
                // Editar checklist existente
                salvarChecklist(checklistName, true)
            } else {
                // Criar novo checklist
                salvarChecklist(checklistName, false)
            }
        }
    }

    // Função para salvar um checklist (novo ou editado)
    private fun salvarChecklist(checklistName: String, isEditing: Boolean) {
        val finalChecklistName: String = if (isEditing) {
            // Pega o nome do checklist sendo editado
            checklistNameForEdit ?: return // Se for nulo, retorna e não faz nada
        } else {
            // Se for um novo checklist, usa o nome do campo de texto
            checklistName
        }

        if (finalChecklistName.isNotEmpty()) {
            // Se for edição, exclui as entradas antigas para reescrever
            dbHelper.deleteChecklistByName(finalChecklistName)

            // Salva as seções e itens no banco de dados
            for ((sectionName, itens) in itensPorSecao) {
                val sectionId = dbHelper.getSectionIdByName(sectionName) ?: dbHelper.addSection(sectionName)
                dbHelper.addChecklist(finalChecklistName, sectionId, -1) // Adiciona a seção
                for (itemName in itens) {
                    val itemId = dbHelper.getItemIdByName(itemName) ?: dbHelper.addItem(itemName)
                    dbHelper.addChecklist(finalChecklistName, sectionId, itemId) // Adiciona os itens
                }
            }
            Toast.makeText(this, "Checklist salvo com sucesso", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Por favor, insira o nome do checklist", Toast.LENGTH_SHORT).show()
        }
    }




    private fun carregarChecklistsSalvos() {
        checklistsSalvos = dbHelper.getChecklists().map { it.name }.toMutableList()

        // Adiciona uma opção vazia no spinner
        checklistsSalvos.add(0, "Selecione um Checklist")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, checklistsSalvos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChecklistsSalvos.adapter = adapter
    }

    private fun carregarSecoes() {
        val secoes = dbHelper.getSections().map { it.name }.toMutableList()

        // Adiciona a opção padrão no início da lista
        secoes.add(0, "Escolha uma seção")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, secoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSecoes.adapter = adapter
    }


    private fun carregarItens() {
        val itens = dbHelper.getItems().map { it.name }.toMutableList()

        // Adicionar a mensagem padrão como primeiro item
        itens.add(0, "Escolha um item")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itens)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerItens.adapter = adapter

        // Configurar o spinner para exibir a mensagem padrão por padrão
        spinnerItens.setSelection(0)
    }

    // Class-level variable to store the checklist name when editing
    private var checklistNameForEdit: String? = null

    private fun carregarChecklistParaEdicao(checklistName: String) {
        layoutItensSelecionados.removeAllViews()

        // Armazena o nome do checklist para edição
        checklistNameForEdit = checklistName

        val checklistItens = dbHelper.getChecklistByName(checklistName)
        if (checklistItens.isEmpty()) {
            Toast.makeText(this, "Nenhum dado encontrado para o checklist selecionado.", Toast.LENGTH_SHORT).show()
            return
        }

        var lastSection: String? = null
        for (entry in checklistItens) {
            val sectionName = entry.sectionName
            val itemName = entry.itemName

            // Verifica se a seção já foi adicionada, para não duplicá-la
            if (lastSection != sectionName) {
                val sectionLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val textViewSection = TextView(this).apply {
                    text = sectionName
                    textSize = 20f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val buttonRemoveSection = Button(this).apply {
                    text = "Remover"
                    setOnClickListener {
                        layoutItensSelecionados.removeView(sectionLayout)
                        itensPorSecao.remove(sectionName) // Remove os itens associados à seção
                    }
                }

                sectionLayout.addView(textViewSection)
                sectionLayout.addView(buttonRemoveSection)
                layoutItensSelecionados.addView(sectionLayout)

                // Inicializa a lista de itens para a seção
                itensPorSecao[sectionName] = mutableListOf()
                lastSection = sectionName
            }

            // Adiciona o item à seção
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textViewItem = TextView(this).apply {
                text = itemName
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val buttonRemoveItem = Button(this).apply {
                text = "Remover"
                setOnClickListener {
                    layoutItensSelecionados.removeView(itemLayout) // Remove o layout do item
                    itensPorSecao[sectionName]?.remove(itemName) // Remove o item da lista de selecionados
                }
            }

            itemLayout.addView(textViewItem)
            itemLayout.addView(buttonRemoveItem)
            layoutItensSelecionados.addView(itemLayout)

            // Adiciona o item à lista de itens da seção
            itensPorSecao[sectionName]?.add(itemName)
        }
    }



    // Mapeia cada seção para a lista de itens selecionados
    private val itensPorSecao = mutableMapOf<String, MutableList<String>>()

    private fun adicionarSeção(sectionName: String) {
        if (sectionName == "Escolha uma seção") {
            Toast.makeText(this, "Por favor, selecione uma seção válida", Toast.LENGTH_SHORT).show()
            return
        }

        val sectionId = dbHelper.getSectionIdByName(sectionName)

        if (sectionId != null) {
            currentSectionId = sectionId

            val sectionLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textViewSection = TextView(this).apply {
                text = sectionName
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val buttonRemoveSection = Button(this).apply {
                text = "Remover"
                setOnClickListener {
                    layoutItensSelecionados.removeView(sectionLayout)
                    itensPorSecao.remove(sectionName)
                }
            }

            sectionLayout.addView(textViewSection)
            sectionLayout.addView(buttonRemoveSection)
            layoutItensSelecionados.addView(sectionLayout)

            itensPorSecao[sectionName] = mutableListOf()
        } else {
            Toast.makeText(this, "Seção não encontrada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adicionarItem(itemName: String) {
        // Verifica se uma seção foi selecionada
        if (currentSectionId == null || spinnerSecoes.selectedItem == null) {
            Toast.makeText(this, "Por favor, selecione ou adicione uma seção antes de adicionar um item", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o item selecionado é a mensagem padrão "Escolha um item"
        if (itemName == "Escolha um item") {
            Toast.makeText(this, "Por favor, selecione um item válido", Toast.LENGTH_SHORT).show()
            return
        }

        val sectionName = spinnerSecoes.selectedItem as String
        val itensSelecionadosNaSecao = itensPorSecao[sectionName] ?: mutableListOf() // Aqui está a linha que você mencionou

        if (itemName !in itensSelecionadosNaSecao) {
            itensSelecionadosNaSecao.add(itemName)
            itensPorSecao[sectionName] = itensSelecionadosNaSecao

            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textViewItem = TextView(this).apply {
                text = itemName
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val buttonRemoveItem = Button(this).apply {
                text = "Remover"
                setOnClickListener {
                    layoutItensSelecionados.removeView(itemLayout) // Remove o layout do item
                    itensSelecionadosNaSecao.remove(itemName) // Remove o item da lista de selecionados
                }
            }

            itemLayout.addView(textViewItem)
            itemLayout.addView(buttonRemoveItem)
            layoutItensSelecionados.addView(itemLayout)
        } else {
            Toast.makeText(this, "Este item já foi adicionado", Toast.LENGTH_SHORT).show()
        }
    }}