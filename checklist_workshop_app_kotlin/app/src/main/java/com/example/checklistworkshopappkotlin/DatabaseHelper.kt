package com.example.checklistworkshopappkotlin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "checklist.db"
        private const val DATABASE_VERSION = 1

        // Tabela de Dados da Empresa
        const val TABLE_EMPRESA = "dados_empresa"
        const val COLUMN_NOME_OFICINA = "nome_oficina"
        const val COLUMN_TELEFONE = "telefone"
        const val COLUMN_ENDERECO = "endereco"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_LOGO_URI = "logo_uri"
        const val COLUMN_OUTRAS_INFORMACOES = "outras_informacoes"

        // Tabela de Seções
        const val TABLE_SECTIONS = "sections"
        const val COLUMN_SECTION_ID = "id"
        const val COLUMN_SECTION_NAME = "nome"

        // Tabela de Itens
        const val TABLE_ITEMS = "items"
        const val COLUMN_ITEM_ID = "id"
        const val COLUMN_ITEM_NAME = "nome"

        // Tabela de Checklist (para associar seções e itens)
        const val TABLE_CHECKLIST = "checklist"
        const val COLUMN_CHECKLIST_ID = "id"
        const val COLUMN_CHECKLIST_NAME = "checklist_name"
        const val COLUMN_CHECKLIST_SECTION_ID = "section_id"
        const val COLUMN_CHECKLIST_ITEM_ID = "item_id"

        // SQL para criar as tabelas
        private const val CREATE_TABLE_SECTIONS = """
            CREATE TABLE IF NOT EXISTS $TABLE_SECTIONS (
                $COLUMN_SECTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SECTION_NAME TEXT NOT NULL
            )
        """
        private const val CREATE_TABLE_ITEMS = """
            CREATE TABLE IF NOT EXISTS $TABLE_ITEMS (
                $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ITEM_NAME TEXT NOT NULL
            )
        """

        // SQL para criar a tabela de dados da empresa
        private const val CREATE_TABLE_EMPRESA = """
            CREATE TABLE IF NOT EXISTS $TABLE_EMPRESA (
                $COLUMN_NOME_OFICINA TEXT,
                $COLUMN_TELEFONE TEXT,
                $COLUMN_ENDERECO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_LOGO_URI TEXT,
                $COLUMN_OUTRAS_INFORMACOES TEXT
            )
        """

        private const val CREATE_TABLE_CHECKLIST = """
            CREATE TABLE IF NOT EXISTS $TABLE_CHECKLIST (
                $COLUMN_CHECKLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHECKLIST_NAME TEXT NOT NULL,
                $COLUMN_CHECKLIST_SECTION_ID INTEGER,
                $COLUMN_CHECKLIST_ITEM_ID INTEGER,
                FOREIGN KEY($COLUMN_CHECKLIST_SECTION_ID) REFERENCES $TABLE_SECTIONS($COLUMN_SECTION_ID),
                FOREIGN KEY($COLUMN_CHECKLIST_ITEM_ID) REFERENCES $TABLE_ITEMS($COLUMN_ITEM_ID)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SECTIONS)
        db.execSQL(CREATE_TABLE_ITEMS)
        db.execSQL(CREATE_TABLE_CHECKLIST)
        db.execSQL(CREATE_TABLE_EMPRESA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHECKLIST")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SECTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPRESA")
        onCreate(db)
    }

    // Função para adicionar uma nova seção
    fun addSection(nome: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SECTION_NAME, nome)
        }
        return db.insert(TABLE_SECTIONS, null, contentValues)
    }

    // Função para atualizar o nome da seção
    fun updateSection(sectionId: Long, newName: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SECTION_NAME, newName)
        }
        db.update(TABLE_SECTIONS, contentValues, "$COLUMN_SECTION_ID = ?", arrayOf(sectionId.toString()))
    }

    // Função para excluir uma seção
    fun deleteSection(sectionId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_SECTION_ID = ?", arrayOf(sectionId.toString()))
        db.delete(TABLE_SECTIONS, "$COLUMN_SECTION_ID = ?", arrayOf(sectionId.toString()))
    }

    // Função para adicionar um novo item
    fun addItem(nome: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ITEM_NAME, nome)
        }
        return db.insert(TABLE_ITEMS, null, contentValues)
    }

    // Função para atualizar o nome do item
    fun updateItem(itemId: Long, newName: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ITEM_NAME, newName)
        }
        db.update(TABLE_ITEMS, contentValues, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
    }

    // Função para excluir um item
    fun deleteItem(itemId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.delete(TABLE_ITEMS, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
    }

    // Função para associar seções e itens ao checklist
    fun addChecklist(checklistName: String, sectionId: Long, itemId: Long): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_CHECKLIST_NAME, checklistName)
            put(COLUMN_CHECKLIST_SECTION_ID, sectionId)
            put(COLUMN_CHECKLIST_ITEM_ID, itemId)
        }
        return db.insert(TABLE_CHECKLIST, null, contentValues)
    }

    // Função para buscar o ID de um item pelo nome
    fun getItemIdByName(itemName: String): Long {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT $COLUMN_ITEM_ID FROM $TABLE_ITEMS WHERE $COLUMN_ITEM_NAME = ?", arrayOf(itemName))
        return if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID))
        } else {
            -1 // Retorna -1 se o item não for encontrado
        }.also {
            cursor.close()
        }
    }

    // Função para buscar as seções salvas no banco de dados
    fun getSections(): List<Section> {
        val sectionsList = mutableListOf<Section>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_SECTIONS", null)

        if (cursor.moveToFirst()) {
            do {
                val sectionId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SECTION_ID))
                val sectionName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECTION_NAME))
                val section = Section(sectionId, sectionName)
                sectionsList.add(section)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return sectionsList
    }

    // Função para buscar os itens salvos no banco de dados
    fun getItems(): List<Item> {
        val itemsList = mutableListOf<Item>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_ITEMS", null)

        if (cursor.moveToFirst()) {
            do {
                val itemId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID))
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                val item = Item(itemId, itemName)
                itemsList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemsList
    }

    // Função para pegar os dados do checklist baseado no nome
    // Função para pegar os dados do checklist baseado no nome
    fun getChecklistByName(checklistName: String): List<ChecklistEntry> {
        val checklistEntries = mutableListOf<ChecklistEntry>()
        val db = this.readableDatabase
        val query = """
        SELECT sections.nome AS sectionName, items.nome AS itemName
        FROM checklist
        JOIN sections ON checklist.section_id = sections.id
        JOIN items ON checklist.item_id = items.id
        WHERE checklist.checklist_name = ?
    """
        val cursor: Cursor = db.rawQuery(query, arrayOf(checklistName))

        if (cursor.moveToFirst()) {
            do {
                val sectionName = cursor.getString(cursor.getColumnIndexOrThrow("sectionName"))
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow("itemName"))
                val checklistEntry = ChecklistEntry(sectionName, itemName)
                checklistEntries.add(checklistEntry)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return checklistEntries
    }


    // Função para obter o ID da seção pelo nome
    fun getSectionIdByName(sectionName: String): Long? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_SECTION_ID FROM $TABLE_SECTIONS WHERE $COLUMN_SECTION_NAME = ?", arrayOf(sectionName))
        var sectionId: Long? = null

        if (cursor.moveToFirst()) {
            sectionId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SECTION_ID))
        }
        cursor.close()
        return sectionId
    }



    // Função para pegar os itens de um checklist específico
    fun getChecklistItens(checklistName: String): List<String> {
        val itemList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT items.nome FROM items JOIN checklist ON items.id = checklist.item_id WHERE checklist.checklist_name = ?",
            arrayOf(checklistName)
        )

        if (cursor.moveToFirst()) {
            do {
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                itemList.add(itemName)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }

    // Função para salvar ou atualizar os dados da empresa
    fun saveDadosEmpresa(nomeOficina: String, telefone: String, endereco: String, email: String, logoUri: String?, outrasInformacoes: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EMPRESA", null)
        val contentValues = ContentValues().apply {
            put(COLUMN_NOME_OFICINA, nomeOficina)
            put(COLUMN_TELEFONE, telefone)
            put(COLUMN_ENDERECO, endereco)
            put(COLUMN_EMAIL, email)
            put(COLUMN_LOGO_URI, logoUri)
            put(COLUMN_OUTRAS_INFORMACOES, outrasInformacoes)
        }

        if (cursor.count > 0) {
            db.update(TABLE_EMPRESA, contentValues, null, null)
        } else {
            db.insert(TABLE_EMPRESA, null, contentValues)
        }
        cursor.close()
    }

    // Função para pegar os dados da tabela EMPRESA
    fun getDadosEmpresa(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EMPRESA LIMIT 1", null)
    }

    // Função para deletar os dados da tabela EMPRESA
    fun deleteDadosEmpresa(): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_EMPRESA, null, null)
    }

    // Função para pegar todos os checklists salvos
    // Função para pegar todos os checklists salvos
    fun getChecklists(): List<Checklist> {
        val checklistList = mutableListOf<Checklist>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT DISTINCT $COLUMN_CHECKLIST_NAME FROM $TABLE_CHECKLIST", null)

        if (cursor.moveToFirst()) {
            do {
                val checklistName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECKLIST_NAME))
                val checklist = Checklist(checklistName)
                checklistList.add(checklist)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return checklistList
    }

    // Função para salvar ou atualizar um checklist
    fun saveChecklist(checklistName: String, itens: List<String>) {
        val db = this.writableDatabase
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_NAME = ?", arrayOf(checklistName))

        for (item in itens) {
            val itemId = getItemIdByName(item)
            val contentValues = ContentValues().apply {
                put(COLUMN_CHECKLIST_NAME, checklistName)
                put(COLUMN_CHECKLIST_ITEM_ID, itemId)
            }
            db.insert(TABLE_CHECKLIST, null, contentValues)
        }
    }

    // Função para deletar um checklist baseado no nome
    fun deleteChecklistByName(checklistName: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_NAME = ?", arrayOf(checklistName))
    }


    // Função para verificar se o checklist já foi criado
    fun isChecklistCreated(): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CHECKLIST", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }
}

// Classe de dados para armazenar os itens
data class Item(val id: Long, val name: String)

// Classe de dados para armazenar as seções
data class Section(val id: Long, val name: String)

// Classe de dados para armazenar checklists
//data class Checklist(val id: Long, val name: String)


data class ChecklistEntry(val sectionName: String, val itemName: String)

// Classe de dados para armazenar checklists
data class Checklist(val name: String)


