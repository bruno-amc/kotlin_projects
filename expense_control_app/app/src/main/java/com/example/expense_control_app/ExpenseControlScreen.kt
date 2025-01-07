package com.example.expense_control_app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
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
    private lateinit var spinnerCategoryFilter: Spinner
    private lateinit var buttonStartDate: Button //data inicial do campo de filtro
    private lateinit var buttonEndDate: Button //data final do campo de filtro
    private lateinit var textViewSomaGastos: TextView
    private lateinit var buttonSelectDate: Button //data para a parte de criação de nova despesa
    private lateinit var buttonSelectTime: Button //hora para a parte de criação de nova despesa
    private var selectedDateCalendar: String? = null  // variável para data para a parte de criação de nova despesa
    private var selectedTimeClock: String? = null // variável para hora para a parte de criação de nova despesa

    @SuppressLint("ClickableViewAccessibility")
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
        spinnerCategoryFilter = findViewById(R.id.spinner_category_filter)
        buttonStartDate = findViewById(R.id.button_select_start_date)
        buttonEndDate = findViewById(R.id.button_select_end_date)
        textViewSomaGastos = findViewById(R.id.textViewSomaGastos)
        buttonSelectDate = findViewById(R.id.button_select_date)
        buttonSelectTime = findViewById(R.id.button_select_time)

        // Estado inicial dos botões de Adicionar Despesas e de clicar na Hora
        buttonSelectTime.isEnabled = false // Hora congelada, só libera dps que a data for escolhida
        buttonAddNewExpense.isEnabled = true // Adicionar despesa liberado, só congela se uma data for escolhida até que a hora tbm seja escolhida (obrigar usuário a escolher data + hora).


        // botão do canto superior esquerdo das engrenagens de configuração que faz navegação para tela de configurações
        val btn_icon_got_to_screen_configuration: ImageButton = findViewById(R.id.button_icon_configuration)
        btn_icon_got_to_screen_configuration.setOnClickListener {
            val intent = Intent(this, ConfigurationActivity::class.java)
            startActivity(intent)
        }


        // botões dos filtros de data e aplicar filtros, limpar filtros

        val buttonApplyFilter = findViewById<Button>(R.id.button_apply_date_filter)
        val buttonCleanFilters = findViewById<Button>(R.id.button_remove_date_and_category_filter)

        // BOTÃO PARA ROLAR PARA A PARTE SUPERIOR DA TELA
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nestedScrollView)
        val buttonScrollToTop = findViewById<Button>(R.id.button_scroll_to_top)
        // Rola suavemente para o topo
        buttonScrollToTop.setOnClickListener {
            nestedScrollView.smoothScrollTo(0, 0)
        }


        // ação para escolher a data na parte de criação de nova despesa
        buttonSelectDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                selectedDateCalendar = selectedDate
                buttonSelectDate.text = " $selectedDate "
                Toast.makeText(this, "Data escolhida: $selectedDate\n Escolha a Hora / Minuto", Toast.LENGTH_SHORT).show()
                // Descongela o botão de hora
                buttonSelectTime.isEnabled = true
                // Congela o botão de adicionar despesa
                buttonAddNewExpense.isEnabled = false
            }
        }
        //ação para escolher a hora na parte de criação de nova despesa
        buttonSelectTime.setOnClickListener {
            showTimePickerDialog { selectedTime ->
                selectedTimeClock = selectedTime
                buttonSelectTime.text = " $selectedTime "
                Toast.makeText(this, "Hora escolhida: $selectedTime", Toast.LENGTH_SHORT).show()
                // Descongela o botão de adicionar despesa
                buttonAddNewExpense.isEnabled = true

            }
        }


        // Carregar categorias no Spinner
        loadCategoryFilter()

        setupExpenseCategorySpinner()

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
            applyFilters()
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
            spinnerCategoryFilter.setSelection(0) // Retorna para "Selecione uma categoria"

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

            val (currentDate, currentTime) = getCurrentDateTime()

            val finalDateTime = when {
                selectedDateCalendar != null && selectedTimeClock != null -> "$selectedDateCalendar $selectedTimeClock:00"
                selectedDateCalendar != null -> "$selectedDateCalendar $currentTime"
                else -> currentDate
            }


            if (expenseValue != null && selectedCategory != "Selecione uma categoria") {
                val categoryId = dbHelper.getCategoryIdByName(selectedCategory)

                if (categoryId != null) {
                    dbHelper.addExpense(expenseValue, expenseNotes, finalDateTime, categoryId)

                    Toast.makeText(this, "Despesa adicionada na categoria $selectedCategory", Toast.LENGTH_SHORT).show()

                    editTextNewExpenseValue.text.clear()
                    editTextNewExpenseNotes.text.clear()
                    spinnerExpenseCategory.setSelection(0)

                    selectedDateCalendar = null
                    selectedTimeClock = null
                    buttonSelectDate.text = "Selecionar Data"
                    buttonSelectTime.text = "Selecionar Hora"

                    buttonSelectTime.isEnabled = false
                    buttonAddNewExpense.isEnabled = true

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

    override fun onResume() {
        super.onResume()
        // Recarregar as categorias do spinner de inserção de despesas ao retornar para esta tela
        loadExpenseCategories()

        // Recarregar as categorias no spinner de filtro
        loadCategoryFilter()
    }

    private fun applyFilters() {
        val selectedCategory = spinnerCategoryFilter.selectedItem.toString()
        val categoryId = if (selectedCategory != "Todas as Categorias") dbHelper.getCategoryIdByName(selectedCategory) else null

        val startDateFormatted = startDate?.let { "$it 00:00:00" }
        val endDateFormatted = endDate?.let { "$it 23:59:59" }

        // Carregar despesas filtradas
        loadExpensesFiltered(categoryId, startDateFormatted, endDateFormatted)
        Toast.makeText(this, "Filtro Aplicado", Toast.LENGTH_SHORT).show()
    }

    // Função para exibir o TimePickerDialog
    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(selectedTime) // Retorna o horário selecionado
        }, hour, minute, true) // 'true' para formato 24h

        timePickerDialog.show()
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

    // Função para carregar as categorias no Spinner da parte de inserção de nova despesa
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

        // Verificar se existem categorias além de "Selecione uma categoria"
        if (categories.size == 1) { // Significa que apenas a opção inicial está presente
            Toast.makeText(this, "Nenhuma categoria cadastrada.\nInsira categorias nas configurações.", Toast.LENGTH_LONG).show()
        }

        // Configurar o Spinner com as categorias
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExpenseCategory.adapter = adapter

    }

    //Função para Obter Data e Hora
    private fun getCurrentDateTime(): Pair<String, String> {

        //obeter data e hora
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())

        //Obter somente a hora, sem a data
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return Pair( dateFormat.format(java.util.Date()), timeFormat.format(java.util.Date()))
    }

    // função para avisar o usuário cadastrar uma categoria se o spinner estiver vazio
    @SuppressLint("ClickableViewAccessibility")
    private fun setupExpenseCategorySpinner() {
        spinnerExpenseCategory.setOnTouchListener { _, _ ->
            // Verifica se o Spinner está vazio (apenas com a opção inicial)
            if ((spinnerExpenseCategory.adapter?.count ?: 0) <= 1) {
                Toast.makeText(this, "Nenhuma categoria cadastrada.\nInsira categorias nas configurações.", Toast.LENGTH_LONG).show()
            }
            false // Retorna false para permitir que o Spinner abra (se necessário)
        }
    }

    //método para atualizar a exibição quando usamos os filtros por datas e o filtro de categorias
    private fun loadExpensesFiltered(categoryId: Long?, startDate: String?, endDate: String?) {
        expenseListLayout.removeAllViews()
        var total = 0.0

        val cursor = dbHelper.getFilteredExpenses(categoryId, startDate, endDate)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Garantir que as colunas existem
                val expenseIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSES_ID)
                val expenseValueIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSES)
                val expenseNotesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXTRA_NOTES)
                val expenseDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE)
                val expenseCategoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE)

                if (expenseIdIndex != -1 && expenseValueIndex != -1 && expenseNotesIndex != -1 &&
                    expenseDateIndex != -1 && expenseCategoryIndex != -1) {

                    // Obter valores
                    val expenseId = cursor.getLong(expenseIdIndex)
                    val expenseValue = cursor.getDouble(expenseValueIndex)
                    val expenseNotes = cursor.getString(expenseNotesIndex)
                    val expenseDate = cursor.getString(expenseDateIndex)
                    val expenseCategory = cursor.getString(expenseCategoryIndex)

                    // Incrementar o total
                    total += expenseValue

                    // Criar TextView e Botão de Exclusão
                    val textViewExpenseInfo = TextView(this).apply {
                        text = "R$ %.2f - $expenseCategory\n$expenseDate\nObs: $expenseNotes\n".format(expenseValue)
                        textSize = 16f
                    }

                    val buttonDeleteExpense = Button(this).apply {
                        text = "Excluir"
                        textSize = 14f
                        setOnClickListener {
                            try {
                                showDeleteExpenseDialog(expenseId)
                            } catch (e: Exception) {
                                Toast.makeText(this@ExpenseControlScreen, "Erro ao excluir despesa.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // Criar Layout Horizontal para TextView e Botão
                    val expenseLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(8, 8, 8, 8)
                        addView(textViewExpenseInfo, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        addView(buttonDeleteExpense)
                    }

                    // Adicionar o Layout ao Container
                    expenseListLayout.addView(expenseLayout)
                }
            } while (cursor.moveToNext())
        }

        cursor?.close()

        // Atualizar o Total no TextView
        val formatador = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        textViewSomaGastos.text = "Total: ${formatador.format(total)}"
    }




    // função para cerregar as categorias no filtro/spinner de categorias
    private fun loadCategoryFilter() {
        val categories = mutableListOf("Todas as Categorias") // Opção padrão
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
        spinnerCategoryFilter.adapter = adapter
    }


    //função para mostrar o diálogo de confirmação do botão de deleção que tem ao lado de cada item
    private fun showDeleteExpenseDialog(expenseId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Despesa")
            .setMessage("Tem certeza de que deseja excluir esta despesa?")
            .setPositiveButton("Sim") { _, _ ->
                dbHelper.deleteExpense(expenseId)
                Toast.makeText(this, "Despesa excluída com sucesso!", Toast.LENGTH_SHORT).show()

                // Reaplicar os filtros atualmente ativos
                val selectedCategory = spinnerCategoryFilter.selectedItem.toString()
                val categoryId = if (selectedCategory != "Todas as Categorias") dbHelper.getCategoryIdByName(selectedCategory) else null

                val startDateFormatted = startDate?.let { "$it 00:00:00" }
                val endDateFormatted = endDate?.let { "$it 23:59:59" }

                loadExpensesFiltered(categoryId, startDateFormatted, endDateFormatted)
            }
            .setNegativeButton("Não", null)
            .show()
    }



    //Função para carregar as despesas
    private fun loadExpenses() {
        expenseListLayout.removeAllViews()
        var total = 0.0

        val cursor = dbHelper.getAllExpenses()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val expenseId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSES_ID))
                val expenseValue = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSES))
                val expenseNotes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXTRA_NOTES))
                val expenseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val expenseCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSESTYPE_TYPE))

                // Incrementar o total
                total += expenseValue

                // Criar TextView e Botão de Exclusão
                val textViewExpenseInfo = TextView(this).apply {
                    text = String.format(
                        "R$ %.2f - %s\n%s\nObs: %s\n",
                        expenseValue,
                        expenseCategory ?: "Sem categoria",
                        expenseDate ?: "Data não registrada",
                        expenseNotes ?: "Sem observações"
                    )
                    textSize = 16f
                }

                val buttonDeleteExpense = Button(this).apply {
                    text = "Excluir"
                    textSize = 14f
                    setOnClickListener {
                        showDeleteExpenseDialog(expenseId)
                    }
                }

                // Criar Layout Horizontal para TextView e Botão
                val expenseLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(8, 8, 8, 8)
                    addView(textViewExpenseInfo, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(buttonDeleteExpense)
                }

                // Adicionar o Layout ao Container
                expenseListLayout.addView(expenseLayout)

            } while (cursor.moveToNext())
        }

        cursor?.close()

        // Atualizar o Total no TextView
        val formatador = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        textViewSomaGastos.text = "Total: ${formatador.format(total)}"
    }


}


