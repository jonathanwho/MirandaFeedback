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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
 * MirandaFeedback is wrapper class that creates a user-customizable feedback 
 * form that allows you to easily recieve feedback on your app via a Gmail
 * account. Adding multiple custom attributes is as easy as a few method calls.
 * 
 * @author Jonathan Miranda of jonathanwho.com
 * @version 1.0.2
 */
public class MirandaFeedback {
   /** Name of the app */
   private String appName;
   /** Fields */
   private ArrayList<String> fields;
   /** Text for the dialog's positive button */
   private String positiveButtonText;
   /** Text for the dialog's negative button */
   private String negativeButtonText;
   /** Text to replace the default dialog title */
   private String dialogTitle;
   /** Manages the FeedbackDialog/DialogFragment */
   private FragmentManager fragmentManager;
   /** The wrapped DialogFragment */
   private FeedbackDialog dialog;
   /** True if the feedback email will be sent via plain text (versus default HTML) */
   private boolean textEmail;

   /** Constants used for the dialog's arguments */
   private final static String APP_NAME = "APP_NAME";
   private final static String POS_BUTTON = "POS_BUTTON";
   private final static String NEG_BUTTON = "NEG_BUTTON";
   private final static String DIALOG_TITLE = "DIALOG_TITLE";
   private final static String ADDED_FIELDS = "ADDED_FIELDS";
   private final static String TEXT_EMAIL = "TEXT_EMAIL";

   /** Text for dialog buttons */
   private final static String POS_BUTTON_TXT = "Send";
   private final static String NEG_BUTTON_TXT = "Cancel";

   /** Gmail account information */
   /** google email account username (include @gmail.com) */
   private static String gmailUsername;
   /** password */
   private static String gmailPassword;
   /** subject */
   private static String gmailSubject;
   /** recipient */
   private static String gmailRecipient;

   /**
    * Construct a new MirandaFeedback.
    * @param context Context of the calling activity.
    */
   public MirandaFeedback(Context context) {
      this.fields = new ArrayList<String>();
      this.positiveButtonText = POS_BUTTON_TXT;
      this.negativeButtonText = NEG_BUTTON_TXT;
      this.fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
      this.dialog = new FeedbackDialog();
   }

   /**
    * Sets the attributes for the gmail configuration.
    * @param username Username of the gmail account (include @gmail.com)
    * @param password Password of the gmail account
    * @param subject Subject of the email
    * @param recipient Email account that the feedback will be sent to
    * @return 
    */
   public MirandaFeedback setEmail(String username, String password, String subject,
      String recipient) {
      gmailUsername = username;
      gmailPassword = password;
      gmailSubject = subject;
      gmailRecipient = recipient;
      return this;
   }

   /** 
    * Sets the app name for the default dialog title.
    * Dialog title will be in the form: Send Feedback for |appName|
    * @param appName The name of the calling app.
    * 
    */
   public MirandaFeedback setAppName(String appName) {
      this.appName = appName;
      return this;
   }

   /**
    * Adds a new edit text field to the feedback field.
    * @param label The label for the new field.
    * @return
    */
   public MirandaFeedback addEditText(String label) {
      fields.add(label);
      return this;
   }

   /**
    * Sets the text for the positive button.
    * @param positiveButtonText The text for the positive button.
    * @return
    */
   public MirandaFeedback setPositiveButtonText(String positiveButtonText) {
      this.positiveButtonText = positiveButtonText;
      return this;
   }

   /**
    * Sets the text for the negative button.
    * @param negativeButtonText The text for the negative button.
    * @return
    */
   public MirandaFeedback setNegativeButtonText(String negativeButtonText) {
      this.negativeButtonText = negativeButtonText;
      return this;
   }

   /**
    * Replaces the entire feedback dialog title.
    * @param dialogTitle The new title for the feedback dialog.
    * @return
    */
   public MirandaFeedback setDialogTitle(String dialogTitle) {
      this.dialogTitle = dialogTitle;
      return this;
   }

   /**
    * Determines how to send the feedback email to |gmailRecipient|.
    * @param textEmail If true, feedback email is sent via plain text. HTML otherwise.
    * @return
    */
   public MirandaFeedback setTextEmail(boolean textEmail) {
      this.textEmail = textEmail;
      return this;
   }

   /**
    * Sets arguments and displays the feedback dialog.
    * @throws MirandaNoEmailException
    */
   public void show() throws MirandaNoEmailException {
      if (gmailUsername == null || gmailPassword == null || gmailSubject == null
         || gmailRecipient == null) {
         throw new MirandaNoEmailException();
      }
      Bundle arguments = new Bundle();
      arguments.putString(APP_NAME, appName);
      arguments.putString(POS_BUTTON, positiveButtonText);
      arguments.putString(NEG_BUTTON, negativeButtonText);
      arguments.putString(DIALOG_TITLE, dialogTitle);
      arguments.putBoolean(TEXT_EMAIL, textEmail);
      arguments.putStringArrayList(ADDED_FIELDS, fields);
      dialog.setArguments(arguments);
      dialog.show(fragmentManager, "feedback_dialog");
   }

   /**
    * The actual DialogFragment that appears on the UI.
    * @author jmiranda
    *
    */
   public static class FeedbackDialog extends DialogFragment implements OnClickListener {
      /** Holds the ViewGroups of the custom fields */
      private List<ViewGroup> viewFields;
      /** Holds the text from the feedback form */
      private EditText feedback;
      /** Holds the format of the email. True: text/plain False: text/html*/
      private boolean textEmail;

      private static final int FIELD_TEXT = 0;
      private static final int FIELD_INPUT = 1;

      private static final String NETWORK_ERROR = "No network found.";
      private static final String REQUIRED_FIELD_ERROR = "This field is required.";
      private static final String UNKNOWN_ERROR = "An unknown error occurred.";
      private static final String SUCCESSFUL_MSG = "Thank you for your feedback!";
      private final String PROGRESS_MSG = "Sending feedback...";

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
         String appName = arguments.getString(APP_NAME);
         String positiveButtonText = arguments.getString(POS_BUTTON);
         String negativeButtonText = arguments.getString(NEG_BUTTON);
         String dialogTitle = arguments.getString(DIALOG_TITLE);
         textEmail = arguments.getBoolean(TEXT_EMAIL);
         ArrayList<String> fields = arguments.getStringArrayList(ADDED_FIELDS);

         // Gets the title for the dialog
         String title =
            appName == null ? "Send Feedback" : dialogTitle == null ? "Send Feedback for "
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
         String tableRowFormat = "<tr><td><b>%s</b></td><td>%s</td></tr>";

         // Builds body of the email
         body.append("<table>");
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
       * Formats the feedback response into a plain text email in the form:
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
               GmailSender sender = new GmailSender(gmailUsername, gmailPassword, textEmail);
               try {
                  sender.sendMail(gmailSubject, subject[0], gmailUsername, gmailRecipient);
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

   /**
    * MirandaNoEmailException is thrown when 
    * {@link MirandaFeedback#setEmail(String, String, String, String)} is not
    * called or it's paramters are invalid.  
    * @author jmiranda
    *
    */
   public class MirandaNoEmailException extends RuntimeException {
      private static final long serialVersionUID = -9223012931688847972L;

      public MirandaNoEmailException() {
         super();
      }

      public MirandaNoEmailException(String message) {
         super(message);
      }

      public MirandaNoEmailException(String message, Throwable cause) {
         super(message, cause);
      }

      public MirandaNoEmailException(Throwable cause) {
         super(cause);
      }
   }
}
