MirandaFeedback
===============

Feedback dialog for Android apps.

Usage
-------------------------
The demo app uses a few conditional check statements from the app to build a MirandaFeedback Dialog as so:
<pre>
// Builds the MirandaFeedback dialog
// for app simplicity we reuse the to/from emails (2nd/4th parameters)
MirandaFeedback dialog =
  new MirandaFeedback(MirandaDemo.this, email, password,
     "Feedback from MirandaFeedbackDemo", email);

if (!appName.isEmpty()) // Sets app name in dialog title
  dialog.setAppName(appName);

if (!dialogTitle.isEmpty()) // Replaces dialog title entirely
  dialog.setDialogTitle(dialogTitle);

if (mEmailType.getCheckedRadioButtonId() == R.id.plain_text)
  dialog.setTextEmail(true); // HTML by default

if (mOptName.isChecked()) // Adds name field to dialog
  dialog.addField("Name");

if (mOptEmail.isChecked()) // Adds email field to dialog
  dialog.addField("Email");

dialog.show(); // displays the dialog
</pre>

But a real use case may be to build the dialog, and to display it once a user clicks on your feedback widget as follows:
<pre>
MirandaFeedback dialog = new MirandaFeedback(getActivity(), USER_EMAIL@GMAIL.COM, PASSWORD, EMAIL_SUBJECT, TO_EMAIL@MAIL.COM)
  .setDialogTitle("What do you think about our app?")
  .addField("Email");

feedbackWidget.setOnClickListener(new OnClickListener() {
  @Override
  public void onClick(View view) {
    dialog.show();
  }
});
</pre>