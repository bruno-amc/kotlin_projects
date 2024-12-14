package com.example.expense_control_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_control_screen)

        // Navegação para tela de lista de controle de despesas
     //   val btn_screen_list_expenses: Button = findViewById(R.id.buttonListExpenseControl)
     //   btn_screen_list_expenses.setOnClickListener {
     //           val intent = Intent(this, ExpenseControlScreen::class.java)
      //      startActivity(intent)
      //  }

        // Navegação para tela de configurações
        //val btn_screen_configuration: Button = findViewById(R.id.buttonConfiguration)
      //  btn_screen_configuration.setOnClickListener {
        //    val intent = Intent(this, ConfigurationActivity::class.java)
        //    startActivity(intent)
       // }

    }
}