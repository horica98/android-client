package com.example.android_app

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.android_app.models.CompilationText
import com.example.android_app.networking.RestClient
import com.google.android.material.card.MaterialCardView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_code.*

class CodeActivity: AppCompatActivity() {
    lateinit var googleText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        googleText = intent.getStringExtra("googleText")!!
        editText.setText(intent.getStringExtra("text"))
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        compare.setOnClickListener {
            showGoogleResponseDialog()
        }
        compile.setOnClickListener {
            compile()
        }

        copy.setOnClickListener {
            val clip = ClipData.newPlainText("RANDOM UUID", editText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("CheckResult")
    private fun compile() {
        RestClient.service.compile(CompilationText(editText.text.toString()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d("gigi", result.toString())
                showCompilationDialog(result.text)
            }
                ,
                { throwable -> Log.d("gigi", "Pl: " + throwable.message!!) })


    }

        private fun showGoogleResponseDialog() {
        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.google_dialog)
        dialog.findViewById<TextView>(R.id.googleText).text = googleText
        dialog.show()
    }

    private fun showCompilationDialog(text: String) {
        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.compilation_dialog)
        dialog.findViewById<TextView>(R.id.compilationText).text = text
        dialog.show()
    }
}