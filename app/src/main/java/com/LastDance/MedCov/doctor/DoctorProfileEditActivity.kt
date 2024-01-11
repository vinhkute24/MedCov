package com.LastDance.MedCov.doctor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.LastDance.MedCov.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import com.LastDance.MedCov.doctor.models.DoctorData

class DoctorProfileEditActivity : AppCompatActivity() {
    lateinit var edtDName: EditText
    lateinit var edtDSurname: EditText
    lateinit var edtDAge: EditText
    lateinit var edtOldPassword: EditText
    lateinit var edtNewPassword: EditText
    lateinit var spinnerField: Spinner
    lateinit var btnSaveChanges: Button
    lateinit var imgDoctorProfile: ImageView

    lateinit var downloadUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile_edit)

        edtDName = findViewById(R.id.editDName)
        edtDSurname = findViewById(R.id.editDSurname)
        edtDAge = findViewById(R.id.editDAge)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        spinnerField = findViewById(R.id.spinnerField)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgDoctorProfile = findViewById(R.id.imgDoctorProfilePicture)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser


        db.collection("doctors")
            .document(user?.email!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val doctorData = document.toObject(DoctorData::class.java)

                    edtDName.setText(doctorData?.first)
                    edtDSurname.setText(doctorData?.last)
                    edtDAge.setText(doctorData?.age)

                    val specialties = resources.getStringArray(R.array.doctor_specialties)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerField.adapter = adapter
                    val selectedIndex = specialties.indexOf(doctorData?.field)
                    spinnerField.setSelection(selectedIndex)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctor data: ${e.message}", e)
            }

        btnSaveChanges.setOnClickListener {
            if (edtDName.text.isNotEmpty() &&
                edtDSurname.text.isNotEmpty() &&
                edtDAge.text.isNotEmpty() &&
                edtOldPassword.text.isNotEmpty() &&
                edtNewPassword.text.isNotEmpty()
            ) {

                AlertDialog.Builder(this).apply {
                    setTitle("Approval")
                    setMessage("Do you want to update?")
                    setPositiveButton("Yes") { _, _ ->
                        val oldPassword = edtOldPassword.text.toString()
                        val newPassword = edtNewPassword.text.toString()
                        val name = edtDName.text.toString()
                        val surname = edtDSurname.text.toString()
                        val age = edtDAge.text.toString()
                        val field = spinnerField.selectedItem.toString()

                        // Eski şifreyi kontrol edin ve güncelleme işlemi yapın
                        verifyAndUpdate(
                            oldPassword,
                            newPassword,
                            name,
                            surname,
                            age,
                            field,
                            user?.email!!,
                            downloadUri.toString()
                        )

                        Handler().postDelayed({
                            val intent = Intent(
                                this@DoctorProfileEditActivity,
                                DoctorProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }, 2000)
                    }
                    setNegativeButton("No", null)
                }.create().show()
            } else {
                Toast.makeText(this, "Please fill in the information completely", Toast.LENGTH_LONG)
                    .show()
            }
        }

        imgDoctorProfile.setOnClickListener {
            openGallery()
        }
    }

    private val READ_EXTERNAL_STORAGE_PERMISSION = 123
    private val PICK_IMAGE_REQUEST = 123
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery access denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data


            Glide.with(this).load(selectedImageUri).into(imgDoctorProfile)


            val user = FirebaseAuth.getInstance().currentUser
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("users/${user?.email}/profile.jpg")

            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }

                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result

                } else {

                }
            }
        }
    }

    private fun verifyAndUpdate(
        oldPassword: String,
        newPassword: String,
        first: String,
        last: String,
        age: String,
        field: String,
        email: String,
        image: String?
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {

                        updateDoctorInFirestore(
                            user.uid,
                            first,
                            last,
                            age,
                            field,
                            email,
                            newPassword,
                            image
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Could not update password: ${updateTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "The old password is incorrect: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateDoctorInFirestore(
        userId: String,
        first: String,
        last: String,
        age: String,
        field: String,
        email: String,
        password: String,
        image: String?
    ) {
        val db = FirebaseFirestore.getInstance()

        val doctorDataInfo = DoctorData(
            UID = userId,
            first = first,
            last = last,
            age = age,
            field = field,
            email = email,
            password = password,
            image = image
        )

        db.collection("doctors")
            .document(email)
            .set(doctorDataInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "An error occurred while updating information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
