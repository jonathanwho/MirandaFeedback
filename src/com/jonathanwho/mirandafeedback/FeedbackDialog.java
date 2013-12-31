package com.jonathanwho.mirandafeedback;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jonathanwho.mail.GmailSender;

/**
 * The actual DialogFragment that appears on the UI.
 * @author jmiranda
 *
 */
public class FeedbackDialog extends DialogFragment implements OnClickListener {
   /** Holds the ViewGroups of the custom fields */
   private List<ViewGroup> viewFields;
   /** Holds the text from the feedback form */
   private EditText feedback;
   /** Holds the format of the email. True: text/plain False: text/html*/
   private boolean textEmail;

   private static final int FIELD_TEXT = 0; // TextView is the 0th child
   private static final int FIELD_INPUT = 1; // EditText is the 1st child

   private static final String NETWORK_ERROR = "No network found.";
   private static final String REQUIRED_FIELD_ERROR = "This field is required.";
   private static final String UNKNOWN_ERROR = "An unknown error occurred.";
   private static final String SUCCESSFUL_MSG = "Thank you for your feedback!";
   private final String PROGRESS_MSG = "Sending feedback...";

   private static final String DIALOG_TITLE = "Send Feedback";
   private static final String DIALOG_TITLE_WITH_APP_NAME = "Send Feedback for ";

   /** Gmail account information */
   private static String gmailFromEmail;
   private static String gmailPassword;
   private static String gmailSubject;
   private static String gmailRecipientEmail;

   /**
    * Construct a new FeedbackDialog.
    */
   public FeedbackDialog() {
      viewFields = new ArrayList<ViewGroup>();
   }

   /**
    * Gets and sets custom dialog attributes then displays the dialog.
    */
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      Bundle arguments = getArguments();
      String appName = arguments.getString(MirandaFeedback.APP_NAME);
      String positiveButtonText = arguments.getString(MirandaFeedback.POS_BUTTON);
      String negativeButtonText = arguments.getString(MirandaFeedback.NEG_BUTTON);
      String dialogTitle = arguments.getString(MirandaFeedback.DIALOG_TITLE);
      textEmail = arguments.getBoolean(MirandaFeedback.TEXT_EMAIL);
      gmailFromEmail = arguments.getString(MirandaFeedback.GMAIL_FROM);
      gmailRecipientEmail = arguments.getString(MirandaFeedback.GMAIL_RECIPIENT);
      gmailPassword = arguments.getString(MirandaFeedback.GMAIL_PW);
      gmailSubject = arguments.getString(MirandaFeedback.GMAIL_SUBJECT);

      ArrayList<String> fields = arguments.getStringArrayList(MirandaFeedback.ADDED_FIELDS);

      // Gets the title for the dialog
      String title =
         appName == null ? DIALOG_TITLE : dialogTitle == null ? DIALOG_TITLE_WITH_APP_NAME
            + appName : dialogTitle;

      // Sets the dialog attributes
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(title);
      builder.setPositiveButton(positiveButtonText, null);
      builder.setNegativeButton(negativeButtonText, null);

      LayoutInflater inflater = getActivity().getLayoutInflater();
      ViewGroup root = (ViewGroup) inflater.inflate(R.layout.feedback_layout, null);
      feedback = (EditText) root.findViewById(R.id.feedback);

      // Adds new fields to the feedback form
      for (String field : fields) {
         ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.edit_text_template, null);
         TextView textView = (TextView) layout.findViewById(R.id.text_view);
         textView.setText(field);
         root.addView(layout);
         viewFields.add(layout);
      }

      builder.setView(root);
      return builder.create();
   }

   @Override
   public void onStart() {
      super.onStart();
      AlertDialog dialog = (AlertDialog) getDialog();
      if (dialog != null) {
         Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
         positiveButton.setOnClickListener(this); // FeedbackDialog#onClick
      }
   }

   /**
    * Formats the feedback response into a properly formatted HTML email:
    * 
    * _____________________________________
    * |{Feedback Label} | {Feedback Input}|
    * _____________________________________
    * |{Feedback Label} | {Feedback Input}|
    * _____________________________________
    * 
    * @return Feedback input as HTML.
    */
   public String formattedHtmlEmail() {
      StringBuilder body = new StringBuilder();
      String fieldLabel;
      String userInput;
      String tableRowFormat =
         "<tr><td style='width: 25%%; background: #efefef;'><b>%s</b></td><td>%s</td></tr>";

      // Builds body of the email
      body.append("<table style='width: 600px; border: 1px solid black;'>");
      body.append(String.format(tableRowFormat, "Feedback", feedback.getText().toString()));
      for (ViewGroup layout : viewFields) {
         fieldLabel = ((TextView) layout.getChildAt(FIELD_TEXT)).getText().toString();
         userInput = ((EditText) layout.getChildAt(FIELD_INPUT)).getText().toString();
         // Strip input
         fieldLabel = Html.fromHtml(fieldLabel).toString();
         userInput = Html.fromHtml(userInput).toString();
         body.append(String.format(tableRowFormat, fieldLabel, userInput));
      }
      body.append("</table>");
      return body.toString();
   }

   /**
    * Formats user input into a plain text email in the form:
    * 
    * {Fieldback Label}:
    *    {Feedback Input}
    * {Fieldback Label}:
    *    {Feedback Input}
    * 
    * @return Feedback input as plain text.
    */
   public String formattedPlainTextEmail() {
      StringBuilder body = new StringBuilder();
      String fieldLabel;
      String userInput;

      // Builds body of the email
      body.append("Feedback:" + "\n\t" + feedback.getText().toString() + "\n");
      for (ViewGroup layout : viewFields) {
         fieldLabel = ((TextView) layout.getChildAt(FIELD_TEXT)).getText().toString();
         userInput = ((EditText) layout.getChildAt(FIELD_INPUT)).getText().toString();
         // Strip input
         fieldLabel = Html.fromHtml(fieldLabel).toString();
         userInput = Html.fromHtml(userInput).toString();
         body.append(fieldLabel + ":\n\t" + userInput + "\n");
      }
      return body.toString();
   }

   /**
    * Sends the feedback form input using the Gmail credentials.
    *  
    * @param email The body of the email message.
    */
   public void sendEmail(String email) {
      // sends feedback to |gmailRecipient|
      new AsyncTask<String, Void, Boolean>() {
         ProgressDialog progressDialog;

         @Override
         protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(PROGRESS_MSG);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
         }

         @Override
         protected Boolean doInBackground(String... subject) {
            GmailSender sender = new GmailSender(gmailFromEmail, gmailPassword, textEmail);
            try {
               sender.sendMail(gmailSubject, subject[0], gmailFromEmail, gmailRecipientEmail);
               return true;
            } catch (Exception e) {
               e.printStackTrace();
            }
            return false;
         }

         @Override
         protected void onPostExecute(Boolean sent) {
            progressDialog.dismiss();
            if (sent) {
               Toast.makeText(getActivity(), SUCCESSFUL_MSG, Toast.LENGTH_LONG).show();
               getDialog().dismiss();
            } else {
               Toast.makeText(getActivity(), UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
         }
      }.execute(email);
   }

   /**
    * Verifies network connection and feedback input fields.
    * If everything is valid, it attemps to send the email
    * Else displays a relevant error.
    * 
    * This method is called when the user clicks on the dialog's positive
    * button.
    */
   @Override
   public void onClick(View view) {
      NetworkInfo networkInfo =
         (NetworkInfo) ((ConnectivityManager) getActivity().getSystemService(
            Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
      if (networkInfo == null || !networkInfo.isConnected()) {
         // no internet connection
         Toast.makeText(getActivity(), NETWORK_ERROR, Toast.LENGTH_LONG).show();
      } else if (feedback.getText().toString().isEmpty()) {
         // required field is left blank
         feedback.setError(REQUIRED_FIELD_ERROR);
      } else {
         sendEmail(textEmail ? formattedPlainTextEmail() : formattedHtmlEmail());
      }
   }
}
