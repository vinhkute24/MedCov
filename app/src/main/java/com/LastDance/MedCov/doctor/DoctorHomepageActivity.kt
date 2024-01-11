package com.LastDance.MedCov.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.LastDance.MedCov.MainActivity
import com.LastDance.MedCov.NewsActivity
import com.LastDance.MedCov.R

import com.LastDance.MedCov.doctor.adapter.DoctorAppointmentAdapter
import com.LastDance.MedCov.doctor.services.DoctorAppointmentService

class DoctorHomepageActivity : AppCompatActivity() {
    lateinit var appointmentsList: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_homepage)

        appointmentsList = findViewById(R.id.appointmentsList)

        val doctorAppointmentService = DoctorAppointmentService()
        val doctorEmail =
            FirebaseAuth.getInstance().currentUser?.email


        doctorAppointmentService.getAppointmentsForDoctor(doctorEmail!!) { appointments ->
            Log.d("appointments", appointments.toString())
            val adapter = DoctorAppointmentAdapter(this, appointments)
            appointmentsList.adapter = adapter

            appointmentsList.setOnItemLongClickListener { _, _, position, _ ->
                val selectedAppointment = appointments[position]

                AlertDialog.Builder(this)
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want to cancel this appointment?")
                    .setPositiveButton("Evet") { _, _ ->

                        doctorAppointmentService.deleteAppointment(
                            doctorEmail,
                            selectedAppointment.patientEmail!!,
                            selectedAppointment.id!!
                        ) { success ->
                            if (success) {
                                Toast.makeText(
                                    this,
                                    "The appointment has been deleted successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                doctorAppointmentService.getAppointmentsForDoctor(doctorEmail) { updatedAppointments ->
                                    adapter.clear()
                                    adapter.addAll(updatedAppointments)
                                    adapter.notifyDataSetChanged()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "An error occurred while deleting the appointment.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()

                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.doctor_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doctor_profile -> {
                var intent = Intent(this, DoctorProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.doctor_news -> {
                var intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
            }
            R.id.doctor_logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Log out of account")
                    setMessage("Are you sure you want to log out?")
                    setPositiveButton("Yes") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    setNegativeButton("No", null)

                }.create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

