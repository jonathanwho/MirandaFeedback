package com.jonathanwho.mirandafeedback.demo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.jonathanwho.mirandafeedback.MirandaFeedback;
import com.jonathanwho.mirandafeedback.R;

public class MirandaDemo extends FragmentActivity implements OnClickListener {

   Button mOpen;
   EditText mEmail;
   EditText mPassword;
   RadioGroup mEmailType;

   EditText mAppName;
   EditText mDialogTitle;

   private final String REQUIRED_FIELD = "This field is required.";
   private final String GMAIL_ACCOUNT = "GMAIL_ACCOUNT";
   private final String GMAIL_PW = "GMAIL_PW";
   private final String APP_NAME = "APP_NAME";
   private final String DIALOG_TITLE = "DIALOG_TITLE";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mOpen = (Button) findViewById(R.id.send_feedback);
      mEmail = (EditText) findViewById(R.id.email);
      mPassword = (EditText) findViewById(R.id.password);
      mEmailType = (RadioGroup) findViewById(R.id.email_type);
      mAppName = (EditText) findViewById(R.id.app_name);
      mDialogTitle = (EditText) findViewById(R.id.dialog_title);
      mOpen.setOnClickListener(this);

      // Gets info from preferences
      SharedPreferences preferences = getPreferences(MODE_PRIVATE);
      mEmail.setText(preferences.getString(GMAIL_ACCOUNT, ""));
      mPassword.setText(preferences.getString(GMAIL_PW, ""));
      mAppName.setText(preferences.getString(APP_NAME, ""));
      mDialogTitle.setText(preferences.getString(DIALOG_TITLE, ""));
   }

   @Override
   public void onClick(View view) {
      String email = mEmail.getText().toString();
      String password = mPassword.getText().toString();
      String appName = mAppName.getText().toString();
      String dialogTitle = mDialogTitle.getText().toString();

      if (email.isEmpty())
         mEmail.setError(REQUIRED_FIELD);
      else if (password.isEmpty())
         mPassword.setError(REQUIRED_FIELD);
      else {
         // Saves info to preferences
         SharedPreferences preferences = getPreferences(MODE_PRIVATE);
         Editor editor = preferences.edit();
         editor.putString(GMAIL_ACCOUNT, email);
         editor.putString(GMAIL_PW, password);
         editor.putString(APP_NAME, appName);
         editor.putString(DIALOG_TITLE, dialogTitle);
         editor.commit();

         // Builds the MirandaFeedback dialog
         MirandaFeedback dialog = new MirandaFeedback(MirandaDemo.this);
         // setEmail is required (from email must be a gmail acct)
         // for app simplicity we reuse the to/from emails
         dialog.setEmail(email, password, "Feedback from MirandaFeedbackDemo", email);
         // Sets app name
         if (!appName.isEmpty())
            dialog.setAppName(appName);
         // Replaces dialog title entirely
         if (!dialogTitle.isEmpty())
            dialog.setDialogTitle(dialogTitle);
         switch (mEmailType.getCheckedRadioButtonId()) {
         // HTML by default
            case R.id.plain_text:
               dialog.setTextEmail(true);
               break;
         }
         // displays the dialog
         dialog.show();
      }
   }
}
