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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetsEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    // This is the Adapter being used to display the list's data

    PetCursorAdapter petCursorAdapter;
    ListView petsLV;
    private static final int PET_LOADER_ID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });
                // Find the ListView which will be populated with the pet data
        petsLV = (ListView) findViewById(R.id.pets_ListView);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petsLV.setEmptyView(emptyView);

         petCursorAdapter = new PetCursorAdapter(this,null);
        petsLV.setAdapter(petCursorAdapter);

        getSupportLoaderManager().initLoader(PET_LOADER_ID,null,this);


        petsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri prtUri= ContentUris.withAppendedId(PetContract.PetsEntry.CONTENT_URI,id);

                intent.setData(prtUri);

                startActivity(intent);
            }
        });


     //   displayDatabaseInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();

      //  displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        PetDbHelper mDbHelper = new PetDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
      //  Cursor cursor = db.query(PetsEntry.TABLE_NAME,projection,null,null,null,null,PetsEntry._ID);

        Cursor cursor= getContentResolver().query(PetContract.PetsEntry.CONTENT_URI,projection,null,null,null);

        ListView petsLV = (ListView) findViewById(R.id.pets_ListView);
                 Log.e("Temp", "Count: "+cursor.getCount());

        PetCursorAdapter petCursorAdapter = new PetCursorAdapter(this,cursor);
        petsLV.setAdapter(petCursorAdapter);

        /*try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("");
            displayView.setText("Number of rows in pets database table: " + cursor.getCount()+"\n\n");
            while(cursor.moveToNext()) {
                String petID = cursor.getString(cursor.getColumnIndex(PetsEntry._ID));
                String petName = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_NAME));
                String petBreed = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_BREED));
                String petGender = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_GENDER));
                String petWeight = cursor.getString(cursor.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_WEIGHT));
                displayView.append("ID: "+petID+", Name: "+petName+", Breed: "+petBreed+", Gender: "+petGender+", Weight: "+ petWeight+"\n" );
            }

            //displayView.setText("Number of rows in pets database table: " + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }*/



    }



    private void insertData() {
       // PetDbHelper mDbHelper = new PetDbHelper(this);
      //  SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetContract.PetsEntry.COLUMN_PET_NAME,"Toto1");
        values.put(PetsEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetsEntry.COLUMN_PET_GENDER,1);
        values.put(PetContract.PetsEntry.COLUMN_PET_WEIGHT,7);

        Uri newRowId = getContentResolver().insert(PetsEntry.CONTENT_URI,values);

       // long newRowId = db.insert(PetsEntry.TABLE_NAME,null,values);
        Log.v("Catalog","Row ID = "+newRowId);
        //displayDatabaseInfo();

    }


    String [] projection = {
            PetsEntry._ID,
            PetsEntry.COLUMN_PET_NAME,
            PetContract.PetsEntry.COLUMN_PET_BREED,
    };

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                getContentResolver().delete(PetsEntry.CONTENT_URI,null,null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PetsEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        petCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        petCursorAdapter.swapCursor(null);
    }
}
