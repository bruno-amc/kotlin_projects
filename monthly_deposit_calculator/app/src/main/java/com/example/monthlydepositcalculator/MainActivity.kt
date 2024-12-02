package com.example.monthlydepositcalculator


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navegação para tela de depósitos mensais sem aporte inicial
        val btn_screen_montly_deposit: Button = findViewById(R.id.buttonMontlyDeposit)
        btn_screen_montly_deposit.setOnClickListener {
            val intent = Intent(this, screen_montly_deposit::class.java)
            startActivity(intent)
        }

        // Navegação para tela de depósitos mensais COM aporte inicial
        val btn_screen_montly_deposit_with_init_deposit: Button = findViewById(R.id.buttonDepositWithInitialAmount)
        btn_screen_montly_deposit_with_init_deposit.setOnClickListener {
            val intent = Intent(this, screen_with_initial_deposit::class.java)
            startActivity(intent)
        }



    }
}
