package com.example.android.pets;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetsEntry;


/**
 * Created by Dhyan on 2017-01-21.
 */


public class PetDbHelper extends SQLiteOpenHelper{

     private static final int DATABASE_VERSION = 1;
     private static final String DATABASE_NAME = "petsDB.db";

   //  static final String DATABASE_TABLE_PETS = "CREATE TABLE "+ PetsEntry.TABLE_NAME+" ("+ PetsEntry._ID+"INTEGER PRIMARY KEY AUTOINCREMENT, "+ PetsEntry.COLUMN_PET_NAME +
   //         " TEXT NOR NULL, "+ PetsEntry.COLUMN_PET_BREED+" TEXT, "+ PetsEntry.COLUMN_PET_GENDER+" INTEGER NOT NULL, "+ PetsEntry.COLUMN_PET_WEIGHT+" INTEGER NOT NULL DEFAULT 0);";

    private static final String DATABASE_TABLE_PETS = "CREATE TABLE "+ PetsEntry.TABLE_NAME+" ("+ PetsEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ PetContract.PetsEntry.COLUMN_PET_NAME +
            " TEXT NOR NULL, "+ PetsEntry.COLUMN_PET_BREED+" TEXT, "+ PetsEntry.COLUMN_PET_GENDER+" INTEGER NOT NULL, "+ PetsEntry.COLUMN_PET_WEIGHT+" INTEGER NOT NULL DEFAULT 0);";


    public PetDbHelper(Context context) {

        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_TABLE_PETS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
