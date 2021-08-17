package com.example.ediary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener{

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById (R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener (v -> startActivityForResult (
                new Intent (getApplicationContext (), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));
        notesRecyclerView = findViewById (R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager (
                new StaggeredGridLayoutManager (2,StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<> ();
        notesAdapter = new NotesAdapter (noteList, this);
        notesRecyclerView.setAdapter (notesAdapter);

        getNotes ();
    }
    @Override
    public void onNoteClicked(Note note, int position){
        noteClickedPosition = position;
        Intent intent = new Intent (getApplicationContext (),CreateNoteActivity.class);
        intent.putExtra ("isViewOrUpdate", true);
        intent.putExtra ("note", note);
        startActivityForResult (intent,REQUEST_CODE_UPDATE_NOTE);
    }

        private void getNotes(){

                    @SuppressLint("StaticFieldLeak")
                    class GetNotesTask extends AsyncTask<Void, Void, List<Note>>{
                        @Override
                        protected List<Note> doInBackground(Void... voids){
                            return NotesDatabase
                                    .getDatabase (getApplicationContext ())
                                    .noteDao ().getAllNotes ();
                        }
                        @Override
                        protected void onPostExecute(List<Note> notes){
                            super.onPostExecute (notes);
                            if (noteList.size ()==0){
                                noteList.addAll (notes);
                                notesAdapter.notifyDataSetChanged ();
                            }else {
                                noteList.add (0, notes.get (0));
                                notesAdapter.notifyItemInserted (0);
                            }
                            notesRecyclerView.smoothScrollToPosition (0);
                        }
                    }
                    new GetNotesTask ().execute ();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data){
        super.onActivityResult (requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes ();
        }
    }
}