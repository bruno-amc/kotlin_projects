package com.example.expense_control_app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expenseApp.db"
        private const val DATABASE_VERSION = 1

        // Tabela de categorias de despesas
        const val TABLE_EXPENSESTYPE = "expenses"
        const val COLUMN_EXPENSESTYPE_ID = "id"
        const val COLUMN_EXPENSESTYPE_TYPE = "type"


        // Tabela de todas as despesas inseridas
        const val TABLE_EXPENSES = "expensesTable"
        const val COLUMN_EXPENSES_ID = "id"
        const val COLUMN_EXPENSES = "expense"
        const val COLUMN_EXTRA_NOTES = "notes"
        const val COLUMN_DATE = "date"
        const val COLUMN_EXPENSES_CATEGORY_ID = "category_id"

        // SQL para criar a tabela de categorias de despesas
        private const val CREATE_TABLE_EXPENSESTYPE = """
            CREATE TABLE IF NOT EXISTS $TABLE_EXPENSESTYPE (
                $COLUMN_EXPENSESTYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXPENSESTYPE_TYPE TEXT NOT NULL
            )
        """


        // SQL para criar a tabela de despesas
        private const val CREATE_TABLE_EXPENSES = """
        CREATE TABLE IF NOT EXISTS $TABLE_EXPENSES (
            $COLUMN_EXPENSES_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_EXPENSES REAL NOT NULL, 
            $COLUMN_EXTRA_NOTES TEXT,
            $COLUMN_DATE TEXT NOT NULL,
            $COLUMN_EXPENSES_CATEGORY_ID INTEGER NOT NULL,
            FOREIGN KEY ($COLUMN_EXPENSES_CATEGORY_ID) REFERENCES $TABLE_EXPENSESTYPE($COLUMN_EXPENSESTYPE_ID)
        )
    """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_EXPENSESTYPE) // Cria a tabela de categorias
        db.execSQL(CREATE_TABLE_EXPENSES) // Cria a tabela de despesas
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSESTYPE")
        onCreate(db) // Recria a tabela após o drop
    }

    // Função para adicionar uma nova categoria de gastos
    fun addExpenseType(type: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_EXPENSESTYPE_TYPE, type)
        }
        val result = db.insert(TABLE_EXPENSESTYPE, null, contentValues)
       // db.close()
        return result
    }

    // Função para excluir uma categoria de gastos
       fun deleteExpenseType(typeId: Long): Boolean {
        val db = this.readableDatabase

        // Verificar se há despesas associadas à categoria
        val cursor = db.query(
            TABLE_EXPENSES,
            arrayOf(COLUMN_EXPENSES_ID),
            "$COLUMN_EXPENSES_CATEGORY_ID = ?",
            arrayOf(typeId.toString()),
            null,
            null,
            null
        )

        val hasAssociatedExpenses = cursor.count > 0
        cursor.close()

        if (hasAssociatedExpenses) {
            // Retorna falso para indicar que a exclusão não foi realizada
            return false
        }

        // Excluir a categoria se não houver despesas associadas
        val writableDb = this.writableDatabase
        writableDb.delete(TABLE_EXPENSESTYPE, "$COLUMN_EXPENSESTYPE_ID = ?", arrayOf(typeId.toString()))
        return true // Retorna true para indicar que a exclusão foi realizada
    }

    // Função para buscar todas as categorias de gastos
    fun getAllExpenseTypes(): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_EXPENSESTYPE,
            arrayOf(COLUMN_EXPENSESTYPE_ID, COLUMN_EXPENSESTYPE_TYPE),
            null,
            null,
            null,
            null,
            "$COLUMN_EXPENSESTYPE_TYPE ASC"
        )
    }

    // Função para verificar se uma categoria existe
    fun expenseTypeExists(type: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EXPENSESTYPE,
            arrayOf(COLUMN_EXPENSESTYPE_ID),
            "$COLUMN_EXPENSESTYPE_TYPE = ?",
            arrayOf(type),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }


    // ABAIXO, AS FUNCOES REFERETES A TABELA COM TODAS AS INFORMACOES DAS DESPESAS
    //adicionar despesas, tipos, data, observacoes
    fun addExpense(expense: Double, notes: String?, date: String, categoryId: Long): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_EXPENSES, expense)
            put(COLUMN_EXTRA_NOTES, notes)
            put(COLUMN_DATE, date)
            put(COLUMN_EXPENSES_CATEGORY_ID, categoryId)
        }
        return db.insert(TABLE_EXPENSES, null, contentValues)
    }

    // buscar todas as despesas, incluindo a categoria associada
    fun getAllExpenses(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            """
        SELECT e.$COLUMN_EXPENSES_ID AS id, e.$COLUMN_EXPENSES AS expense, e.$COLUMN_EXTRA_NOTES AS notes, 
               e.$COLUMN_DATE AS date, c.$COLUMN_EXPENSESTYPE_TYPE AS type
        FROM $TABLE_EXPENSES e
        INNER JOIN $TABLE_EXPENSESTYPE c ON e.$COLUMN_EXPENSES_CATEGORY_ID = c.$COLUMN_EXPENSESTYPE_ID
        ORDER BY e.$COLUMN_DATE DESC
        """, null
        )
    }


    //BUSCAR A CATEGORIA USANDO O ID
    fun getCategoryIdByName(categoryName: String): Long? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EXPENSESTYPE,
            arrayOf(COLUMN_EXPENSESTYPE_ID),
            "$COLUMN_EXPENSESTYPE_TYPE = ?",
            arrayOf(categoryName),
            null,
            null,
            null
        )
        var categoryId: Long? = null
        if (cursor.moveToFirst()) {
            categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSESTYPE_ID))
        }
        cursor.close()
        return categoryId
    }


    //Função para buscar despesas entre duas datas
    fun getExpensesFilteredByDate(startDate: String, endDate: String): Cursor {
        val db = this.readableDatabase



        // Tratar a data final para incluir o final do dia
        val query = """
        SELECT e.$COLUMN_EXPENSES, e.$COLUMN_EXTRA_NOTES, e.$COLUMN_DATE, c.$COLUMN_EXPENSESTYPE_TYPE
        FROM $TABLE_EXPENSES e
        INNER JOIN $TABLE_EXPENSESTYPE c ON e.$COLUMN_EXPENSES_CATEGORY_ID = c.$COLUMN_EXPENSESTYPE_ID
        WHERE e.$COLUMN_DATE >= ? AND e.$COLUMN_DATE <= ?
        ORDER BY e.$COLUMN_DATE DESC
    """
        return db.rawQuery(query, arrayOf("$startDate 00:00:00", "$endDate 23:59:59"))
    }

    // função para deletar uma despesa usando o botão de excluir que tem ao lado de cada despesa
    fun deleteExpense(expenseId: Long): Boolean {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_EXPENSES, "$COLUMN_EXPENSES_ID = ?", arrayOf(expenseId.toString()))
        return rowsDeleted > 0
    }

    //função para buscar despesas com base na categoria e datas
    fun getFilteredExpenses(categoryId: Long?, startDate: String?, endDate: String?): Cursor {
        val db = this.readableDatabase
        val selectionArgs = mutableListOf<String>()
        val selectionBuilder = StringBuilder()

        // Filtro de categoria
        if (categoryId != null) {
            selectionBuilder.append("e.$COLUMN_EXPENSES_CATEGORY_ID = ?")
            selectionArgs.add(categoryId.toString())
        }

        // Filtro de datas
        if (startDate != null && endDate != null) {
            if (selectionBuilder.isNotEmpty()) selectionBuilder.append(" AND ")
            selectionBuilder.append("e.$COLUMN_DATE BETWEEN ? AND ?")
            selectionArgs.add(startDate)
            selectionArgs.add(endDate)
        } else if (startDate != null) {
            if (selectionBuilder.isNotEmpty()) selectionBuilder.append(" AND ")
            selectionBuilder.append("e.$COLUMN_DATE >= ?")
            selectionArgs.add(startDate)
        } else if (endDate != null) {
            if (selectionBuilder.isNotEmpty()) selectionBuilder.append(" AND ")
            selectionBuilder.append("e.$COLUMN_DATE <= ?")
            selectionArgs.add(endDate)
        }

        // Consulta SQL com JOIN
        val query = """
        SELECT e.$COLUMN_EXPENSES_ID AS id, e.$COLUMN_EXPENSES, e.$COLUMN_EXTRA_NOTES, e.$COLUMN_DATE, c.$COLUMN_EXPENSESTYPE_TYPE
        FROM $TABLE_EXPENSES e
        INNER JOIN $TABLE_EXPENSESTYPE c ON e.$COLUMN_EXPENSES_CATEGORY_ID = c.$COLUMN_EXPENSESTYPE_ID
        ${if (selectionBuilder.isNotEmpty()) "WHERE $selectionBuilder" else ""}
        ORDER BY e.$COLUMN_DATE DESC
    """

        return db.rawQuery(query, selectionArgs.toTypedArray())
    }


}
