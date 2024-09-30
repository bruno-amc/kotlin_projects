package com.example.checklistworkshopappkotlin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener

class DadosDaEmpresaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var editTextNomeOficina: EditText
    private lateinit var editTextTelefone: EditText
    private lateinit var editTextEndereco: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextOutrasInformacoes: EditText
    private lateinit var buttonSalvarDados: Button
    private lateinit var buttonUploadLogo: Button
    private var logoUri: String? = null  // Para armazenar o caminho da logo

    private val REQUEST_CODE_PERMISSION_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dados_da_empresa)

        dbHelper = DatabaseHelper(this)

        // Inicializa os campos
        editTextNomeOficina = findViewById(R.id.editText_nome_oficina)
        editTextTelefone = findViewById(R.id.editText_telefone)
        editTextEndereco = findViewById(R.id.editText_endereco)
        editTextEmail = findViewById(R.id.editText_email)
        editTextOutrasInformacoes = findViewById(R.id.editText_outras_informacoes)
        buttonSalvarDados = findViewById(R.id.button_salvar_dados)
        buttonUploadLogo = findViewById(R.id.button_upload_logo)

        // Desabilita o botão salvar até os campos obrigatórios serem preenchidos
        buttonSalvarDados.isEnabled = false

        // Adiciona listeners para habilitar/desabilitar o botão salvar
        addTextWatchers()

        // Botão para upload da logo
        buttonUploadLogo.setOnClickListener {
            if (checkStoragePermission()) {
                abrirGaleria() // Abre a galeria se a permissão já foi concedida
            } else {
                requestStoragePermission() // Solicita a permissão se ainda não foi concedida
            }
        }

        // Botão para salvar os dados
        buttonSalvarDados.setOnClickListener {
            salvarDados()
        }

        // Carregar dados existentes
        carregarDadosEmpresa()

        val buttonDeletarDados: Button = findViewById(R.id.button_deletar_dados)

        buttonDeletarDados.setOnClickListener {
            confirmarDelecao()
        }


    }

    // Função que exibe a caixa de diálogo de confirmação
    private fun confirmarDelecao() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Confirmar Exclusão")
        alertDialog.setMessage("Tem certeza que deseja deletar todos os dados e a imagem da oficina?")

        // Botão de confirmar exclusão
        alertDialog.setPositiveButton("Sim") { _, _ ->
            deletarDados()  // Chama a função para deletar os dados
        }

        // Botão de cancelar a exclusão
        alertDialog.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()  // Fecha o diálogo sem deletar nada
        }

        alertDialog.show()  // Exibe o diálogo
    }

    // Função para deletar os dados
    private fun deletarDados() {
        dbHelper.deleteDadosEmpresa()  // Aqui você chama a função que deleta os dados da empresa
        Toast.makeText(this, "Dados deletados com sucesso!", Toast.LENGTH_SHORT).show()
        limparCampos()  // Limpa os campos após a exclusão
    }

    // Função para limpar os campos do formulário
    private fun limparCampos() {
        editTextNomeOficina.text.clear()
        editTextTelefone.text.clear()
        editTextEndereco.text.clear()
        editTextEmail.text.clear()
        editTextOutrasInformacoes.text.clear()
        // Limpar a imagem da logo
        val imageViewLogo: ImageView = findViewById(R.id.logo_image_view)
        imageViewLogo.setImageDrawable(null) // Remove a imagem
    }

    // Função para salvar os dados no banco de dados
    private fun salvarDados() {
        val nomeOficina = editTextNomeOficina.text.toString().trim()
        val telefone = editTextTelefone.text.toString().trim()
        val endereco = editTextEndereco.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val outrasInformacoes = editTextOutrasInformacoes.text.toString().trim()

        dbHelper.saveDadosEmpresa(nomeOficina, telefone, endereco, email, logoUri, outrasInformacoes)
        Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
    }

    // Função para carregar dados já salvos
    private fun carregarDadosEmpresa() {
        val cursor: Cursor? = dbHelper.getDadosEmpresa()

        cursor?.let {
            if (it.moveToFirst()) {
                editTextNomeOficina.setText(it.getString(it.getColumnIndex("nome_oficina")))
                editTextTelefone.setText(it.getString(it.getColumnIndex("telefone")))
                editTextEndereco.setText(it.getString(it.getColumnIndex("endereco")))
                editTextEmail.setText(it.getString(it.getColumnIndex("email")))
                editTextOutrasInformacoes.setText(it.getString(it.getColumnIndex("outras_informacoes")))
                logoUri = it.getString(it.getColumnIndex("logo_uri"))

                // Carregar a logo se existir
                logoUri?.let { uriString ->
                    val bitmap = BitmapFactory.decodeFile(uriString)
                    findViewById<ImageView>(R.id.logo_image_view).setImageBitmap(bitmap)
                }
            }
            it.close() // Certifique-se de fechar o cursor
        }
    }

    // Função para monitorar as mudanças de texto nos campos obrigatórios
    private fun addTextWatchers() {
        val textFields = listOf(editTextNomeOficina, editTextTelefone, editTextEndereco, editTextEmail)

        textFields.forEach { field ->
            field.addTextChangedListener {
                // Habilitar o botão se todos os campos obrigatórios estiverem preenchidos
                buttonSalvarDados.isEnabled = areRequiredFieldsFilled()
            }
        }
    }

    // Função para verificar se os campos obrigatórios estão preenchidos
    private fun areRequiredFieldsFilled(): Boolean {
        return editTextNomeOficina.text.toString().isNotEmpty() &&
                editTextTelefone.text.toString().isNotEmpty() &&
                editTextEndereco.text.toString().isNotEmpty() &&
                editTextEmail.text.toString().isNotEmpty()
    }

    // Função para verificar se a permissão já foi concedida
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 ou superior
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 ou inferior
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Função para solicitar permissão de acesso ao armazenamento
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Solicita permissão para ler imagens no Android 13+
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_PERMISSION_STORAGE
            )
        } else {
            // Solicita permissão para ler armazenamento externo no Android 12 ou inferior
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION_STORAGE
            )
        }
    }

    // Função para abrir a galeria se a permissão foi concedida
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    // Função para capturar o resultado do upload da logo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri = data.data!!
            logoUri = getRealPathFromURI(selectedImage)

            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            findViewById<ImageView>(R.id.logo_image_view).setImageBitmap(bitmap)
        }
    }

    // Função para obter o caminho real da imagem selecionada
    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
                path = it.getString(index)
            }
            it.close()
        }
        return path
    }

    // Resultado da solicitação de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                abrirGaleria() // Permissão concedida, abre a galeria
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
