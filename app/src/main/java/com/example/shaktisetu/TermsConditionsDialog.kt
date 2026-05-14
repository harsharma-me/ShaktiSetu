package com.example.shaktisetu

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class TermsConditionsDialog(

    context: Context,

    private val onAgree: () -> Unit

) : Dialog(
    context,
    R.style.DialogTheme
) {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.dialog_terms_conditions
        )

        setupDialogWindow()

        initializeViews()
    }

    // =========================
    // WINDOW SETUP
    // =========================

    private fun setupDialogWindow() {

        window?.setLayout(

            (
                    context.resources
                        .displayMetrics
                        .widthPixels * 0.94
                    ).toInt(),

            (
                    context.resources
                        .displayMetrics
                        .heightPixels * 0.88
                    ).toInt()
        )

        setCanceledOnTouchOutside(false)

        setCancelable(false)
    }

    // =========================
    // INITIALIZE
    // =========================

    private fun initializeViews() {

        val tvTitle =
            findViewById<TextView>(
                R.id.tvTitle
            )

        val tvTermsContent =
            findViewById<TextView>(
                R.id.tvTermsContent
            )

        val btnAgree =
            findViewById<Button>(
                R.id.btnAgree
            )

        val btnDisagree =
            findViewById<Button>(
                R.id.btnDisagree
            )

        tvTitle.text =
            "📋 Terms & Conditions"

        tvTermsContent.text =
            loadTermsContent()

        btnAgree.setOnClickListener {

            Toast.makeText(
                context,
                "✅ Terms accepted!",
                Toast.LENGTH_SHORT
            ).show()

            onAgree.invoke()

            dismiss()
        }

        btnDisagree.setOnClickListener {

            Toast.makeText(
                context,
                "❌ You must agree to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================
    // TERMS CONTENT
    // =========================

    private fun loadTermsContent():
            String {

        return """
TERMS AND CONDITIONS

Last Updated: April 2026

1. ACCEPTANCE OF TERMS
By using ShaktiSetu application ("App"), you agree to these Terms and Conditions.

2. USE LICENSE
Permission is granted for personal, non-commercial use only.

You may not:
• Modify or copy materials
• Reverse engineer the application
• Use for commercial purposes
• Remove copyright notices

3. DISCLAIMER
ShaktiSetu is provided "as is" without warranties of any kind.

4. LIMITATION OF LIABILITY
ShaktiSetu shall not be liable for damages arising from use or inability to use the App.

5. USER RESPONSIBILITY
Users are responsible for account security and emergency usage decisions.

6. PRIVACY
Some emergency features may use:
• Location
• SMS
• Camera
• Microphone

Only when permissions are granted.

7. EMERGENCY FEATURES
ShaktiSetu is an assistive safety tool and does not guarantee emergency response.

8. MODIFICATIONS
Terms may be updated without prior notice.

9. GOVERNING LAW
Governed under the laws of India.

10. CONTACT
support@shaktisetu.com

By continuing, you acknowledge that you have read and accepted these Terms and Conditions.
        """.trimIndent()
    }
}