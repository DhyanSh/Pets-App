/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetsEntry;

import static java.lang.Integer.parseInt;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
   private Uri currentPrtUri;
    private static final int EXISTING_PET_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
        Intent  intent = getIntent();
         currentPrtUri = intent.getData();

        if (currentPrtUri == null){
            setTitle(R.string.editor_activity_title_new_pet);
            invalidateOptionsMenu();

        }
        else {
            setTitle(R.string.editor_activity_title_edit_pet);
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }



    }


    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {

                        mGender = PetContract.PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {

                        mGender = PetContract.PetsEntry.GENDER_FEMALE; // Female

                    } else {
                        mGender = PetContract.PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }


    private void saveData() {



          //  PetDbHelper mDbHelper = new PetDbHelper(this);
          //  SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int petWeightInt = 0;

        String petName=mNameEditText.getText().toString();
        String petBreed=mBreedEditText.getText().toString();
        String petWeightString = mWeightEditText.getText().toString();

        if (currentPrtUri == null &&
                TextUtils.isEmpty(petName) && TextUtils.isEmpty(petBreed) &&
                TextUtils.isEmpty(petWeightString) && mGender == PetsEntry.GENDER_UNKNOWN) {return;}

        if (!TextUtils.isEmpty(petWeightString)) {
            petWeightInt = Integer.parseInt(petWeightString);
        }
            ContentValues values = new ContentValues();
            values.put(PetContract.PetsEntry.COLUMN_PET_NAME, petName);
            values.put(PetContract.PetsEntry.COLUMN_PET_BREED,petBreed );
            values.put(PetContract.PetsEntry.COLUMN_PET_GENDER, mGender);
            values.put(PetContract.PetsEntry.COLUMN_PET_WEIGHT, petWeightInt);


            if (currentPrtUri==null) {
                Log.v("Editor", "Current Uri = " + currentPrtUri);
                Uri newRowId = getContentResolver().insert(PetContract.PetsEntry.CONTENT_URI, values);
                // String newRowId = String.valueOf(ContentUris.parseId(newRowUri));
                // long newRowId = db.insert(PetsEntry.TABLE_NAME,null,values);
                Log.v("Catalog", "Row ID = " + newRowId);

                if (newRowId == null) {
                    Toast.makeText(this, R.string.Error_with_saving_pet, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.Pet_saved) + newRowId, Toast.LENGTH_SHORT).show();
                }
            }
        else {
            int editRowID = getContentResolver().update(currentPrtUri,values,null,null);
                Log.v("Editor", "Current Uri = " + currentPrtUri);
                Log.v("Editor", "Edit Row ID = " + editRowID);
                if (editRowID == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                            Toast.LENGTH_SHORT).show();
                }

            }

    }


    String [] projection = {
            PetContract.PetsEntry._ID,
            PetsEntry.COLUMN_PET_NAME,
            PetContract.PetsEntry.COLUMN_PET_BREED,
            PetContract.PetsEntry.COLUMN_PET_GENDER,
            PetContract.PetsEntry.COLUMN_PET_WEIGHT  };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentPrtUri==null){
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
                // Do nothing for now
                    saveData();

                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteData() {

            int deletedRowID = getContentResolver().delete(currentPrtUri,null,null);
            Log.v("Editor", "Current Uri = " + currentPrtUri);
            Log.v("Editor", "Edit Row ID = " + deletedRowID);
            if (deletedRowID == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }

        NavUtils.navigateUpFromSameTask(this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,currentPrtUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            //get Columns Names
            int nameColumnIndex = data.getColumnIndex(PetContract.PetsEntry.COLUMN_PET_NAME);
            int breedColumnIndex = data.getColumnIndex(PetContract.PetsEntry.COLUMN_PET_BREED);
            int genderColumnIndex = data.getColumnIndex(PetContract.PetsEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = data.getColumnIndex(PetContract.PetsEntry.COLUMN_PET_WEIGHT);

            //Get data from the cursor
            String name = data.getString(nameColumnIndex);
            String breed = data.getString(breedColumnIndex);
            int gender = data.getInt(genderColumnIndex);
            int weight = data.getInt(weightColumnIndex);

            //Set data on activity
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            switch (gender) {
                case PetContract.PetsEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(0);
                    break;
                case PetContract.PetsEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetContract.PetsEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}