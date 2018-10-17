package com.example.jong.loginactivity1

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // Firebase Authentication 관리 클래스
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase 로그인 통합 관리하는 Object 만들기
        auth = FirebaseAuth.getInstance()



        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 구글 로그인에 접근할 수 있는 인증키
            .requestEmail()
            .build()  // build() 조립완성

        // 구글 로그인 클래스를 만들
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        email_login_button.setOnClickListener {
            createAndLoginEmail()
        }

        google_sign_in_button.setOnClickListener {
            googleLogin()
        }
    }


    // 이메일 회원 가입 및 로그인 메소드
    fun createAndLoginEmail() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 아이디 생성이 성공했을 경우
                    Toast.makeText(this, getString(R.string.signup_complete), Toast.LENGTH_LONG).show()
                } else if (task.exception?.message.isNullOrEmpty()) {  //isNullOrEmpty()란 해당 객체가 null이거나 빈 객체라면 false를 반환
                    // 회원 가입 에러가 발생했을 경우
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    signinEmail()
                }
            }
    }

    // 로그인 메소드
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공 및 다음 페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {

                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {

        // User is signed in
        if (user != null) {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) // 구글에서 성공된 데이터 가 넘어옴

            if (result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //다음페이지 호출
                moveMainPage(auth?.currentUser)
            }
        }

    }

}
