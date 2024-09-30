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

        // SQL para criar as tabelas, com IF NOT EXISTS
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
        // Cria as tabelas se não existirem
        db.execSQL(CREATE_TABLE_SECTIONS)
        db.execSQL(CREATE_TABLE_ITEMS)
        db.execSQL(CREATE_TABLE_CHECKLIST)
        db.execSQL(CREATE_TABLE_EMPRESA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Atualiza o banco de dados, se necessário
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
        // Excluir também os itens relacionados ao checklist que utilizam essa seção
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_SECTION_ID = ?", arrayOf(sectionId.toString()))
        // Excluir a própria seção
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

    // Função para atualizar o nome do ITEM
    fun updateItem(itemId: Long, newName: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ITEM_NAME, newName)
        }
        db.update(TABLE_ITEMS, contentValues, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
    }

    // Função para excluir um ITEM
    fun deleteItem(itemId: Long) {
        val db = this.writableDatabase
        // Excluir também os itens relacionados ao checklist que utilizam essa seção
        db.delete(TABLE_CHECKLIST, "$COLUMN_CHECKLIST_ITEM_ID = ?", arrayOf(itemId.toString()))
        // Excluir a própria seção
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


    // Função para buscar as ITEMS salvos no banco de dados
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

    // Função para buscar os itens de uma seção específica
    fun getItemsBySection(sectionId: Long): List<Item> {
        val itemsList = mutableListOf<Item>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_ITEMS WHERE $COLUMN_ITEM_ID IN (SELECT $COLUMN_CHECKLIST_ITEM_ID FROM $TABLE_CHECKLIST WHERE $COLUMN_CHECKLIST_SECTION_ID = ?)",
            arrayOf(sectionId.toString())
        )

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


    // Função para salvar ou atualizar os dados da empresa
    fun saveDadosEmpresa(nomeOficina: String, telefone: String, endereco: String, email: String, logoUri: String?, outrasInformacoes: String) {
        val db = this.writableDatabase

        // Verifica se já existem dados salvos
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
            // Se os dados já existem, faz um update
            db.update(TABLE_EMPRESA, contentValues, null, null)
        } else {
            // Se os dados não existem, insere um novo registro
            db.insert(TABLE_EMPRESA, null, contentValues)
        }

        cursor.close() }

    // Função para pegar os dados da tabela EMPRESA
    fun getDadosEmpresa(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EMPRESA LIMIT 1", null)
    }

    //Função para deletar os dados da tabela EMPRESA
    fun deleteDadosEmpresa(): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_EMPRESA, null, null)  // Exclui todos os dados da tabela (nome da tabela, where, argumentos do where)
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
