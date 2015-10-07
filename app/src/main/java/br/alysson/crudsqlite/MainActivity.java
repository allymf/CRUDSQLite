package br.alysson.crudsqlite;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase sqliteDatabase = null;
    Cursor cursor;
    final String DATABASENAME = "CRUD";

    final String TABLENAME = "people";

    final String COLLUM_ID = "id";
    final String COLLUM_NAME = "name";
    final String COLLUM_PHONE = "phone";
    final String COLLUM_ADDRESS = "address";

    private static final String TAG = "CRUDSQLite";

    LinearLayout llPrevNext;
    EditText etID,etName,etPhone,etAddress;
    Button btCreate,btRead,btUpdate,btDelete,btPrevious,btNext,btCancel;

    String id, name, phone, address;

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
        btCancel   = (Button) findViewById(R.id.btCancel);

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

                if(!reading){
                    llPrevNext.setVisibility(View.GONE);
                    toggleEnableButtons(View.GONE);
                    btRead.setVisibility(View.VISIBLE);

                    toggleEnableEditTexts(false);
                    etName.setEnabled(true);
                    clear();

                    reading=true;
                }else {
                    name = etName.getText().toString();
                    String search = "name LIKE ?";
                    read(search,new String[]{"%"+name+"%"});

                    toggleEnableButtons(View.VISIBLE);

                    reading = false;
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
                creating = false;
                reading = false;
                updating = false;

                toggleEnableButtons(View.VISIBLE);

                read(null,null);
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
            sqliteDatabase = openOrCreateDatabase(DATABASENAME,MODE_PRIVATE,null);
            String sql = "CREATE TABLE IF NOT EXISTS people " +
                        "( id INTEGER PRIMARY KEY," +
                        "name TEXT,phone TEXT, " +
                        "address TEXT);";
            sqliteDatabase.execSQL(sql);
            read(null,null);

        }catch(SQLException e){
            Toast.makeText(MainActivity.this,"Erro com banco de dados",Toast.LENGTH_LONG).show();
            Log.e(TAG,"Excecao no banco: "+e.getMessage());
        }
    }

    private void create(){

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

            toggleEnableButtons(View.GONE);
            btCreate.setVisibility(View.VISIBLE);
            btCreate.setText(getString(R.string.btSave));

            creating = true;
        }else{
            // Create Code Here
            if(!name.equals("") && !phone.equals("") && !address.equals("")) {
                try {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLLUM_NAME,name);
                    contentValues.put(COLLUM_PHONE,phone);
                    contentValues.put(COLLUM_ADDRESS,address);
                    sqliteDatabase.insert(TABLENAME,null,contentValues);

                    clear();
                    toggleEnableButtons(View.VISIBLE);
                    btCreate.setText(getString(R.string.btCreate));

                    read(null,null);
                    creating = false;



                }catch (SQLException e){
                    Toast.makeText(MainActivity.this, "Erro ao inserir no banco", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Erro ao inserir banco: "+e.getMessage());
                }

            }else{
                Toast.makeText(MainActivity.this, "Prencha todos os campos", Toast.LENGTH_SHORT).show();
                creating = false;
                read(null,null);
            }


        }

    }

    private boolean read(@Nullable String selection,@Nullable String[] selectionArgs){
        etID.setEnabled(false);

        try{
            cursor = sqliteDatabase.query(TABLENAME, null,selection,selectionArgs,null,null,null,null);
            if(cursor.getCount()>0){
                if(creating){
                    cursor.moveToLast();
                    btNext.setEnabled(false);
                    btPrevious.setEnabled(!cursor.isFirst());
                }else{
                    cursor.moveToFirst();
                    btNext.setEnabled(!cursor.isLast());
                    btPrevious.setEnabled(false);
                }
                displayData();
                llPrevNext.setVisibility(View.VISIBLE);
                return true;
            }else{
                Toast.makeText(MainActivity.this, "Nenhum registro semelhante", Toast.LENGTH_SHORT).show();
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
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        address = etAddress.getText().toString();

        if(!etID.isEnabled() && !id.equals("") && !updating){
            updating = true;

            llPrevNext.setVisibility(View.GONE);

            toggleEnableButtons(View.GONE);
            btUpdate.setVisibility(View.VISIBLE);

            toggleEnableEditTexts(true);
            etID.setEnabled(false);

        }else if(!etID.isEnabled() && !id.equals("") && updating && !name.equals("") && !phone.equals("") && !address.equals("")){
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLLUM_NAME,name);
                contentValues.put(COLLUM_PHONE,phone);
                contentValues.put(COLLUM_ADDRESS,address);
                sqliteDatabase.update(TABLENAME, contentValues, "id = ?", new String[]{id});

                toggleEnableEditTexts(false);
                toggleEnableButtons(View.VISIBLE);
                read(null, null);
                llPrevNext.setVisibility(View.VISIBLE);
                updating = false;

            }catch (SQLException e){
                Toast.makeText(MainActivity.this, "Erro ao atualizar registros", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Erro ao atualizar registro: "+e.getMessage());
            }

        }else{
            Toast.makeText(MainActivity.this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show();

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
                        sqliteDatabase.delete(TABLENAME,"id = ?",new String[]{id});
                        read(null,null);
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

    private void toggleEnableButtons(int visibility){
        btCreate.setVisibility(visibility);
        btRead.setVisibility(visibility);
        btUpdate.setVisibility(visibility);
        btDelete.setVisibility(visibility);

        if(visibility == View.GONE){
            btCancel.setVisibility(View.VISIBLE);
        }else{
            btCancel.setVisibility(View.GONE);
        }
    }

    private void clear(){
        etID.setText("");
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
    }


}
