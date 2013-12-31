package com.jonathanwho.mirandafeedback;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * MirandaFeedback is wrapper class that creates a user-customizable feedback 
 * form that allows you to easily recieve feedback on your app via a Gmail
 * account. Adding multiple custom attributes is as easy as a few method calls.
 * 
 * @author Jonathan Miranda of jonathanwho.com
 * @version 1.1
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
   public final static String APP_NAME = "APP_NAME";
   public final static String POS_BUTTON = "POS_BUTTON";
   public final static String NEG_BUTTON = "NEG_BUTTON";
   public final static String DIALOG_TITLE = "DIALOG_TITLE";
   public final static String ADDED_FIELDS = "ADDED_FIELDS";
   public final static String TEXT_EMAIL = "TEXT_EMAIL";
   public final static String GMAIL_RECIPIENT = "GMAIL_RECIPIENT";
   public final static String GMAIL_PW = "GMAIL_PW";
   public final static String GMAIL_FROM = "GMAIL_FROM";
   public final static String GMAIL_SUBJECT = "GMAIL_SUBJECT";

   /** Text for dialog buttons */
   private final static String POS_BUTTON_TXT = "Send";
   private final static String NEG_BUTTON_TXT = "Cancel";

   /** Gmail account information */
   private static String gmailFromEmail;
   private static String gmailPassword;
   private static String gmailSubject;
   private static String gmailRecipientEmail;

   /**
    * Construct a new MirandaFeedback object.
    * @param context Context of the calling activity.
    * @param fromEmail Gmail account (include @gmail.com)
    * @param password Password of the gmail account
    * @param subject Subject of the email
    * @param recipientEmail Recipient of the feedback response.
    * @throws MirandaInvalidEmailException
    */
   public MirandaFeedback(Context context, String fromEmail, String password, String subject,
      String recipientEmail) {

      if (fromEmail == null || password == null || subject == null || recipientEmail == null
         || !android.util.Patterns.EMAIL_ADDRESS.matcher(fromEmail).matches()
         || !android.util.Patterns.EMAIL_ADDRESS.matcher(recipientEmail).matches()) {
         throw new MirandaInvalidEmailException();
      }

      fields = new ArrayList<String>();
      positiveButtonText = POS_BUTTON_TXT;
      negativeButtonText = NEG_BUTTON_TXT;
      fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
      dialog = new FeedbackDialog();
      gmailFromEmail = fromEmail;
      gmailPassword = password;
      gmailSubject = subject;
      gmailRecipientEmail = recipientEmail;
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
    * Adds a new edit text field to the dialog.
    * @param label The label for the new field.
    * @return This MirandaFeedback object to allow for chaining of calls to set methods.
    */
   public MirandaFeedback addField(String label) {
      fields.add(label);
      return this;
   }

   /**
    * Sets the text for the positive button.
    * @param positiveButtonText The text for the positive button.
    * @return This MirandaFeedback object to allow for chaining of calls to set methods.
    */
   public MirandaFeedback setPositiveButtonText(String positiveButtonText) {
      this.positiveButtonText = positiveButtonText;
      return this;
   }

   /**
    * Sets the text for the negative button.
    * @param negativeButtonText The text for the negative button.
    * @return This MirandaFeedback object to allow for chaining of calls to set methods.
    */
   public MirandaFeedback setNegativeButtonText(String negativeButtonText) {
      this.negativeButtonText = negativeButtonText;
      return this;
   }

   /**
    * Replaces the entire feedback dialog title.
    * @param dialogTitle The new title for the feedback dialog.
    * @return This MirandaFeedback object to allow for chaining of calls to set methods.
    */
   public MirandaFeedback setDialogTitle(String dialogTitle) {
      this.dialogTitle = dialogTitle;
      return this;
   }

   /**
    * Determines how to send the feedback email to |gmailRecipient|.
    * @param textEmail If true, email is sent via plain text. HTML otherwise.
    * @return This MirandaFeedback object to allow for chaining of calls to set methods.
    */
   public MirandaFeedback setTextEmail(boolean textEmail) {
      this.textEmail = textEmail;
      return this;
   }

   /**
    * Sets arguments and displays the feedback dialog.
    */
   public void show() {
      Bundle arguments = new Bundle();
      arguments.putString(APP_NAME, appName);
      arguments.putString(POS_BUTTON, positiveButtonText);
      arguments.putString(NEG_BUTTON, negativeButtonText);
      arguments.putString(DIALOG_TITLE, dialogTitle);
      arguments.putBoolean(TEXT_EMAIL, textEmail);
      arguments.putStringArrayList(ADDED_FIELDS, fields);
      arguments.putString(GMAIL_FROM, gmailFromEmail);
      arguments.putString(GMAIL_PW, gmailPassword);
      arguments.putString(GMAIL_SUBJECT, gmailSubject);
      arguments.putString(GMAIL_RECIPIENT, gmailRecipientEmail);
      dialog.setArguments(arguments);
      dialog.show(fragmentManager, "feedback_dialog");
   }

   /**
    * MirandaNoEmailException is thrown when 
    * {@link MirandaFeedback#setEmail(String, String, String, String)} is not
    * called or its parameters are invalid.  
    */
   public class MirandaInvalidEmailException extends RuntimeException {
      private static final long serialVersionUID = -9223012931688847972L;

      public MirandaInvalidEmailException() {
         super();
      }

      public MirandaInvalidEmailException(String message) {
         super(message);
      }

      public MirandaInvalidEmailException(String message, Throwable cause) {
         super(message, cause);
      }

      public MirandaInvalidEmailException(Throwable cause) {
         super(cause);
      }
   }
}
