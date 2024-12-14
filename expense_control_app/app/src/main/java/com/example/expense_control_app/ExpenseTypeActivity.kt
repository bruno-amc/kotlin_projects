package com.example.expense_control_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseTypeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var itemListLayout: LinearLayout
    private lateinit var editTextNewItem: EditText
    private lateinit var buttonAddItem: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_type)

        // Inicializar o DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Referências aos elementos da tela / interface
        itemListLayout = findViewById(R.id.item_list)
        editTextNewItem = findViewById(R.id.editText_new_item)
        buttonAddItem = findViewById(R.id.button_add_expense_type)

        // Carregar items salvos
        loadItems()

        // Ação para adicionar novo item
        buttonAddItem.setOnClickListener {
            val itemName = editTextNewItem.text.toString().trim()
            if (itemName.isNotEmpty()) {
                // Adicionar ao banco de dados
                dbHelper.addExpenseType(itemName)
                // Limpar o campo e recarregar a lista
                editTextNewItem.text.clear()
                loadItems()
                Toast.makeText(this, "Categoria ${itemName} inserida!", Toast.LENGTH_SHORT).show()




            } else {
                Toast.makeText(this, "Por favor, insira o item", Toast.LENGTH_SHORT).show()
            }
        }





    }



    // Função para carregar seções do banco de dados
    private fun loadItems() {
        itemListLayout.removeAllViews() // Limpa a lista de itens antes de recarregar

        val cursor = dbHelper.getAllExpenseTypes()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Obtenha os valores das colunas
                val itemId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_ID))
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE))

                // Cria o layout para cada item
                val sectionLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val textViewSection = TextView(this).apply {
                    text = itemName
                    textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val buttonDelete = Button(this).apply {
                    text = "Excluir"
                    setOnClickListener { showDeleteSectionDialog(itemId) } // Passa o ID diretamente
                }

                // Adiciona botão de exclusão (opcional)
               // val buttonDelete = Button(this).apply {
               //     text = "Excluir"
                 //   setOnClickListener { showDeleteItemDialog(itemId) } // Implementar a lógica do diálogo
              //  }

                // Adiciona as views ao layout
                sectionLayout.addView(textViewSection)
                sectionLayout.addView(buttonDelete)

                itemListLayout.addView(sectionLayout)

            } while (cursor.moveToNext()) // Move para o próximo item no cursor
        }

        // Sempre feche o cursor após usá-lo para evitar vazamentos de memória
        cursor?.close()
    }


    // Função para mostrar o diálogo de confirmação para excluir a seção
    private fun showDeleteSectionDialog(itemId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Categoria")
            .setMessage("Tem certeza que deseja excluir esta categoria?")
            .setPositiveButton("Sim") { _, _ ->
                val isDeleted = dbHelper.deleteExpenseType(itemId)
                if (isDeleted) {
                    Toast.makeText(this, "Categoria excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    loadItems() // Atualiza a lista de categorias
                } else {
                    Toast.makeText(this, "Não é possível excluir. Há despesas associadas a esta categoria.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}