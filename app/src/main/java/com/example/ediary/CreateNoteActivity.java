package com.example.ediary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {


    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private ImageView imageNote;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;

    private String selectednoteColor;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2 ;

    private AlertDialog dialogAddURl;
    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_create_note);

        ImageView imageBack = findViewById (R.id.imageBack);
        imageBack.setOnClickListener (v -> onBackPressed ());

        inputNoteTitle = findViewById (R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById (R.id.inputNoteTitleSubtitle);
        inputNoteText = findViewById (R.id.inputNote);
        textDateTime = findViewById (R.id.textDateTime);
        viewSubtitleIndicator = findViewById (R.id.viewSubtitleIndicator);
        imageNote = findViewById (R.id.imageNote);
        textWebURL = findViewById (R.id.textWebURL);
        layoutWebURL = findViewById (R.id.layoutWebURL);


        textDateTime.setText (
                new SimpleDateFormat("EEEE, dd MMMM yyyy  HH:mm a", Locale.getDefault ())
                .format (new Date ())
        );

        ImageView imageSave = findViewById (R.id.imageSave);
        imageSave.setOnClickListener (v -> saveNote ());

        selectednoteColor = "#333333";
        selectedImagePath = " ";

        if (getIntent ().getBooleanExtra ("isViewOrUpdate", false)){
            alreadyAvailableNote = (Note)getIntent ().getSerializableExtra ("note");
            setViewOrUpdateNote ();
        }

        initMiscellaneous ();
        setSubtitleIndicatorColor ();
    }
    private void setViewOrUpdateNote(){
        inputNoteTitle.setText (alreadyAvailableNote.getTitle ());
        inputNoteSubtitle.setText (alreadyAvailableNote.getSubtitle ());
        inputNoteText.setText (alreadyAvailableNote.getNoteText ());
        textDateTime.setText (alreadyAvailableNote.getDate_time ());

        if (alreadyAvailableNote.getImagePath () !=null && !alreadyAvailableNote.getImagePath ().trim ().isEmpty ()){
            imageNote.setImageBitmap (BitmapFactory.decodeFile (alreadyAvailableNote.getImagePath ()));
            imageNote.setVisibility (View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath ();
        }

        if (alreadyAvailableNote.getWebLink () != null && !alreadyAvailableNote.getWebLink ().trim ().isEmpty ()){
            textWebURL.setText (alreadyAvailableNote.getWebLink ());
            layoutWebURL.setVisibility (View.VISIBLE);
        }
    }

    private void saveNote(){
        if (inputNoteTitle.getText ().toString ().trim ().isEmpty ()){
            Toast.makeText (this, "Note title can not be empty!", Toast.LENGTH_SHORT).show ();
            return;
        } else if (inputNoteSubtitle.getText ().toString ().trim ().isEmpty ()
                   && inputNoteText.getText ().toString ().trim ().isEmpty ()){
            Toast.makeText (this, "Note can not be empty!", Toast.LENGTH_SHORT).show ();
            return;
        }
        final Note note = new Note ();
        note.setTitle (inputNoteTitle.getText ().toString ());
        note.setSubtitle (inputNoteSubtitle.getText ().toString ());
        note.setNoteText (inputNoteText.getText ().toString ());
        note.setDate_time (textDateTime.getText ().toString ());
        note.setColor (selectednoteColor);
        note.setImagePath (selectedImagePath);

        if (layoutWebURL.getVisibility () == View.VISIBLE){
            note.setWebLink (textWebURL.getText ().toString ());
        }
        if (alreadyAvailableNote != null){
            note.setId (alreadyAvailableNote.getId ());
        }

        @SuppressLint("StaticFieldLeak")
        class saveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground (Void... voids){
                NotesDatabase.getDatabase (getApplicationContext ()).noteDao ().insertNote (note);
                return null;
            }
            @Override
            protected void onPostExecute(Void avoid){
                super.onPostExecute (avoid);
                Intent intent = new Intent ();
                setResult (RESULT_OK,intent);
                finish ();
            }
        }
        new saveNoteTask ().execute ();
    }
    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById (R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from (layoutMiscellaneous);
        layoutMiscellaneous.findViewById (R.id.textMiscellaneous).setOnClickListener (v -> {
            if (bottomSheetBehavior.getState () != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState (BottomSheetBehavior.STATE_EXPANDED);
            }else {
                bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById (R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById (R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById (R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById (R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById (R.id.imageColor5);

        layoutMiscellaneous.findViewById (R.id.viewColor).setOnClickListener (v -> {
            selectednoteColor = "#333333";
            imageColor1.setImageResource (R.drawable.ic_done_24);
            imageColor2.setImageResource (0);
            imageColor3.setImageResource (0);
            imageColor4.setImageResource (0);
            imageColor5.setImageResource (0);
            setSubtitleIndicatorColor ();
        });

        layoutMiscellaneous.findViewById (R.id.viewColor2).setOnClickListener (v -> {
            selectednoteColor = "#FDBE3B";
            imageColor1.setImageResource (0);
            imageColor2.setImageResource (R.drawable.ic_done_24);
            imageColor3.setImageResource (0);
            imageColor4.setImageResource (0);
            imageColor5.setImageResource (0);
            setSubtitleIndicatorColor ();
        });

        layoutMiscellaneous.findViewById (R.id.viewColor3).setOnClickListener (v -> {
            selectednoteColor = "#FF4842";
            imageColor1.setImageResource (0);
            imageColor2.setImageResource (0);
            imageColor3.setImageResource (R.drawable.ic_done_24);
            imageColor4.setImageResource (0);
            imageColor5.setImageResource (0);
            setSubtitleIndicatorColor ();
        });

        layoutMiscellaneous.findViewById (R.id.viewColor4).setOnClickListener (v -> {
            selectednoteColor = "#3A52Fc";
            imageColor1.setImageResource (0);
            imageColor2.setImageResource (0);
            imageColor3.setImageResource (0);
            imageColor4.setImageResource (R.drawable.ic_done_24);
            imageColor5.setImageResource (0);
            setSubtitleIndicatorColor ();
        });

        layoutMiscellaneous.findViewById (R.id.viewColor5).setOnClickListener (v -> {
            selectednoteColor = "#000000";
            imageColor1.setImageResource (0);
            imageColor2.setImageResource (0);
            imageColor3.setImageResource (0);
            imageColor4.setImageResource (0);
            imageColor5.setImageResource (R.drawable.ic_done_24);
            setSubtitleIndicatorColor ();
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor () !=null && !alreadyAvailableNote.getColor ().trim ().isEmpty ()){
            switch (alreadyAvailableNote.getColor ()){
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById (R.id.viewColor2).performClick ();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById (R.id.viewColor3).performClick ();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById (R.id.viewColor4).performClick ();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById (R.id.viewColor5).performClick ();
                    break;
            }
        }

        layoutMiscellaneous.findViewById (R.id.layoutAddImage).setOnClickListener (v -> {
          bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
          if (ContextCompat.checkSelfPermission (
                  getApplicationContext (), Manifest.permission.READ_EXTERNAL_STORAGE
          ) != PackageManager.PERMISSION_DENIED){
              ActivityCompat.requestPermissions (
                      CreateNoteActivity.this,
                      new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                      REQUEST_CODE_STORAGE_PERMISSION
              );
          }else{
              selectImage ();
          }
        });

        layoutMiscellaneous.findViewById (R.id.layoutAddUri).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog ();
            }
        });
    }
    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable)viewSubtitleIndicator.getBackground ();
        gradientDrawable.setColor (Color.parseColor (selectednoteColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage(){
          Intent intent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          if (intent.resolveActivity (getPackageManager ()) !=null){
              startActivityForResult (intent, REQUEST_CODE_SELECT_IMAGE);
          }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage ();
            }else {
                Toast.makeText (this, "Permission Denied !", Toast.LENGTH_SHORT).show ();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult (requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData ();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver ().openInputStream (selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream (inputStream);
                        imageNote.setImageBitmap (bitmap);
                        imageNote.setVisibility (View.VISIBLE);

                        selectedImagePath = getPathFromUri (selectedImageUri);

                    }catch (Exception exception){
                        Toast.makeText (this,exception.getMessage (), Toast.LENGTH_SHORT).show ();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver ()
                .query (contentUri, null, null,null,null);
        if (cursor == null){
            filePath = contentUri.getPath ();
        }else {
            cursor.moveToFirst ();
            int index = cursor.getColumnIndex ("_data");
            filePath = cursor.getString (index);

            cursor.close ();
        }
        return filePath;
    }

    private void showAddURLDialog(){
        if (dialogAddURl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder (CreateNoteActivity.this);
            View view = LayoutInflater.from (this).inflate (
                    R.layout.add_url,
                    (ViewGroup) findViewById (R.id.layoutAddUriContainer)
            );
            builder.setView (view);

            dialogAddURl = builder.create ();
            if (dialogAddURl.getWindow () != null){
                dialogAddURl.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
            }

            final EditText inputURL = view.findViewById (R.id.inputURL);
            inputURL.requestFocus ();

            view.findViewById (R.id.textAdd).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText ().toString ().trim ().isEmpty ()){
                        Toast.makeText (CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show ();
                    }else if (!Patterns.WEB_URL.matcher (inputURL.getText ().toString ()).matches()){
                        Toast.makeText (CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show ();
                    }else {
                        textWebURL.setText (inputURL.getText ().toString ());
                        layoutWebURL.setVisibility (View.VISIBLE);
                        dialogAddURl.dismiss ();
                    }
                }
            });

            view.findViewById (R.id.textCancel).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    dialogAddURl.dismiss ();
                }
            });
        }
        dialogAddURl.show ();
    }
}