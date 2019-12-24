package net.adadev.qrscanner2020.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import net.adadev.qrscanner2020.util.ButtonHandler;
import net.adadev.qrscanner2020.util.DatabaseHelper;
import net.adadev.qrscanner2020.util.GeneralHandler;

import net.adadev.qrscanner2020.R;
import net.adadev.qrscanner2020.util.BottomNavigationViewHelper;

import static net.adadev.qrscanner2020.util.ButtonHandler.openInWeb;

/**
* Created by Thore Dankworth
* Last Update: 12.12.2019
* Last Update by Thore Dankworth
*
* This class is the HistoryDetailsActivity shows details and further functionality for the chosen item
*/

public class HistoryDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EditDataActivity";
    private GeneralHandler generalHandler;

    private TextView tvCode;
    private BottomNavigationView action_navigation;
    private BottomNavigationItemView action_navigation_web_button, action_navigation_contact_button;

    DatabaseHelper historyDatabaseHelper;
    final Activity activity = this;

    private String selectedCode;
    private int selectedID;

    /**
     * This method handles the main navigation
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.history_action_navigation_delete:
                    historyDatabaseHelper.deleteItem(selectedID);
                    Toast.makeText(activity, activity.getResources().getText(R.string.notice_deleted_from_database), Toast.LENGTH_LONG).show();
                    activity.finish();
                    return true;
                //Following cases using a method from ButtonHandler
                case R.id.history_action_navigation_copy:
                    ButtonHandler.copyToClipboard(tvCode, selectedCode, activity);
                    return true;
                case R.id.history_action_navigation_openInWeb:
                    ButtonHandler.openInWeb(selectedCode, activity);
                    return true;
                case R.id.history_action_navigation_createContact:
                    ButtonHandler.createContact(selectedCode, activity);
                    return true;
                case R.id.history_action_navigation_share:
                    ButtonHandler.shareTo(selectedCode, activity);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        generalHandler = new GeneralHandler(this);
        generalHandler.loadTheme();
        setContentView(R.layout.activity_history_details);
        tvCode = (TextView) findViewById(R.id.tvCodeHD);
        action_navigation = (BottomNavigationView) findViewById(R.id.history_action_navigation);
        BottomNavigationViewHelper.disableShiftMode(action_navigation);
        action_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        action_navigation_web_button = (BottomNavigationItemView) findViewById(R.id.history_action_navigation_openInWeb);
        action_navigation_contact_button = (BottomNavigationItemView) findViewById(R.id.history_action_navigation_createContact);
        historyDatabaseHelper = new DatabaseHelper(this);

        //Get the extra information from the history listview. and set the text in the textview eqaul to code
        Intent receivedIntent = getIntent();
        selectedID = receivedIntent.getIntExtra("id", -1); //-1 is the default value
        Cursor data = historyDatabaseHelper.getItemData(selectedID);
        while(data.moveToNext()){
            selectedCode = data.getString(0);
        }
        tvCode.setText(selectedCode);

        if(selectedCode.contains("BEGIN:VCARD") & selectedCode.contains("END:VCARD")){
            action_navigation_web_button.setVisibility(View.GONE);
            action_navigation_contact_button.setVisibility(View.VISIBLE);
        } else {
            action_navigation_contact_button.setVisibility(View.GONE);
            action_navigation_web_button.setVisibility(View.VISIBLE);
        }

    }
}
