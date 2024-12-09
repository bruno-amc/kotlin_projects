package com.example.expense_control_app

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

    }
}