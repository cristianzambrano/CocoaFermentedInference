package uteq.solutions.cocoafermentedinference;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import uteq.solutions.cocoafermentedinference.ml.FermentedCocoaBean;

public class MainActivity extends AppCompatActivity {

    TextView Result;
    TextView Result2;
    Button camara, galeria;
    ImageView imagenVi;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Result = findViewById(R.id.txtResult);
        Result2= findViewById(R.id.txtResult2);
        camara = findViewById(R.id.btn_TomarFoto);
        galeria = findViewById(R.id.button2);
        imagenVi = findViewById(R.id.image);


        camara.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent camaraInten = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camaraInten, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }

        });
        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camaraInten = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(camaraInten, 1);
            }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap imagen = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(imagen.getWidth(), imagen.getHeight());
                imagen = ThumbnailUtils.extractThumbnail(imagen, dimension, dimension);
                imagenVi.setImageBitmap(imagen);

                imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                classifyImage(imagen);

            } else {
                Uri dat = data.getData();
                Bitmap imagen = null;
                try {
                    imagen = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                imagenVi.setImageBitmap(imagen);
                classifyImage(imagen);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void classifyImage(Bitmap images) {
        try {


            FermentedCocoaBean model = FermentedCocoaBean.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(images);

            // Runs model inference and gets result.
            FermentedCocoaBean.Outputs outputs = model.process(image);

            List<Category> probability = outputs.getProbabilityAsCategoryList();


                /*float[] probability = outputs.getProbabilityAsTensorBuffer().getFloatArray();
                int maxPos = 0;
                float maxPosibility = 0;
                for (int i = 0; i < probability.length; i++) {
                    if (probability[i] > maxPosibility) {
                        maxPosibility = probability[i];
                        maxPos = i;
                    }
                }*/


            String texto="";
            float MaxScore=0; int first=-1;
            Category recognition;
            for (int i = 0; i < probability.size(); i++){
                recognition = probability.get(i);
                if(recognition.getScore() > MaxScore) {
                    first = i;
                    MaxScore = recognition.getScore();
                }
            }

            recognition = probability.get(first);
            Result.setText(recognition.getLabel() + ": " + String.format("%.2f", (100 * recognition.getScore())) + "%");

            texto="";
            for (int i = 0; i < probability.size(); i++){
                if(i!=first){
                     recognition = probability.get(i);
                     texto =  texto + recognition.getLabel() + ": " +  String.format("%.2f", (100 * recognition.getScore())) + "%\n";
                }
            }
            Result2.setText(texto);

            model.close();

        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
}