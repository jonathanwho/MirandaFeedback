MirandaFeedback
===============

Feedback dialog for Android apps.

Usage
-------------------------
The demo app uses a few conditional check statements from the app to build a MirandaFeedback Dialog as so:
<pre>
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
if (mOptName.isChecked())
  dialog.addField("Name");
if (mOptEmail.isChecked())
  dialog.addField("Email");
// displays the dialog
dialog.show();
</pre>

But a real use case may be to build the dialog, and to display it once a user clicks on your feedback widget as follows:
<pre>
MirandaFeedback dialog = new MirandaFeedback(getActivity())
  .setEmail(USER_EMAIL@GMAIL.COM, PASSWORD, "Feedback Response from MirandaFeedback", TO_EMAIL@MAIL.COM)
  .setDialogTitle("What do you think about our app?")
  .addField("Email");

feedbackWidget.setOnClickListener(new OnClickListener() {
  @Override
  public void onClick(View view) {
    dialog.show();
  }
});
</pre>