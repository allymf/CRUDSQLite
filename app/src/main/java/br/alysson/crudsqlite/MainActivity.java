package br.alysson.crudsqlite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase sqliteDatabase = null;
    Cursor cursor;
    final String databaseName = "CRUD";

    private static final String TAG = "CRUDSQLite";

    LinearLayout llPrevNext;
    EditText etID,etName,etPhone,etAddress;
    Button btCreate,btRead,btUpdate,btDelete,btPrevious,btNext;


    boolean creating = false;
    boolean reading = false;
    boolean updating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llPrevNext = (LinearLayout) findViewById(R.id.llPrevNext);

        etID      = (EditText) findViewById(R.id.etID);
        etName    = (EditText) findViewById(R.id.etName);
        etPhone   = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);

        btPrevious = (Button) findViewById(R.id.btPrevious);
        btNext     = (Button) findViewById(R.id.btNext);
        btCreate   = (Button) findViewById(R.id.btCreate);
        btRead     = (Button) findViewById(R.id.btRead);
        btUpdate   = (Button) findViewById(R.id.btUpdate);
        btDelete   = (Button) findViewById(R.id.btDelete);

        etName.requestFocus();

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
                create();
            }
        });

        btRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!updating){
                    toggleEnableButtons(false);
                    btRead.setEnabled(true);

                    toggleEnableEditTexts(true);
                    clear();
                }else{

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

        openOrCreateDatabase();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            sqliteDatabase.close();
        }catch (SQLException e){
            Toast.makeText(MainActivity.this, "Erro ao fechar o banco", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Erro ao fechar banco: "+e.getMessage());
        }

    }

    private void openOrCreateDatabase(){
        try{
            sqliteDatabase = openOrCreateDatabase(databaseName,MODE_PRIVATE,null);
            String sql = "CREATE TABLE IF NOT EXISTS people " +
                        "( id INTEGER PRIMARY KEY," +
                        "name TEXT,phone TEXT, " +
                        "address TEXT);";
            sqliteDatabase.execSQL(sql);
            read(null);

        }catch(SQLException e){
            Toast.makeText(MainActivity.this,"Erro com banco de dados",Toast.LENGTH_LONG).show();
            Log.e(TAG,"Excecao no banco: "+e.getMessage());
        }
    }

    private void create(){
        String name, phone, address;
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        address = etAddress.getText().toString();

        llPrevNext.setVisibility(View.GONE);

        if(!creating) {

            if(!name.equals("") || !phone.equals("") || !address.equals("")) {
                clear();
            }else{
                etID.setText("");
            }

            toggleEnableEditTexts(true);
            etID.setEnabled(false);
            etName.requestFocus();

            toggleEnableButtons(false);
            btCreate.setEnabled(true);
            btCreate.setText(getString(R.string.btSave));

            creating = true;
        }else{
            // Create Code Here
            if(!name.equals("") && !phone.equals("") && !address.equals("")) {
                try {


                    String sql = "INSERT INTO people (name,phone,address) VALUES( '" + name + "', '" + phone + "', '" + address + "' );";
                    sqliteDatabase.execSQL(sql);

                    clear();
                    toggleEnableButtons(true);
                    btCreate.setText(getString(R.string.btCreate));

                    read(null);
                    creating = false;



                }catch (SQLException e){
                    Toast.makeText(MainActivity.this, "Erro ao inserir no banco", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Erro ao inserir banco: "+e.getMessage());
                }

            }else{
                Toast.makeText(MainActivity.this, "Prencha todos os campos", Toast.LENGTH_SHORT).show();
            }


        }

    }

    private boolean read(@Nullable String selection){
        etID.setEnabled(false);

        try{
            cursor = sqliteDatabase.query("people", null,selection,null,null,null,null,null);

            if(cursor.getCount()>0){
                if(creating || updating){
                    cursor.moveToLast();
                    btNext.setEnabled(false);
                    btPrevious.setEnabled(true);
                }else{
                    cursor.moveToFirst();
                    btNext.setEnabled(true);
                    btPrevious.setEnabled(false);
                }
                displayData();
                llPrevNext.setVisibility(View.VISIBLE);
                return true;
            }else{
                return false;
            }

        }catch(SQLException e){
            Toast.makeText(MainActivity.this, "Erro ao pesquisar no banco", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Erro ao pesquisar o banco: "+e.getMessage());
            return false;
        }

    }


    private boolean update(){
        final String id = etID.getText().toString();
        String name, phone, address;
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        address = etAddress.getText().toString();

        if(!etID.isEnabled() && !id.equals("") && !updating){
            updating = true;

            llPrevNext.setVisibility(View.GONE);
            toggleEnableButtons(false);
            btUpdate.setEnabled(true);
            toggleEnableEditTexts(true);
            etID.setEnabled(false);

        }else if(!etID.isEnabled() && !id.equals("") && updating && !name.equals("") && !phone.equals("") && !address.equals("")){
            try {
                String sql = "UPDATE people SET name='"+name+"', phone='"+phone+"', address='"+address+"' WHERE id = "+id+";";
                sqliteDatabase.execSQL(sql);

                toggleEnableEditTexts(false);
                toggleEnableButtons(true);
                read(null);
                llPrevNext.setVisibility(View.VISIBLE);
                updating = false;

            }catch (SQLException e){
                Toast.makeText(MainActivity.this, "Erro ao atualizar registros", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Erro ao atualizar registro: "+e.getMessage());
            }

        }

        return true;
    }

    private boolean delete(){
        final String id = etID.getText().toString();
        if(!etID.isEnabled() && !id.equals("")){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Warning!");
            dialog.setMessage("Are you sure, you want to delete the entry with name " + etName.getText().toString() + "?");

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        sqliteDatabase.delete("people", "id = " + id, null);
                        read(null);
                    } catch (SQLException e) {
                        Toast.makeText(MainActivity.this, "Erro ao excluir registro", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao excluir do banco: " + e.getMessage());
                    }
                }
            });

            dialog.create().show();

        }else{
            Toast.makeText(MainActivity.this, "Operação Proibida", Toast.LENGTH_SHORT).show();
        }


        return true;
    }



    private void previousResult(){
        try{
            if(!cursor.isFirst()) {
                cursor.moveToPrevious();
                btPrevious.setEnabled(!cursor.isFirst());
                btNext.setEnabled(!cursor.isLast());
                displayData();
            }
        }catch (SQLException e){
            Toast.makeText(MainActivity.this, "Não foi possivel mostrar o anterior", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Erro ao regressar no cursor: "+e.getMessage());
        }
    }

    private void nextResult(){
        try{
                if(!cursor.isLast()) {
                    cursor.moveToNext();
                    btNext.setEnabled(!cursor.isLast());
                    btPrevious.setEnabled(true);
                    displayData();
                }

        }catch (SQLException e){
            Toast.makeText(MainActivity.this, "Não foi possivel mostrar o próximo", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Erro ao avancar no cursor: "+e.getMessage());
        }
    }





    private void displayData(){
        try {
            etID.setText(cursor.getString(0));
            etName.setText(cursor.getString(1));
            etPhone.setText(cursor.getString(2));
            etAddress.setText(cursor.getString(3));


            toggleEnableEditTexts(false);
        }catch (SQLException e){
            Toast.makeText(MainActivity.this, "Erro ao mostrar dados", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Erro ao mostrar dados: "+e.getMessage());
        }
    }

    private void toggleEnableEditTexts(boolean enabled){
        etID.setEnabled(enabled);
        etName.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
    }

    private void toggleEnableButtons(boolean enabled){
        btCreate.setEnabled(enabled);
        btRead.setEnabled(enabled);
        btUpdate.setEnabled(enabled);
        btDelete.setEnabled(enabled);
    }

    private void clear(){
        etID.setText("");
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
