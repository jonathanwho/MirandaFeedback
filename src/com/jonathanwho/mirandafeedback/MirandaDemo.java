package com.jonathanwho.mirandafeedback;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MirandaDemo extends FragmentActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      Button button = (Button) findViewById(R.id.send_feedback);
      button.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            new MirandaFeedback(MirandaDemo.this)
            // Sets dialog title Send Feedback for MirandaFeedback
               .setAppName("MirandaFeedback")
               // optional: Adds a "Name" field
               .addEditText("Name")
               // optional: Adds an "Email" field
               .addEditText("Email")
               // setEmail is required (from email must be a gmail acct)
               .setEmail("FROMEMAIL@gmail.com", "PASSWORD", "EMAIL SUBJECT",
                  "SENDFEEDBACKTO@RECIPIENT.com")
               // displays the dialog
               .show();
         }
      });
   }
}
