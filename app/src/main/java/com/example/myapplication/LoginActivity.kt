package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.myapplication.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding


    // private val quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
    private var session: String = ""
    private var csrf_token: String = ""
    private var server: String = ""

    private val file_server: String = "file_server.txt"
    private val file_array = arrayOf(file_server)
    private var fileInputStream: FileInputStream? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val Textusername: TextView = findViewById(R.id.usernameLoginText)
        val Textpassword: TextView = findViewById(R.id.passwordText)
        val Textserver: TextView = findViewById(R.id.serverText)

        // resources
        /*
        val bundle = intent.extras
        if (bundle != null){
            // Log.d("sbm_login", "Login Activity data received: ${bundle.getString("session")} and ${bundle.getString("csrf_token")}")
            server = bundle.getString("server").toString()
            Log.d("sbs_login_server", "server: $server")
            if (server.isNotEmpty()  ){
                Textserver.setText(server)
            }
        }
        */

        localDataLoad()
        if (server.isNotEmpty()) {
            Textserver.text = server
        }

        /*val buttonAlgo: Button = findViewById(R.id.algoButton)

        buttonAlgo.setOnClickListener {
            server = Textserver.text.toString()
            localDataDump()
        }
        */

        // events
        buttonLogin.setOnClickListener {

            login(
                Textusername.text.toString(),
                Textpassword.text.toString(),
                Textserver.text.toString()
            )
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun login(username: String, password: String, server: String) {
        Log.d("sbm_login", "Login launched, username: ${username}, server: $server")
        val save_session_checkBox: CheckBox = findViewById(R.id.save_session_checkBox)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiProvider.initialize(server)
                val quotesApi = ApiProvider.api

                csrf_token = quotesApi.getCookies().cookie
                val logIn = quotesApi.login(
                    "csrf_token=$csrf_token",
                    username,
                    inputPassword = password,
                    csrf_token
                )
                if (logIn.status) {
                    session = logIn.session

                    Log.d("sbm_login: ", "Session init ok")
                    Log.d("sbm_login: ", "User session:        $session")
                    Log.d("sbm_login: ", "User csrf_token: $csrf_token")
                    Log.d("sbm_login: ", "User save session: $save_session_checkBox.isChecked")
                    val data = Intent()
                    data.putExtra("session", session)
                    data.putExtra("csrf_token", csrf_token)
                    data.putExtra("server", server)
                    if (save_session_checkBox.isChecked) {
                        data.putExtra("saveSession", "yes")
                    } else {
                        data.putExtra("saveSession", "no")
                    }

                    localDataDump()


                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    Log.e("sbm_login", "Login error")
                    runOnUiThread {
                        showToast(resources.getString(R.string.loginError))
                    }
                }
            } catch (Ex: Exception) {
                runOnUiThread {
                    showToast(resources.getString(R.string.loginError) + ": " + Ex.message + " server ${RetrofitHelper.getBaseUrl()}")
                }
                Log.e("sbm_login: ", "Error on connection: ${Ex.message}")
            }
        }
    }


    private fun localDataLoad(): Boolean {
        Log.d("sbm_localDataLoad", "read local data")
        var status = true

        try {
            for (file in file_array) {
                fileInputStream = openFileInput(file)
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder: StringBuilder = StringBuilder()
                var text: String?

                while (run {
                        text = bufferedReader.readLine()
                        text
                    } != null) {
                    stringBuilder.append(text)
                }
                if (file == "file_server.txt") {
                    server = stringBuilder.toString()
                    Log.d("sbs_login_server", "Reading file_server.txt, server: $server")
                }
            }

        } catch (e: Exception) {
            Log.e("sbs_login_server", "No data file found, firsts start app ? creating new one :-), $e")
            status = false
            for (file in file_array) {
                val fileOutputStream = openFileOutput(file, MODE_PRIVATE)
                fileOutputStream.close()
            }
        }
        return status
    }

    private fun localDataDump(): Boolean {
        var status = true
        Log.d("sbs_login_server", "write local data, server: $server")
        try {
            for (file in file_array) {

                val fileOutputStream = openFileOutput(file, MODE_PRIVATE)

                if (file == "file_server.txt") {
                    fileOutputStream.write(server.toByteArray())
                }
                fileOutputStream.close()
                Log.d("sbs_login_server", "write local file: $file, server: $server")
            }
        } catch (e: Exception) {
            Log.e("sbs_login_server", "Error save data: $e")
            status = false
        }

        return status
    }
}