package com.example.jong.loginactivity1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.jong.loginactivity1.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 앨범을 여는 코드
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)


        addphoto_image.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {  // 사진 선택했을떄
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            } else if(resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }

    fun contentUpload() {

        val timeStamp = SimpleDateFormat("yyyyMMDD_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp +"_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {taskSnapshot ->
            // 사진이 올라가고 나서 다시 데이터베이스에 넣는 부분

            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()

            // 업로드된 이미지 주소
            var uri = taskSnapshot.downloadUrl

            var contentDTO = ContentDTO()

            // 이미지 주소
            contentDTO.imageUrl = uri!!.toString()

            // 유저의 UDI
            contentDTO.uid = auth?.currentUser?.uid

            // 게시물의 설명
            contentDTO.explain = addphoto_edit_explanin.text.toString()

            // 유저의 아이디
            contentDTO.userId = auth?.currentUser?.email

            // 게시물 업로드 시간
            contentDTO.timestamp = System.currentTimeMillis()

            // 데이터베이스 저장
            // 컬렉션은 일종의 경로 images라는 테이블
            // document() or docuemnt(name)
            firestore?.collection("images")?.document()?.set(contentDTO)
                ?.addOnCompleteListener {
                task ->
                println(task.isSuccessful)
            }?.addOnFailureListener {
                exception ->
                println("에러메세지"  + exception.message.toString())
            }

            setResult(Activity.RESULT_OK)

            finish()

        }

    }
}
