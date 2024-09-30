package com.example.checklistworkshopappkotlin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GerenciarItensActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var itemListLayout: LinearLayout
    private lateinit var editTextNewItem: EditText
    private lateinit var buttonAddItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_itens)

        // Inicializar o DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Referências aos elementos da interface
        itemListLayout = findViewById(R.id.item_list)
        editTextNewItem = findViewById(R.id.editText_new_item)
        buttonAddItem = findViewById(R.id.button_add_item)

        // Carregar items salvos
        loadItems()

        // Ação para adicionar novo item
        buttonAddItem.setOnClickListener {
            val itemName = editTextNewItem.text.toString().trim()
            if (itemName.isNotEmpty()) {
                // Adicionar ao banco de dados
                dbHelper.addItem(itemName)
                // Limpar o campo e recarregar a lista
                editTextNewItem.text.clear()
                loadItems()
            } else {
                Toast.makeText(this, "Por favor, insira o item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para carregar seções do banco de dados
    private fun loadItems() {
        itemListLayout.removeAllViews()

        val items = dbHelper.getItems()
        for (item in items) {
            val sectionLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

            val textViewSection = TextView(this).apply {
                text = item.name
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val buttonEdit = Button(this).apply {
                text = "Editar"
                setOnClickListener { showEditItemDialog(item) }
            }

            val buttonDelete = Button(this).apply {
                text = "Excluir"
                setOnClickListener { showDeleteItemDialog(item) }
            }

            sectionLayout.addView(textViewSection)
            sectionLayout.addView(buttonEdit)
            sectionLayout.addView(buttonDelete)
            itemListLayout.addView(sectionLayout)
        }
    }

    // Função para mostrar o diálogo de edição da seção
    private fun showEditItemDialog(item: Item) {
        val editText = EditText(this).apply { setText(item.name) }
        AlertDialog.Builder(this)
            .setTitle("Editar Item")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    dbHelper.updateItem(item.id, newName)
                    loadItems()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função para mostrar o diálogo de confirmação para excluir a seção
    private fun showDeleteItemDialog(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Seção")
            .setMessage("Tem certeza que deseja excluir esta seção?")
            .setPositiveButton("Sim") { _, _ ->
                dbHelper.deleteItem(item.id)
                loadItems()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
