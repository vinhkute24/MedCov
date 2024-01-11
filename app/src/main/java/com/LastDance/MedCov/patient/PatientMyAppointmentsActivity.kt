package com.LastDance.MedCov.patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.LastDance.MedCov.R
import com.google.firebase.auth.FirebaseAuth

import com.LastDance.MedCov.patient.adapter.PatientAppointmentAdapter
import com.LastDance.MedCov.patient.models.PatientAppointmentData
import com.LastDance.MedCov.patient.services.PatientAppointmentService

class PatientMyAppointmentsActivity : AppCompatActivity() {
    lateinit var patientAppointmentsList : ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_my_appointments)

        patientAppointmentsList = findViewById(R.id.patientAppointmentsList)

        val patientAppointmentService = PatientAppointmentService()
        val patientEmail = FirebaseAuth.getInstance().currentUser?.email

        patientAppointmentService.getAppointmentsForPatient(patientEmail!!) { appointments ->
            val adapter = PatientAppointmentAdapter(this,appointments)
            patientAppointmentsList.adapter = adapter
        }

        patientAppointmentsList.setOnItemLongClickListener { adapterView, _, i, _ ->
            val selectedAppointment = adapterView.getItemAtPosition(i) as PatientAppointmentData
            AlertDialog.Builder(this).apply {
                setTitle("Cancel Appointment")
                setMessage("Are you sure you want to cancel the appointment?")
                setPositiveButton("Yes") { _, _ ->
                    patientAppointmentService.deleteAppointment(
                        patientEmail,
                        selectedAppointment.doctorEmail!!,
                        selectedAppointment.id!!
                    ) { success ->
                        if (success) {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "Appointment canceled", Toast.LENGTH_SHORT).show()

                            patientAppointmentService.getAppointmentsForPatient(patientEmail) { updatedAppointments ->
                                val newAdapter = PatientAppointmentAdapter(this@PatientMyAppointmentsActivity, updatedAppointments)
                                patientAppointmentsList.adapter = newAdapter
                            }
                        } else {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                setNegativeButton("No", null)
            }.create().show()
            true
        }

    }
}

