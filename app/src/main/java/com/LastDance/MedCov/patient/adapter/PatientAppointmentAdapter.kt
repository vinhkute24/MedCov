package com.LastDance.MedCov.patient.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.LastDance.MedCov.R
import com.bumptech.glide.Glide

import com.LastDance.MedCov.patient.models.PatientAppointmentData

class PatientAppointmentAdapter(private val context: Activity, private val list:List<PatientAppointmentData>) : ArrayAdapter<PatientAppointmentData>(context,
    R.layout.custom_appointment, list)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = context.layoutInflater.inflate(R.layout.custom_appointment,null,true)
        val r_appName = rootView.findViewById<TextView>(R.id.r_appPName)
        val r_appDate = rootView.findViewById<TextView>(R.id.r_appPDate)
        val r_appHour = rootView.findViewById<TextView>(R.id.r_appPHour)
        val r_appNote = rootView.findViewById<TextView>(R.id.r_appPNote)
        val r_appField = rootView.findViewById<TextView>(R.id.r_appPField)
        val r_appImg = rootView.findViewById<ImageView>(R.id.r_appPImg)

        val appointment = list.get(position)
        r_appName.text = appointment.doctorName
        r_appDate.text = "Hour : " + appointment.date
        r_appHour.text = "Date : " + appointment.hour
        r_appNote.text = "Note : " + appointment.note
        r_appField.text = appointment.doctorField
        Glide.with(context).load(appointment.doctorImg).into(r_appImg)


        return rootView
    }

}