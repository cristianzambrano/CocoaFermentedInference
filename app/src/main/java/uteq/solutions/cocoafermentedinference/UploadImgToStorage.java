package uteq.solutions.cocoafermentedinference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import Util.Helper;

public class UploadImgToStorage extends AppCompatActivity {

    private ImageView mImageView;
    FloatingActionButton btSubir;
    private StorageReference folderRef, imageRef;

    private UploadTask mUploadTask;
    String pathFile="";

    Spinner spinCat;

    FirebaseStorage storage ;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img_to_storage);

        btSubir  = findViewById(R.id.btSubir);
        btSubir.setEnabled(false);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        spinCat = (Spinner)findViewById(R.id.spnCat);

        Intent myIntent = getIntent();
        pathFile = myIntent.getStringExtra("pathFile");
        ImageView imagenVi = findViewById(R.id.image);
        imagenVi.setImageResource(R.drawable.iconcocoaapp);

        File imgFile = new  File(pathFile);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imagenVi.setImageBitmap(myBitmap);
            btSubir.setEnabled(true);
        }
    }


    public void onBtSubir(View view) {
        if(spinCat.getSelectedItemPosition()>=0) {
            uploadFromFile(pathFile, spinCat.getSelectedItem().toString());
        }


    }

    private void uploadFromFile(String path, String Category) {
        btSubir.setEnabled(true);

        Uri file = Uri.fromFile(new File(path));
        folderRef = storageRef.child("cocoabeans/" +  Category);

        final StorageReference imageRef = folderRef.child(file.getLastPathSegment());
        mUploadTask = imageRef.putFile(file);

        Helper.initProgressDialog(this);
        Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUploadTask.cancel();
            }
        });
        Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Pause", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUploadTask.pause();
            }
        });
        Helper.mProgressDialog.show();

        mUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Helper.dismissProgressDialog();
                Toast.makeText(getApplicationContext(),
                        "Error: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();


            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Helper.dismissProgressDialog();
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(getApplicationContext(),
                                "Imagen subida exitosamente..!",
                                Toast.LENGTH_LONG).show();
                        btSubir.setEnabled(false);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                Helper.setProgress(progress);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),
                        "Pausado: ",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}