package br.alysson.crudsqlite;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CRUDSQLite";


    String id, name, phone, address;

    List<Person> people;
    private int peopleCount;
    PersonDao personDao;

    // Widgets
    LinearLayout llPrevNext;
    EditText etId,etName,etPhone,etAddress;
    Button btCreate,btRead,btUpdate,btDelete,btPrevious,btNext,btSave,btCancel;



    // Flags
    boolean isCreating = false;
    boolean isSearching = false;
    boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instatiation of widgets
        llPrevNext = (LinearLayout) findViewById(R.id.llPrevNext);

        etId      = (EditText) findViewById(R.id.etID);
        etName    = (EditText) findViewById(R.id.etName);
        etPhone   = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);

        btPrevious = (Button) findViewById(R.id.btPrevious);
        btNext     = (Button) findViewById(R.id.btNext);
        btCreate   = (Button) findViewById(R.id.btCreate);
        btRead     = (Button) findViewById(R.id.btRead);
        btUpdate   = (Button) findViewById(R.id.btUpdate);
        btDelete   = (Button) findViewById(R.id.btDelete);
        btSave     = (Button) findViewById(R.id.btSave);
        btCancel   = (Button) findViewById(R.id.btCancel);

        // Request focus to the etName
        etName.requestFocus();

        toggleEnableEditText(false);

        // Button listeners

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousResult();
            }
        });

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextResult();
            }
        });


        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newP();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create();
                if(!isCreating) {
                    btSave.setVisibility(View.GONE);
                }
            }
        });

        btRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isSearching){

                    // Makes only the Read and Cancel Button Visible
                    toggleEnableButtons(View.GONE);
                    btRead.setVisibility(View.VISIBLE);

                    // Makes only the etName enabled
                    toggleEnableEditText(false);
                    etName.setEnabled(true);
                    etName.requestFocus();

                    // Clear all fields
                    clear();

                    // Sets the flag to true
                    isSearching=true;
                }else {
                    // Gets the value
                    name = etName.getText().toString();

                    // calls the search function with the values
                    search(name);

                    // Set as visible the Next and Previous Buttons
                    //llPrevNext.setVisibility(View.VISIBLE);

                    // Resets the flag
                    isSearching = false;
                }


                }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });


        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sets all Flags to false
                isCreating = false;
                isSearching = false;
                isUpdating = false;



                btSave.setVisibility(View.GONE);

                // Make all other buttons visible again
                toggleEnableButtons(View.VISIBLE);

                // Calls the default search
                search(null);

            }
        });


        try{
            personDao = new PersonDao(MainActivity.this);
            personDao.open();
            search(null);

        }catch(SQLException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttOpenCreate), Toast.LENGTH_LONG).show();
            Log.e(TAG,getString(R.string.ttOpenCreate)+": "+e.getMessage());
        }

        //openOrCreateDatabase();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Closes database on activity destruction
        try {
            personDao.close();
        }catch (SQLException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttDestroy), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.ttDestroy)+": "+e.getMessage());
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void newP(){
        // Clear all fields
        clear();

        // Enables all fields but etId
        toggleEnableEditText(true);
        etId.setEnabled(false);

        // Request focus to the etName
        etName.requestFocus();

        // Hides all buttons but btCreate
        toggleEnableButtons(View.GONE);
        //btCreate.setVisibility(View.VISIBLE);


        btSave.setVisibility(View.VISIBLE);

        // Sets Flag to true
        isCreating = true;
    }

    private void create(){

        // Gets the values of the EditText Widgets
        getValues();

            if(!name.equals("") && !phone.equals("") && !address.equals("")) {
                try {

                    Person person = new Person();

                    person.setName(name);
                    person.setPhone(phone);
                    person.setAddress(address);

                    // Executes the query
                    personDao.create(person);

                    // Clear all fields
                    clear();

                    // Show all buttons again
                    toggleEnableButtons(View.VISIBLE);


                    // Calls default search
                    search(null);

                    // Resets flag
                    isCreating = false;

                }catch (SQLException e){
                    Toast.makeText(MainActivity.this, getString(R.string.ttCreate), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.ttCreate)+": "+e.getMessage());
                }

            }else{
                Toast.makeText(MainActivity.this, getString(R.string.ttFillF), Toast.LENGTH_SHORT).show();
            }


        }



    private boolean search(String name){

        try{
            // Runs the querry with the values passed to the function
            Person person = new Person();
            person.setName(name);
            people = personDao.read(person);

            // If they're null, the query will be like SELECT * FROM people

            // If there is any result
            if(people.size()>0){
                if(isCreating){
                    // Moves to the record that was just created
                    peopleCount = people.size()-1;

                    boolean isFirst = (peopleCount == 0);

                    // Disables the next button
                    // Enables the previous button if the record isn't the first
                    btNext.setEnabled(false);
                    btPrevious.setEnabled(!isFirst);
                }else{

                    // Moves to first record
                    peopleCount = 0;

                    boolean isLast = (peopleCount+1 == people.size());

                    // Disables the previous button
                    // Enables the next button if the record isn't the last
                    btNext.setEnabled(!isLast);
                    btPrevious.setEnabled(false);
                }

                // Displays the data in the fields
                displayData(peopleCount);

                llPrevNext.setVisibility(View.VISIBLE);

                return true;

            }else{
                Toast.makeText(MainActivity.this, getString(R.string.ttRead), Toast.LENGTH_SHORT).show();
                return false;
            }

        }catch(SQLException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttRead2), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.ttRead2)+": "+e.getMessage());
            return false;
        }

    }


    private boolean update(){

        // Gets the values of the EditText Widgets
        getValues();

        if(!etId.isEnabled() && !id.equals("") && !isUpdating){

            // Hide all buttons but btUpdate
            toggleEnableButtons(View.GONE);
            btUpdate.setVisibility(View.VISIBLE);

            // Enabled all EditText Widgets but etId
            toggleEnableEditText(true);
            etId.setEnabled(false);

            // Sets the flag to true
            isUpdating = true;
        }else if(!etId.isEnabled() && !id.equals("") && isUpdating && !name.equals("") && !phone.equals("") && !address.equals("")){
            try {

                Person person = new Person();
                person.setId(Long.valueOf(id));
                person.setName(name);
                person.setPhone(phone);
                person.setAddress(address);

                // Executes the update querry
                personDao.update(person);

                // Disables all Fields
                toggleEnableEditText(false);

                // Show all Buttons
                toggleEnableButtons(View.VISIBLE);

                // Calls default search
                search(null);

                // Resets flag
                isUpdating = false;

            }catch (SQLException e){
                Toast.makeText(MainActivity.this, getString(R.string.ttUpdate), Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.ttUpdate)+": "+e.getMessage());
            }

        }else{
            Toast.makeText(MainActivity.this, getString(R.string.ttFillF), Toast.LENGTH_SHORT).show();

        }

        return true;
    }

    private void delete(){
        // Getting the id


        // Making sure that there is a record
        if(!etId.isEnabled() && !etId.getText().toString().equals("")){

            // Creating the AlertDialog to prevent accidental clicks
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.adTitle));
            dialog.setMessage(getString(R.string.adMessage) + etName.getText().toString() + "?");

            // If the no button was clicked it dimisses the dialog
            dialog.setNegativeButton(getString(R.string.adbtNo), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            // If the yes button was clicked
            dialog.setPositiveButton(getString(R.string.adbtYes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Person person = new Person();
                        person.setId(Long.valueOf(etId.getText().toString()));
                        // runs the delete querry
                        personDao.delete(person);

                        clear();

                        // calls the default search
                        search(null);
                    } catch (SQLException e) {
                        Toast.makeText(MainActivity.this, getString(R.string.ttDelete), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, getString(R.string.ttDelete)+": " + e.getMessage());
                    }
                }
            });

            // Displays the AlertDialog
            dialog.create().show();

        }else{
            Toast.makeText(MainActivity.this, getString(R.string.ttDelete2), Toast.LENGTH_SHORT).show();
        }

    }



    private void previousResult(){
        try{
            // Move to previous record
            //cursor.moveToPrevious();
            peopleCount--;

            boolean isFirst = (peopleCount == 0);
            boolean isLast = (peopleCount+1 == people.size());

            // Enables or disables the browse button
            // if there's any record foward or backward
            btPrevious.setEnabled(!isFirst);
            btNext.setEnabled(!isLast);

            // Displays content in the fields
            displayData(peopleCount);

        }catch (SQLException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttPrevR), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.ttPrevR)+": "+e.getMessage());
        }
    }

    private void nextResult(){
        try{
            // Move to next record
            //cursor.moveToNext();
            peopleCount++;

            boolean isFirst = (peopleCount == 0);
            boolean isLast = (peopleCount+1 == people.size());

            // Enables or disables the browse button
            // if there's any record foward or backward
            btNext.setEnabled(!isLast);
            btPrevious.setEnabled(!isFirst);

            // Displays content in the fields
            displayData(peopleCount);

        }catch (SQLException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttNextR), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.ttNextR)+": "+e.getMessage());
        }
    }





    private void displayData(int index){
        try {
            // Setting the content of the fields with their proper values
            etId.setText(String.valueOf(people.get(index).getId()));
            etName.setText(people.get(index).getName());
            etPhone.setText(people.get(index).getPhone());
            etAddress.setText(people.get(index).getAddress());

            // Disables all fields
            toggleEnableEditText(false);

        }catch (ArrayIndexOutOfBoundsException e){
            Toast.makeText(MainActivity.this, getString(R.string.ttDisplayD), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.ttDisplayD)+": "+e.getMessage());
        }
    }

    private void getValues(){
        // Gets all EditText values
        id = etId.getText().toString();
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        address = etAddress.getText().toString();

    }

    private void toggleEnableEditText(boolean enabled){
        // Enables or disables the EditText Widgets
        // based on the enabled variable
        etId.setEnabled(enabled);
        etName.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
    }

    private void toggleEnableButtons(int visibility){
        // Sets the visibility of all buttons and
        // of the LinearLayout that carries
        // Next and previous buttton

        llPrevNext.setVisibility(visibility);

        btCreate.setVisibility(visibility);
        btRead.setVisibility(visibility);
        btUpdate.setVisibility(visibility);
        btDelete.setVisibility(visibility);

        // if all button were hidden
        if(visibility == View.GONE){
            // it shows the cancel button
            btCancel.setVisibility(View.VISIBLE);
        }else{
            // Otherwise it hides it
            btCancel.setVisibility(View.GONE);
        }
    }

    private void clear(){
        // Clear all fields
        etId.setText("");
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
    }


}
