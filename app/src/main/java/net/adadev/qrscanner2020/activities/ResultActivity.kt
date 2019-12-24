package net.adadev.qrscanner2020.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import net.adadev.qrscanner2020.R
import net.adadev.qrscanner2020.util.BottomNavigationViewHelper
import net.adadev.qrscanner2020.util.ButtonHandler
import net.adadev.qrscanner2020.util.DatabaseHelper
import net.adadev.qrscanner2020.util.GeneralHandler
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    private var generalHandler: GeneralHandler? = null
    private var bitmap: Bitmap? = null
    private var multiFormatWriter: MultiFormatWriter? = null
    private var qrcode = ""
    private var qrcodeFormat = ""
    private var mDatabaeHelper: DatabaseHelper? = null

    private var actionnavigation: BottomNavigationView? = null
    private var actionnavigationwebbutton: BottomNavigationItemView? = null
    private  var actionnavigationcontactbutton:BottomNavigationItemView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        mDatabaeHelper = DatabaseHelper(this)
        generalHandler = GeneralHandler(this)
        generalHandler!!.loadTheme()

        actionnavigation = findViewById<View>(R.id.main_action_navigation) as BottomNavigationView
        BottomNavigationViewHelper.disableShiftMode(actionnavigation)
        actionnavigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        actionnavigationwebbutton = findViewById<View>(R.id.main_action_navigation_openInWeb) as BottomNavigationItemView
        actionnavigationcontactbutton = findViewById<View>(R.id.main_action_navigation_createContact) as BottomNavigationItemView

        val extras = intent.extras
        if (extras == null) {
            finish()
        }
        qrcodeFormat = extras?.getString("dataQRFormat", "").toString()
        qrcode = extras?.getString("dataQR", "").toString()
        if(qrcodeFormat=="" || qrcode=="")
            finish()

        showQrImage()
        tvTxtqrcode.text = qrcode
        if (qrcode.contains("BEGIN:VCARD") and qrcode.contains("END:VCARD")) {
            actionnavigationwebbutton!!.visibility = View.GONE
            actionnavigationcontactbutton!!.visibility = View.VISIBLE
        } else {
            actionnavigationcontactbutton!!.visibility = View.GONE
            actionnavigationwebbutton!!.visibility = View.VISIBLE
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val history_setting = prefs.getString("pref_history", "")
        if (history_setting == "false") { //Don't save QR-Code in history
        } else {
            addToDatabase(qrcode, qrcodeFormat)
        }

        val auto_scan = prefs.getString("pref_auto_clipboard", "")
        if (auto_scan == "true") {
            ButtonHandler.copyToClipboard(tvTxtqrcode, qrcode, this)
        }
    }

    fun addToDatabase(newCode: String?, format: String?) {
        val insertData: Boolean = mDatabaeHelper!!.addData(newCode)
        if (!insertData) {
            Toast.makeText(this, resources.getText(R.string.error_add_to_database), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * This method creates a picture of the scanned qr code
     */
    private fun showQrImage() {
        multiFormatWriter = MultiFormatWriter()
        try {
            val format = generalHandler!!.StringToBarcodeFormat(qrcodeFormat)
            val bitMatrix = multiFormatWriter!!.encode(qrcode, format, 250, 250)
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.createBitmap(bitMatrix)
            resultImageMain.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, resources.getText(R.string.error_generate), Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    /**
     * This method handles the main navigation
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.main_action_navigation_copy -> {
                ButtonHandler.copyToClipboard(tvTxtqrcode, qrcode, this)
                return@OnNavigationItemSelectedListener true
            }
            R.id.main_action_navigation_openInWeb -> {
                ButtonHandler.openInWeb(qrcode, qrcodeFormat, this)
                return@OnNavigationItemSelectedListener true
            }
            R.id.main_action_navigation_createContact -> {
                ButtonHandler.createContact(qrcode, this)
                return@OnNavigationItemSelectedListener true
            }
            R.id.main_action_navigation_share -> {
                ButtonHandler.shareTo(qrcode, this)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
