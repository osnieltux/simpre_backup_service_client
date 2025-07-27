package com.example.myapplication


// import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    // globals vars used
    private var backupsNameArray: ArrayList<String> = ArrayList()
    private var backupsDateArray: ArrayList<String> = ArrayList()
    private var backupsBDArray: ArrayList<String> = ArrayList()
    private var backupsSizeArray: ArrayList<String> = ArrayList()
    private var backUpList: List<BackUpList> = emptyList()
    private lateinit var adapter: MyListAdapter

    // BD names
    // private var BackUpNameServerList: List<BackUpListNameServer> = emptyList()
    private var backupsServerName: ArrayList<String> = ArrayList()

    // private var quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
    private lateinit var quotesApi: QuotesApi

    private var session: String = ""
    private var csrf_token: String = ""
    private var server: String = ""
    // private var saveSession: Boolean = false

    // private var fileInputStream: FileInputStream? = null
    private val saveFolder =
        File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sbm")

    //private val file_sessionID:String = "file_sessionID.txt"
    //private val file_csrf_token:String = "file_csrf_token.txt"
    //private val file_server:String = "file_server.txt"

    // private val file_array = arrayOf(file_sessionID, file_csrf_token, file_server)

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginActivityClosed(result)
        }

    /*companion object {
        private const val REQUEST_WRITE_PERMISSION = 1001
    }
   */
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        // WindowCompat.setDecorFitsSystemWindows(window, true)
        // enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)


        // my vars
        val addBackupButton: FloatingActionButton = findViewById(R.id.addBackupButton)
        val listBackUpList: ListView = findViewById(R.id.listaBackup)

        adapter = MyListAdapter(
            this,
            backupsBDArray,
            backupsNameArray,
            backupsDateArray,
            backupsSizeArray
        )

        listBackUpList.adapter = adapter

        registerForContextMenu(listBackUpList)

        // elemento lista presionada
        listBackUpList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ -> // value of item that is clicked
                //  AdapterView.OnItemClickListener { _, _, position, _ -> // value of item that is clicked
                // val itemValue = listBackUpList.getItemAtPosition(position) as String
                // Toast the values
                // Toast.makeText(applicationContext,"Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG).show()
                Toast.makeText(this, R.string.textKeepPress, Toast.LENGTH_SHORT).show()
            }

        // add backup
        addBackupButton.setOnClickListener {
            addBackup()
        }

        // var dataLoaded = false
        /*
        // TODO: to use when you can save your session
        if (localDataLoad()){
            if (server.isEmpty()){
                startLoginActivity()
            } else {
                showProgress()
                checkLogin()
            }
        } else {
            startLoginActivity()
        }
        */

        startLoginActivity()

        /*
        quotesApi = Retrofit.Builder()
             .baseUrl("http://127.0.0.1:5000/")
            .client( OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuotesApi::class.java)

         */
        // quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
        quotesApi = ApiProvider.api

        //selectPath("")
        //create_backup("testeador", "versat2565")
        // restoreBackupBackend(132, false, "")
    }

    // creando elementos del menu de contexto
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu!!.setHeaderTitle(R.string.textContextMenuHeader)
        menu.add(0, v!!.id, 0, R.string.textDownload)
        menu.add(1, v.id, 1, R.string.textRestore)
        menu.add(2, v.id, 2, R.string.textDelete)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.updateMenu -> {
                upDateBackupsBackend()
                updateBDnames()
            }

            R.id.logout -> {
                logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        // println("Hola " + item.order + ", elemento :" + info.position)

        if (item.order == 0) {
            // download
            Log.d(
                "SBM",
                "Download action from ContextMenu (onContextItemSelected item.order == 1.)"
            )
            checkLogin()
            downloadBackup(info.position)
        }
        if (item.order == 1) {
            checkLogin()
            restoreBackup(backUpList[info.position].id)
        }
        if (item.order == 2) {
            // delete
            Log.d("SBM", "Delete action from ContextMenu (onContextItemSelected item.order ==2.)")
            checkLogin()
            deleteBackup(info.position)
        }
        return true
    }

    private fun showProgress(show: Boolean = true) {
        val progressLogin: ProgressBar = findViewById(R.id.progressLogin)
        val plusButton: FloatingActionButton = findViewById(R.id.addBackupButton)
        if (show) {
            progressLogin.visibility = View.VISIBLE
            plusButton.visibility = View.GONE

        } else {
            progressLogin.visibility = View.GONE
            plusButton.visibility = View.VISIBLE
        }

    }

    private fun addBackup() {
        Log.d("sbm", "create new backup.")

        val inflater = layoutInflater
        val inflateView = inflater.inflate(R.layout.create, null)

        // populate spiner
        val spinnerBD: Spinner = inflateView.findViewById(R.id.spinnerBD)
        val nameNewBd: EditText = inflateView.findViewById(R.id.nameNewBd)
        val namesAdapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, backupsServerName)
        spinnerBD.adapter = namesAdapter


        val create = AlertDialog.Builder(this)
        create.setTitle(resources.getString(R.string.createNew))
        create.setView(inflateView)
        create.setCancelable(false)
        create.setNegativeButton(resources.getString(R.string.textNo)) { dialog, which ->
            showOkToast(resources.getString(R.string.operationCanceled))
        }
        create.setPositiveButton(resources.getString(R.string.textYes)) { dialog, which ->

            if (nameNewBd.text.isEmpty()) {
                showErrorToast(resources.getString(R.string.createNameValidate))
            } else {
                // call backend
                if (backupsServerName.isNotEmpty()) {
                    create_backup(
                        backup_cname = nameNewBd.text.toString(),
                        backup_name = backupsServerName[spinnerBD.selectedItemPosition]
                    )
                } else {
                    Log.d("sbm", "No bd name.")
                    showErrorToast(resources.getString(R.string.noBD))
                }
            }
        }
        spinnerBD.adapter = namesAdapter
        val dialog = create.create()
        dialog.show()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showOkToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun loginActivityClosed(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Log.d("sbm", "loginActivityClosed launched")
            if (data != null) {
                Log.d("sbm", "loginActivityClosed received data:")
                Log.d("sbm", "session: ${data.extras?.getString("session")}")
                Log.d("sbm", "csrf_token: ${data.extras?.getString("csrf_token")}")
                Log.d("sbm", "server: ${data.extras?.getString("server")}")
                Log.d("sbm", "saveSession: ${data.extras?.getString("saveSession").toString()}")

                session = data.extras?.getString("session").toString()
                csrf_token = data.extras?.getString("csrf_token").toString()
                server = data.extras?.getString("server").toString()

                // saveSession = data.extras?.getString("saveSession").toString().equals("yes")

                // save data
                // localDataDump()
                // update list
                upDateBackupsBackend()
                updateBDnames()

                showProgress(false)
            }
        }
    }

    private fun downloadBackup(id: Int) {
        Log.d("sbm_downloadBackup", "ID recibido: $id")


        val idToDownload = backUpList[id].id
        val nameToDownload = backUpList[id].file

        Log.d("sbm_downloadBackup", "Elemento global $idToDownload $nameToDownload $backUpList")


        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setTitle(resources.getString((R.string.textDownload)))
        mAlertDialog.setMessage(resources.getString((R.string.downloadMessage)))
        // mAlertDialog.setCancelable(false)
        mAlertDialog.setPositiveButton(R.string.textYes) { _, _ ->
            Log.d("sbm_downloadBackup", "launch download backup with id: $idToDownload")
            showProgress(true)
            downloadBackupBackend(idToDownload, nameToDownload)
            // Log.d("sbm_downloadBackupB", "PRUEBA INICIADA lllll"+nameToDownload)
            // prueba(idToDownload, "nameToDownload")
        }
        mAlertDialog.setNegativeButton(R.string.textNo) { _, _ -> showOkToast(resources.getString(R.string.operationCanceled)) }
        //mAlertDialog.create()
        mAlertDialog.show()
    }

    private fun restoreBackup(id: Int) {
        // Log.d("sbm", "Restore launched, backup id: $id")
        val inflater = layoutInflater
        val inflateView = inflater.inflate(R.layout.restore, null)
        var nameFinded = false


        // populate spiner
        val spinnerBD: Spinner = inflateView.findViewById(R.id.spinnerRestoreBD)
        val nameRestore: EditText = inflateView.findViewById(R.id.nameRestore)
        val createNewBD_checkBox: CheckBox = inflateView.findViewById(R.id.createNewBD_checkBox)

        val namesAdapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, backupsServerName)
        spinnerBD.adapter = namesAdapter

        var pos = 0
        var nameBD = ""
        for (element in backUpList) {
            if (element.id == id) {
                nameBD = element.bd
                break
            }
        }

        for (element in backupsServerName) {
            if (element == nameBD) {
                nameFinded = true
                break
            } else {
                pos++
            }
        }

        if (nameFinded) {
            // preselect bd.
            spinnerBD.setSelection(pos)
        } else {
            spinnerBD.setSelection(0)
        }


        // checkbox para crear nuevo
        createNewBD_checkBox.setOnClickListener {
            if (createNewBD_checkBox.isChecked) {
                nameRestore.visibility = View.VISIBLE
                spinnerBD.visibility = View.GONE
            } else {
                nameRestore.visibility = View.GONE
                spinnerBD.visibility = View.VISIBLE
            }

        }
        val create = AlertDialog.Builder(this)
        create.setTitle(resources.getString(R.string.textRestore))
        create.setView(inflateView)
        create.setCancelable(false)
        create.setNegativeButton(resources.getString(R.string.textNo)) { dialog, which ->
            showOkToast(resources.getString(R.string.operationCanceled))
        }

        create.setPositiveButton(resources.getString(R.string.textYes)) { dialog, which ->
            Log.d(
                "sbm", "create new: ${createNewBD_checkBox.isChecked}," +
                        " cname: ${nameRestore.text} " +
                        "bd: ${spinnerBD.selectedItem} " +
                        "id: $id"
            )
            restoreBackupBackend(
                id = id,
                restore_create_new = createNewBD_checkBox.isChecked,
                cname = nameRestore.text.toString()
            )
        }
        //spinnerBD.adapter = namesAdapter
        val dialog = create.create()
        dialog.show()
    }


    private fun create_backup(backup_cname: String, backup_name: String) {
        // backup_name name in bd
        // backup_cname description
        //Log.i("sbm", "Create $backup_cname $backup_name")
        showProgress(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.create_backup(
                    "session=$session",
                    backup_cname = backup_cname, backup_name = backup_name
                )

                runOnUiThread {
                    showProgress(false)
                    if (call.status == "ok") {
                        Log.d("sbm", "Backup $backup_cname $backup_name created")
                        showOkToast(resources.getString(R.string.created))

                    } else {
                        Log.e(
                            "sbm",
                            "Error on create $backup_cname $backup_name, result code: ${call.response}"
                        )
                        showErrorToast(call.response)
                    }
                    upDateBackupsBackend()
                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm", "Create_backup Error:" + Ex.message)
                    showErrorToast("Error " + Ex.message)
                }
            }
        }
    }

    private fun deleteBackup(id: Int) {
        // no se corre riesgo de que la lista backUpList esté vacía, pues la listview está
        // construida por ella
        val idToDelete = backUpList[id].id
        Log.d("sbm", "ID recibido: $id")
        Log.d("sbm", "Elemente global $idToDelete")
        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setTitle(resources.getString((R.string.textDelete)))
        mAlertDialog.setMessage(resources.getString((R.string.deleteMessage)))
        // mAlertDialog.setCancelable(false)
        mAlertDialog.setPositiveButton(R.string.textYes) { _, _ ->
            Log.d("sbm", "launch delete backup with id: $idToDelete")
            deleteBackupBackend(idToDelete)
        }
        mAlertDialog.setNegativeButton(R.string.textNo) { _, _ -> showOkToast(resources.getString(R.string.operationCanceled)) }
        //mAlertDialog.create()
        mAlertDialog.show()
    }

    private fun upDateBackupsBackend() {
        // start thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.getBackups("session=$session")
                // val call = quotesApi.getBackups()
                val bodyResponse = call.body()
                if (call.isSuccessful) {
                    Log.d("sbm", "upDateBackupsBackend response ok")
                    if (bodyResponse != null) {
                        if (bodyResponse.status == "ok") {
                            runOnUiThread {
                                backUpList = bodyResponse.backUpList

                                backupsBDArray.clear()
                                backupsNameArray.clear()
                                backupsDateArray.clear()
                                backupsSizeArray.clear()

                                for (value in backUpList) {
                                    backupsBDArray.add(value.bd)
                                    backupsNameArray.add(value.name)
                                    backupsDateArray.add(value.date)
                                    backupsSizeArray.add(value.size)
                                }
                                adapter.notifyDataSetChanged()
                                showOkToast(resources.getString(R.string.updatedText))
                            }
                        }
                    } else {
                        showErrorToast("Response error requesting data")
                    }
                } else {
                    Log.e("sbm", "Error aaa in call.isSuccessful: ${call.code()}")

                    if (call.code() == 401) {
                        // startLoginActivity()
                        Log.e("sbm", "Error bbb in call.isSuccessful: ${call.code()}")
                        checkLogin()
                    }
                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm", "upDateBackupsBackend Error :" + Ex.message)
                    showErrorToast("Error " + Ex.message)
                }
            }
        }
    }

    private fun updateBDnames() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.getBackupsNames("session=$session")
                val bodyResponse = call.body()
                if (call.isSuccessful) {
                    Log.d("sbm", "updateBDnames response ok")
                    if (bodyResponse != null) {
                        if (bodyResponse.status == "ok") {
                            backupsServerName.clear()
                            for (elementname in bodyResponse.bd_names) {
                                backupsServerName.add(elementname.name)
                            }
                        } else {
                            runOnUiThread {
                                Log.e("sbm", "Error updateBDnames: ${bodyResponse.response}")
                                showErrorToast("Error updateBDnames: ${bodyResponse.response}")
                            }
                        }
                    }
                }
            } catch (Ex: Exception) {
                runOnUiThread {
                    Log.e("sbm", "updateBDnames Error :" + Ex.message)
                    showErrorToast("Error " + Ex.message)
                }
            }
        }
    }

    private fun deleteBackupBackend(id: Int) {
        // start thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.delete(
                    "session=$session",
                    backup_id = id.toString(),
                    csrf_token = csrf_token
                )

                val code = call.code()

                when (code) {
                    200 -> {
                        Log.d("sbm_delete", "Backup $id deleted")
                    }
                    501 -> {
                        Log.e("sbm_delete", "Se necesita refrescar las bd para obtener nuevos ids")

                        // se necesita withContext para mostrar el R.string.Error_id en el hilo principal
                        withContext(Dispatchers.Main) {
                            showErrorToast(resources.getString(R.string.Error_id))
                        }

                    }
                    else -> {
                        Log.e("sbm_delete", "Error on delete $id, result code: $code")
                    }
                }
                runOnUiThread {
                    upDateBackupsBackend()
                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm_delete", "deleteBackupBackend Error:" + Ex.message)
                    showErrorToast("Error " + Ex.message)
                }
            }
        }
    }

    private fun restoreBackupBackend(id: Int, restore_create_new: Boolean, cname: String) {
        // Log.i("sbm", "restoreBackupBackend")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.restore_bd(
                    Cookie = "session=$session",
                    backup_id = id,
                    backup_name = cname,
                    restore_create_new = restore_create_new
                )
                if (call.status == "ok") {
                    Log.d("sbm", "Backup $id restored")
                    runOnUiThread {
                        when (call.response) {
                            "Restored" -> {
                                showOkToast(resources.getString(R.string.restored))
                            }
                            "New BD created" -> {
                                showOkToast(resources.getString(R.string.restoredNew))
                            }
                            else -> {
                                showOkToast(call.response)
                            }
                        }

                    }
                } else if (call.status == "error") {
                    runOnUiThread {
                        Log.e("smb", "Error on restore $id, result code: ${call.response}")
                        if (call.response == "BD already exist") {
                            showErrorToast("${resources.getString(R.string.alreadyExist)} $cname")
                        } else {
                            showErrorToast(call.response)
                        }
                    }
                }

                upDateBackupsBackend()

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm", "restoreBackupBackend Error:" + Ex.message)
                    showErrorToast("Error " + Ex.message)
                }
            }
        }

    }

    /*private fun prueba(id: Int, filename: String){
        Log.d("sbm_downloadBackupB", "PRUEBA INICIADA")
    }*/


    private fun downloadBackupBackend(id: Int, filename: String) {
        Log.d("sbm_downloadBackupB", "download backup backend start")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call =
                    quotesApi.downloadBackup("session=$session", url = "/api/v1/download_bd/$id")

                if (call.isSuccessful) {
                    Log.d("sbm", "Backup $id downloaded")
                    val data = call.body()
                    if (data != null) {
                        var input: InputStream? = null
                        //val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        // val path = Environment.DIRECTORY_DOWNLOADS
                        // val path = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        // val folder = File(path, "sbm")
                        if (!saveFolder.exists()) {
                            saveFolder.mkdirs()
                        }

                        val file = File(saveFolder, filename)

                        try {
                            input = data.byteStream()
                            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
                            val fos = FileOutputStream(file)
                            fos.use { output ->
                                val buffer = ByteArray(4 * 1024) // or other buffer size
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                }
                                output.flush()
                                Log.d("sbm", "File created: $file")
                                runOnUiThread {
                                    showOkToast("$file " + resources.getString(R.string.downloaded))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("sbm", "Error al escribir archivo: ${e.message}")
                            runOnUiThread {
                                showErrorToast("$e.message")
                            }

                        } finally {
                            input?.close()
                        }
                    } else {
                        Log.d("smb", "Download no data null.")
                    }
                } else {
                    Log.e("smb", "Error on download $id, result code: $call")
                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm", "Download Error:" + Ex.message)
                    showErrorToast("Download Error: " + Ex.message)
                }
            }
            // hide progress bar
            runOnUiThread {
                showProgress(false)
            }
        }
    }

    private fun checkLogin() {
        // start thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.checklogin("session=$session")
                val bodyResponse = call.body()
                if (call.isSuccessful) {
                    Log.d(
                        "sbm_checkLogin",
                        "checkLogin response ok, bodyResponse: ${bodyResponse.toString()}"
                    )
                    if (bodyResponse != null) {
                        runOnUiThread {
                            Log.d("sbm_checkLogin", "checkLogin status: ${bodyResponse.status}")
                            if (bodyResponse.status) {
                                showProgress(false)
                                // TODO: only when the backups do not have dynamic ID
                                // upDateBackupsBackend()
                                // updateBDnames()
                            } else {
                                startLoginActivity()
                            }
                        }
                    } else {
                        showErrorToast("Response checkLogin error requesting data")
                    }
                } else {
                    Log.e("sbm", "Error in else -> call.isSuccessful: $call.isSuccessful")
                    startLoginActivity()
                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm", "checkLogin Error al conectar:" + Ex.message)
                    showErrorToast("Error " + Ex.message)

                    repeatCheckLogin(Ex.message.toString())

                }
            }
        }
    }

    private fun repeatCheckLogin(log: String) {
        val create = AlertDialog.Builder(this)
        create.setTitle(resources.getString(R.string.loginError))
        create.setMessage(log + "\n" + resources.getString(R.string.retry) + "?")
        create.setCancelable(false)
        create.setNegativeButton(resources.getString(R.string.textNo)) { dialog, which ->
            startLoginActivity()
        }
        create.setPositiveButton(resources.getString(R.string.textYes)) { dialog, which ->
            checkLogin()
        }
        val dialog = create.create()
        dialog.show()
    }

    private fun startLoginActivity() {
        // resultado de la activity Login


        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("session", session)
        intent.putExtra("csrf_token", csrf_token)
        intent.putExtra("server", server)
        resultLauncher.launch(intent)
    }


    private fun logout() {
        Log.d("sbm_logout", "Log out")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = quotesApi.logout("session=$session")
                runOnUiThread {
                    if (call.status == "ok") {
                        Log.d("sbm_logout", "Log out ok")
                        showOkToast(resources.getString(R.string.logout))
                        // aqui limpiar credencial y salir

                    } else {
                        Log.e("sbm_logout", "Error on log out result code: $call")
                        showErrorToast(call.status)
                    }
                    session = ""
                    csrf_token = ""
                    // localDataDump()
                    finish()

                }

            } catch (Ex: Exception) {
                // call principal thread.
                runOnUiThread {
                    Log.e("sbm_logout", "logout Error:" + Ex.message)
                    showErrorToast("logout Error " + Ex.message)
                }
            }
        }
    }
}