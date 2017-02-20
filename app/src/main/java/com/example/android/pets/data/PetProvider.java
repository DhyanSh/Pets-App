package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.PetDbHelper;

import static android.R.attr.id;
import static com.example.android.pets.data.PetContract.*;

/**
 * Created by Dhyan on 2017-01-30.
 */

public class PetProvider extends ContentProvider {


    //Database helper Object
    private PetDbHelper petDbHelper;

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS,PETS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS + "/#",PET_ID);
    }

    @Override
    public boolean onCreate() {
        petDbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = petDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                cursor = database.query(PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case PET_ID:
                selection= PetsEntry._ID+"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                    throw new IllegalArgumentException("Cannot Query unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {


        if (inputValidation(values)){
            final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is Not Supported for uri:  " + uri);
        }
    }
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        SQLiteDatabase database = petDbHelper.getWritableDatabase();
       long id = database.insert(PetsEntry.TABLE_NAME,null,values);


        if (id == -1) {
            Log.e("PetProvider", "Failed to insert row for " + uri);
            return null;
           }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }


    private boolean inputValidation(ContentValues values) {

        boolean validEntry = true;

        Integer gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetsEntry.isValidGender(gender)) {
            validEntry = false;
            throw new IllegalArgumentException("Pet requires valid gender");

        }

        String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
        if (name == null) {
            validEntry = false;
            throw new IllegalArgumentException("Pet requires a name");

        }

        Integer weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);

        if (weight != null && weight < 0) {
            validEntry = false;
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        return validEntry;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = petDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                 rowsDeleted = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                 rowsDeleted = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }


    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(PetsEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetsEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetsEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetsEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetsEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if (values.size() == 0) {
            return 0;
        }


        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
            }



    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PetsEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetsEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetsEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetsEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetsEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = petDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetsEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;

    }




}
