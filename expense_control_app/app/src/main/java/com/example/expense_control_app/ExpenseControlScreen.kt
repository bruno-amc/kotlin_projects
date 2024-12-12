package com.example.expense_control_app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseControlScreen : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerExpenseCategory: Spinner
    private lateinit var editTextNewExpenseValue: EditText
    private lateinit var editTextNewExpenseNotes: EditText
    private lateinit var buttonAddNewExpense: Button
    private lateinit var expenseListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_control_screen)


        // Inicializando os componentes
        dbHelper = DatabaseHelper(this)
        spinnerExpenseCategory = findViewById(R.id.spinner_expense_category)
        editTextNewExpenseValue = findViewById(R.id.editText_new_expense_value)
        editTextNewExpenseNotes = findViewById(R.id.editText_new_expense_notes)
        buttonAddNewExpense = findViewById(R.id.button_add_new_expense)
        expenseListLayout = findViewById(R.id.expense_list_with_all_infos)

        // Carregar as categorias no Spinner
        loadExpenseCategories()

        // carregar as despesas assim que a tela abrir
        loadExpenses()

        // Ação do botão para adicionar uma despesa
        buttonAddNewExpense.setOnClickListener {
            val selectedCategory = spinnerExpenseCategory.selectedItem.toString()
            val expenseValue = editTextNewExpenseValue.text.toString().toDoubleOrNull()
            val expenseNotes = editTextNewExpenseNotes.text.toString()

            val currentDate = getCurrentDateTime()

            if (expenseValue != null && selectedCategory != "Selecione uma categoria") {
                val categoryId = dbHelper.getCategoryIdByName(selectedCategory)

                if (categoryId != null) {
                    dbHelper.addExpense(expenseValue, expenseNotes, currentDate, categoryId)

                    Toast.makeText(this, "Despesa adicionada na categoria $selectedCategory", Toast.LENGTH_SHORT).show()

                    editTextNewExpenseValue.text.clear()
                    editTextNewExpenseNotes.text.clear()
                    spinnerExpenseCategory.setSelection(0) // Retorna o Spinner para a posição inicial
                    loadExpenses()
                } else {
                    Toast.makeText(this, "Erro ao encontrar a categoria selecionada.", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (selectedCategory == "Selecione uma categoria") {
                    Toast.makeText(this, "Por favor, selecione uma categoria válida!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Preencha o valor da despesa!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Função para carregar as categorias no Spinner
    private fun loadExpenseCategories() {
        val categories = mutableListOf<String>()
        // Adicionar a opção inicial
        categories.add("Selecione uma categoria")

        val cursor = dbHelper.getAllExpenseTypes()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE))
                categories.add(categoryName)
            } while (cursor.moveToNext())
        }
        cursor?.close()

        // Configurar o Spinner com as categorias
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExpenseCategory.adapter = adapter

    }

    //Função para Obter Data e Hora
    private fun getCurrentDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }


    //Função para carregar as despesas
    private fun loadExpenses() {
        expenseListLayout.removeAllViews()

        val cursor = dbHelper.getAllExpenses()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val expenseValue = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSES))
                val expenseNotes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXTRA_NOTES))
                val expenseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val expenseCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE))

                // Criar um layout para cada despesa
                val expenseLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(8, 8, 8, 8)
                }

                val textViewExpenseInfo = TextView(this).apply {
                    text = "R$ %.2f - $expenseCategory\n$expenseDate\nNotas: $expenseNotes".format(expenseValue)
                    textSize = 16f
                }

                expenseLayout.addView(textViewExpenseInfo)
                expenseListLayout.addView(expenseLayout)
            } while (cursor.moveToNext())
        }

        cursor?.close()
    }

}


