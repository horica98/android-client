package com.example.android_app

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_code.*

class CodeActivity: AppCompatActivity() {
    lateinit var googleText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        googleText = intent.getStringExtra("googleText")!!
        editText.setText(intent.getStringExtra("text"))

        compare.setOnClickListener {
            showGoogleResponseDialog()
        }
        compile.setOnClickListener {
            compile()
        }
    }

    private fun compile() {
        Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
    }

    private fun showGoogleResponseDialog() {
        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.google_dialog)
        dialog.findViewById<TextView>(R.id.googleText).text = googleText
        dialog.show()
    }
}