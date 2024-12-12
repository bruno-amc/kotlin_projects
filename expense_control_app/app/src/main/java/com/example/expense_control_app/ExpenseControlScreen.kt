package com.example.expense_control_app

import android.app.DatePickerDialog
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
import java.util.Calendar

class ExpenseControlScreen : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerExpenseCategory: Spinner
    private lateinit var editTextNewExpenseValue: EditText
    private lateinit var editTextNewExpenseNotes: EditText
    private lateinit var buttonAddNewExpense: Button
    private lateinit var expenseListLayout: LinearLayout
    private var startDate: String? = null
    private var endDate: String? = null

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


        // botões dos filtros de data e aplicar filtros, limpar filtros
        val buttonStartDate = findViewById<Button>(R.id.button_select_start_date)
        val buttonEndDate = findViewById<Button>(R.id.button_select_end_date)
        val buttonApplyFilter = findViewById<Button>(R.id.button_apply_date_filter)
        val buttonCleanFilters = findViewById<Button>(R.id.button_remove_date_and_category_filter)

        // Ação para selecionar a data inicial
        buttonStartDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                startDate = selectedDate
                buttonStartDate.text = "Início: $selectedDate"
            }
        }

        // Ação para selecionar a data final
        buttonEndDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                endDate = selectedDate
                buttonEndDate.text = "Fim: $selectedDate"
            }
        }

        // Ação para aplicar o filtro
        buttonApplyFilter.setOnClickListener {
            if (startDate != null && endDate != null) {
                // Ajustar o final do dia para a data final
                val adjustedEndDate = "$endDate 23:59:59"

                loadExpensesFilteredByDate("$startDate 00:00:00", adjustedEndDate)
            } else {
                Toast.makeText(this, "Por favor, selecione as datas.", Toast.LENGTH_SHORT).show()
            }
        }

        // Ação para o botão de limpar os filtros de data e categoria
        buttonCleanFilters.setOnClickListener {
            // Resetar as variáveis de filtro
            startDate = null
            endDate = null

            // Restaurar os textos dos botões
            buttonEndDate.text = "Data Final"
            buttonStartDate.text = "Data Inicial"

            // Opcional: Resetar categoria (Spinner, por exemplo)
            spinnerExpenseCategory.setSelection(0) // Retorna para "Selecione uma categoria"

            // Carregar todas as despesas novamente
            loadExpenses()

            // Feedback ao usuário
            Toast.makeText(this, "Filtros limpos com sucesso!", Toast.LENGTH_SHORT).show()
        }

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

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSelected(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
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

    //método para atualizar a exibição quando usamos os filtros por datas
    private fun loadExpensesFilteredByDate(startDate: String, endDate: String) {
        expenseListLayout.removeAllViews()

        val cursor = dbHelper.getExpensesFilteredByDate(startDate, endDate)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val expenseValue = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSES))
                val expenseNotes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXTRA_NOTES))
                val expenseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val expenseCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE))

                // Criar layout para cada despesa
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


