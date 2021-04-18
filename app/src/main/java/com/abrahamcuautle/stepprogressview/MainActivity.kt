package com.abrahamcuautle.stepprogressview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var position: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            findViewById<StepProgressView>(R.id.step_progress_view).clear()
        }
        findViewById<Button>(R.id.btn_edt).setOnClickListener {
            try { position = findViewById<EditText>(R.id.edt).text.toString().toInt() } catch (e: Exception) { }
            findViewById<StepProgressView>(R.id.step_progress_view).setStepPosition(position)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("POSITION", position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        position = savedInstanceState.getInt("POSITION", 0)
        findViewById<StepProgressView>(R.id.step_progress_view).setStepPosition(position)
    }
}