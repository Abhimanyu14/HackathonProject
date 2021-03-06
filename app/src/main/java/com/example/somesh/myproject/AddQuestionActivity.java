package com.example.somesh.myproject;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.somesh.myproject.data.Contract.CategoryEntry;
import com.example.somesh.myproject.data.Contract.QuestionsEntry;

public class AddQuestionActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //    Identifier for the category data loader
    private static final int EXISTING_QUESTION_LOADER = 30;

    //    Content URI for the existing category (null if it's a new category)
    private Uri mCurrentQuesstionUri;

    //    EditText field to enter the category's name
    private EditText q_no_et;
    private EditText q_q_et;
    private EditText q_op1_et;
    private EditText q_op2_et;
    private EditText q_op3_et;
    private EditText q_op4_et;
    private EditText q_ans_et;

    //    Boolean flag that keeps track of whether the category has been edited (true) or not (false)
    private boolean mCategoryHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCategoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCategoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new category or editing an existing one.
        Intent intent = getIntent();
        mCurrentQuesstionUri = intent.getData();

        // If the intent DOES NOT contain a category content URI, then we know that we are
        // creating a new category.
        if (mCurrentQuesstionUri == null) {
            // This is a new category, so change the app bar to say "Add a category"
            setTitle("Add Category");

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing category, so change app bar to say "Edit category"
            setTitle("Edit Category");

            // Initialize a loader to read the category data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_QUESTION_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        q_no_et = findViewById(R.id.q_no_et_id);
        q_q_et = findViewById(R.id.q_q_et_id);
        q_ans_et = findViewById(R.id.q_ans_et_id);
        q_op1_et = findViewById(R.id.q_op1_et_id);
        q_op2_et = findViewById(R.id.q_op2_et_id);
        q_op3_et = findViewById(R.id.q_op3_et_id);
        q_op4_et = findViewById(R.id.q_op4_et_id);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        q_no_et.setOnTouchListener(mTouchListener);
        q_q_et.setOnTouchListener(mTouchListener);
        q_ans_et.setOnTouchListener(mTouchListener);
        q_op1_et.setOnTouchListener(mTouchListener);
        q_op2_et.setOnTouchListener(mTouchListener);
        q_op3_et.setOnTouchListener(mTouchListener);
        q_op4_et.setOnTouchListener(mTouchListener);

    }

    //    Get user input from editor and save category into database.
    private void saveQuestion() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String q_no_String = q_no_et.getText().toString().trim();
        String q_ans_String = q_ans_et.getText().toString().trim();
        String q_op1_String = q_op1_et.getText().toString().trim();
        String q_op2_String = q_op2_et.getText().toString().trim();
        String q_op3_String = q_op3_et.getText().toString().trim();
        String q_op4_String = q_op4_et.getText().toString().trim();
        String q_q_String = q_q_et.getText().toString().trim();

        // Check if this is supposed to be a new category
        // and check if all the fields in the editor are blank
        if (mCurrentQuesstionUri == null && TextUtils.isEmpty(q_no_String)) {
            // Since no fields were modified, we can return early without creating a new category.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and category attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(QuestionsEntry.COLUMN_Q_NO, q_no_String);
        values.put(QuestionsEntry.COLUMN_Q_ANS, q_ans_String);
        values.put(QuestionsEntry.COLUMN_Q_OPTION1, q_op1_String);
        values.put(QuestionsEntry.COLUMN_Q_OPTION2, q_op2_String);
        values.put(QuestionsEntry.COLUMN_Q_OPTION3, q_op3_String);
        values.put(QuestionsEntry.COLUMN_Q_OPTION4, q_op4_String);
        values.put(QuestionsEntry.COLUMN_Q_Q, q_q_String);
        // If the credit is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.

        // Determine if this is a new or existing category by checking if mCurrentQuesstionUri is null or not
        if (mCurrentQuesstionUri == null) {
            // This is a NEW category, so insert a new category into the provider,
            // returning the content URI for the new category.
            Uri newUri = getContentResolver().insert(QuestionsEntry.CONTENT_URI_Q, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error with saving", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Question Added", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING category, so update the category with content URI: mCurrentQuesstionUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentQuesstionUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentQuesstionUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Error with updating", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Category Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new category, hide the "Delete" menu item.
        if (mCurrentQuesstionUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save category to database
                saveQuestion();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the category hasn't changed, continue with navigating up to parent activity
                // which is the {@link DisplayActivity}.
                if (!mCategoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddQuestionActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask
                                        (AddQuestionActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //    This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the category hasn't changed, continue with handling back button press
        if (!mCategoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the category.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //    Prompt the user to confirm that they want to delete this category.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this category?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the category.
                deleteCategory();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the category.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //    Perform the deletion of the category in the database.
    private void deleteCategory() {
        // Only perform the delete if this is an existing category.
        if (mCurrentQuesstionUri != null) {
            // Call the ContentResolver to delete the category at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentQuesstionUri
            // content URI already identifies the category that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentQuesstionUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error with deleting...", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Category Deleted", Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all category attributes, define a projection that contains
        // all columns from the category table
        String[] projection = {
                CategoryEntry._ID,
                CategoryEntry.COLUMN_CATEGORY_NAME};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentQuesstionUri,         // Query the content URI for the current category
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of category attributes that we're interested in
            int no_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_NO);
            int ans_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_ANS);
            int q_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_Q);
            int op1_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_OPTION1);
            int op2_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_OPTION2);
            int op3_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_OPTION3);
            int op4_ColumnIndex = cursor.getColumnIndex(QuestionsEntry.COLUMN_Q_OPTION4);

            // Extract out the value from the Cursor for the given column index
            String q_no = cursor.getString(no_ColumnIndex);
            String q_ans = cursor.getString(ans_ColumnIndex);
            String q_q = cursor.getString(q_ColumnIndex);
            String q_op1 = cursor.getString(op1_ColumnIndex);
            String q_op2 = cursor.getString(op2_ColumnIndex);
            String q_op3 = cursor.getString(op3_ColumnIndex);
            String q_op4 = cursor.getString(op4_ColumnIndex);

            // Update the views on the screen with the values from the database
            q_no_et.setText(q_no);
            q_ans_et.setText(q_ans);
            q_q_et.setText(q_q);
            q_op1_et.setText(q_op1);
            q_op2_et.setText(q_op2);
            q_op3_et.setText(q_op3);
            q_op4_et.setText(q_op4);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        q_no_et.setText("");
        q_ans_et.setText("");
        q_q_et.setText("");
        q_op1_et.setText("");
        q_op2_et.setText("");
        q_op3_et.setText("");
        q_op4_et.setText("");
    }

}
