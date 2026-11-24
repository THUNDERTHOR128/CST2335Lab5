
package com.example.lab5;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private EditText todoInput;
    private Switch urgentSwitch;
    private ListView todoList;

    private final ArrayList<Todo> todos = new ArrayList<>();
    private ArrayAdapter<Todo> adapter;

    private static final String TAG = "Lab5Database";

    private static class Todo {

        long id;
        String message;
        boolean urgent;

        Todo(long id, String message, boolean urgent) {
            this.id = id;
            this.message = message;
            this.urgent = urgent;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.ime());
                    v.setPadding(bars.left, bars.top,
                            bars.right, bars.bottom);
                    return insets;
                });

        todoInput = findViewById(R.id.todoInput);
        urgentSwitch = findViewById(R.id.urgentSwitch);
        todoList = findViewById(R.id.todoList);
        Button addButton = findViewById(R.id.addButton);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        adapter = new ArrayAdapter<Todo>(
                this,
                android.R.layout.simple_list_item_1,
                todos) {

            @Override
            public View getView(int position,
                                View convertView,
                                ViewGroup parent) {

                TextView view = (TextView) super.getView(
                        position, convertView, parent);

                Todo todo = getItem(position);

                if (todo != null && todo.urgent) {
                    view.setBackgroundColor(Color.RED);
                    view.setTextColor(Color.WHITE);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                    view.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        todoList.setAdapter(adapter);

        loadTodos();

        addButton.setOnClickListener(v -> addTodo());

        todoList.setOnItemLongClickListener(
                (parent, view, position, id) ->
                        onItemLongClickHandler(position));
    }

    private void loadTodos() {

        todos.clear();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,
                null, null, null,
                null, null,
                DatabaseHelper.COL_ID + " ASC");

        printCursor(cursor);

        while (cursor.moveToNext()) {

            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_ID));

            String message = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_MESSAGE));

            boolean urgent = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_URGENT)) == 1;

            todos.add(new Todo(id, message, urgent));
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void addTodo() {

        String message = todoInput.getText()
                .toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this,
                    R.string.empty_message,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean urgent = urgentSwitch.isChecked();

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COL_MESSAGE, message);
        values.put(DatabaseHelper.COL_URGENT,
                urgent ? 1 : 0);

        long id = db.insert(
                DatabaseHelper.TABLE_NAME,
                null, values);

        if (id != -1) {
            todos.add(new Todo(id, message, urgent));
            adapter.notifyDataSetChanged();

            todoInput.setText("");
            urgentSwitch.setChecked(false);
        } else {
            Log.e(TAG, "Failed to insert task");
        }
    }

    private boolean onItemLongClickHandler(int position) {

        Todo todo = todos.get(position);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_message)

                .setPositiveButton(R.string.yes,
                        (dialog, which) -> {

                            int deleted = db.delete(
                                    DatabaseHelper.TABLE_NAME,
                                    DatabaseHelper.COL_ID + " = ?",
                                    new String[]{
                                            String.valueOf(todo.id)
                                    });

                            if (deleted > 0) {
                                todos.remove(todo);
                                adapter.notifyDataSetChanged();
                            }
                        })

                .setNegativeButton(R.string.no, null)
                .show();

        return true;
    }

    private void printCursor(Cursor c) {

        Log.d(TAG, "Database version: " + db.getVersion());

        Log.d(TAG, "Number of columns: "
                + c.getColumnCount());

        Log.d(TAG, "Column names: "
                + Arrays.toString(c.getColumnNames()));

        Log.d(TAG, "Number of results: "
                + c.getCount());

        int originalPosition = c.getPosition();

        c.moveToPosition(-1);

        while (c.moveToNext()) {

            StringBuilder row = new StringBuilder();

            for (int i = 0; i < c.getColumnCount(); i++) {
                row.append(c.getColumnName(i))
                        .append(": ")
                        .append(c.getString(i))
                        .append(" | ");
            }

            Log.d(TAG, "Row: " + row);
        }

        c.moveToPosition(originalPosition);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
