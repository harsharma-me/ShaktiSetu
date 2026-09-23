package com.valeno.shaktisetu

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

    private fun loadTermsContent(): String {
        return """
VALENO / SHAKTISETU
Terms & Conditions

The terms that govern your access to and use of the ShaktiSetu safety platform.
• Effective: September 22, 2026
• Updated: September 22, 2026

IMPORTANT EMERGENCY-SERVICE NOTICE
ShaktiSetu is a technology-based assistance tool and does not replace official emergency services or guarantee emergency response.

01 Introduction
These Terms & Conditions (“Terms”) govern your use of the ShaktiSetu application operated by Valeno (“Valeno”, “we”, “us”, or “our”). By installing, accessing or using ShaktiSetu, you agree to these Terms. If you do not agree with these Terms, you should not use the application.

02 Acceptance of Terms
Your access to and use of ShaktiSetu is subject to these Terms and our Privacy Policy. By continuing to use the application after the Terms are updated, you acknowledge the revised Terms where permitted by applicable law.

03 About ShaktiSetu
ShaktiSetu is a technology-based women-safety and emergency-assistance application. Depending on the version of the application and enabled features, ShaktiSetu may provide:
• SOS alerts
• Live location sharing
• Emergency contacts
• Emergency SMS and calling assistance
• Fake call functionality
• Alarm and siren functionality
• Voice-trigger functionality
• Offline SMS fallback
• Danger-zone alerts
• Audio, photo and video evidence recording
• PIN-protected SOS dismissal

04 Eligibility & Accounts
You must provide accurate information when creating or using a ShaktiSetu account. You are responsible for maintaining the security of your account and for activity performed through your account. You must not intentionally provide false information or use another person’s account without authorization.

05 Lawful Use
You agree to use ShaktiSetu only for lawful purposes and in accordance with applicable laws. Safety features must be used responsibly and for their intended purpose.

06 Prohibited Activities
You must not:
• Intentionally misuse SOS or emergency features
• Submit false emergency reports for malicious purposes
• Attempt to access another user's account
• Attempt to bypass security controls
• Disrupt or interfere with the service
• Upload unlawful or harmful content
• Use the application to harass or threaten another person
• Violate another person's privacy or legal rights
• Use the application for fraudulent or abusive purposes

07 SOS & Emergency Features
SOS and related features are intended to assist users during potentially unsafe situations. Depending on the feature activated, ShaktiSetu may attempt to:
• Share location
• Send emergency messages
• Assist with calls
• Activate an alarm
• Record evidence

No guaranteed emergency response: The successful operation of an SOS feature can depend on network connectivity, GPS availability, battery level, device settings, permissions, operating-system restrictions, mobile carrier services and third-party systems.

08 Emergency Contacts
You are responsible for ensuring that the emergency contacts you add are accurate and appropriate. You should inform your emergency contacts that they may receive alerts or communications from ShaktiSetu. Valeno is not responsible for the actions, availability or response of emergency contacts.

09 Location Sharing
Location sharing occurs as part of applicable features and permissions that you enable. Location information may not always be accurate or available. You should not rely solely on ShaktiSetu's location information during an emergency.

10 Evidence Recording
If you use audio, video or photo recording features, you are responsible for using them in accordance with applicable laws. You must not use ShaktiSetu to unlawfully record, distribute, threaten, harass or violate the privacy or rights of another person.

11 Third-Party Services
ShaktiSetu may depend on third-party services such as authentication providers, cloud infrastructure, mapping services, mobile networks, SMS services and device operating-system functionality. Third-party services may experience outages, limitations, changes or failures outside our control.

12 Service Availability
We aim to keep ShaktiSetu available and reliable, but we do not guarantee uninterrupted or error-free operation. The application may become temporarily unavailable because of maintenance, technical failures, network issues, security incidents, third-party outages or circumstances outside our reasonable control.

13 Account Suspension & Termination
We may suspend or terminate an account where reasonably necessary to address:
• Abuse
• Fraud
• Security threats
• Violation of these Terms
• Illegal activity
• Misuse of emergency features

Users may request account deletion through the ShaktiSetu account-deletion process.

14 Intellectual Property
The ShaktiSetu application, branding, logos, designs, software, text, graphics and other original materials are owned by or licensed to Valeno unless otherwise stated. You may not copy, modify, distribute, sell, reverse engineer or commercially exploit these materials except where permitted by applicable law or with appropriate authorization.

15 User Content
You retain rights you may have in content you provide through the application. You grant Valeno the limited permissions necessary to process, store, transmit and display that content solely for providing the functionality you requested and operating the service.

16 Privacy
Your use of ShaktiSetu is also governed by our Privacy Policy, which explains how personal information is processed, stored, protected and deleted.

17 Disclaimer
Important: ShaktiSetu is a technology-based assistance service. It is not a replacement for police, ambulance, medical services, emergency responders or official emergency services. We do not guarantee emergency response, successful communication, exact location information, uninterrupted availability or successful delivery of alerts.

18 Limitation of Liability
To the maximum extent permitted by applicable law, Valeno will not be responsible for failures caused by circumstances outside its reasonable control. These circumstances may include network outages, GPS limitations, device failures, carrier failures, battery depletion, operating-system restrictions and third-party service failures. Nothing in these Terms is intended to exclude liability that cannot legally be excluded.

19 Changes to Terms
We may update these Terms from time to time when our services, technology, legal requirements or business practices change. The latest version will be made available through the application or our website.

20 Governing Law
These Terms shall be governed by the laws applicable in India, subject to applicable consumer-protection and other mandatory legal rights.

21 Contact
If you have questions regarding these Terms, contact:
• Developer / Company: Valeno
• Product: ShaktiSetu
• Legal Owner / Proprietor: Harsh
• Contact Email: hello.valeno@gmail.com
        """.trimIndent()
    }
}