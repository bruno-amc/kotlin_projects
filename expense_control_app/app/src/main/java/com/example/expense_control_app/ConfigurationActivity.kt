package com.example.expense_control_app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConfigurationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuration)

        // Navegação para tela de tipos de despesas
        val btn_create_expense_type: Button = findViewById(R.id.buttonCreateExpenseType)
        btn_create_expense_type.setOnClickListener {
            val intent = Intent(this, ExpenseTypeActivity::class.java)
            startActivity(intent)
        }

        val buttonOperationManual = findViewById<Button>(R.id.buttonOperationManual)
        buttonOperationManual.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Manual")
            builder.setMessage("Na tela principal clique "+
            "no ícone das configurações para "+
            "configurar uma categoria de despesas. \n" +
                    "A inserção de novas despesas "+
            "deve receber o valor e categoria. "+
            "O campo observação é opcional. "+
            "O usuário pode escolher a data e hora manualmente. "+
                    "Caso os campos de data e hora fiquem vazios, "+
            "o aplicativo utilizará a data/hora do momento da inserção "+
            "da despesa, dessa forma o preenchimento deles é opcional. "+
            "O preenchimento da HORA só é liberado após o preenchimento da DATA.")

            builder.setPositiveButton("OK", null)
            builder.show()
        }

    }
}