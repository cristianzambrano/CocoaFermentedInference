package utc.solutions.cocoafermentedinference;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import data.Registro;
import data.RegistrosDbHelper;
import uteq.solutions.cocoafermentedinference.R;
import uteq.solutions.cocoafermentedinference.ml.FermentedCocoaBean;

public class MainActivity extends AppCompatActivity {

    TextView Result;
    TextView Result2;
    ImageView imagenVi;
    int imageSize = 224;
    String pathFile, IDImg;

    private RegistrosDbHelper registrosDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        registrosDbHelper = new RegistrosDbHelper(this.getApplicationContext());

        Result = findViewById(R.id.txtResult);
        Result2= findViewById(R.id.txtResult2);

        imagenVi = findViewById(R.id.image);




    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClick(View view) {

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent camaraInten = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camaraInten, 3);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }



    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menutoolbar , menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mmnuViewRegs:
                Intent intent = new Intent(this, ListaregistroActivity.class);
                startActivity(intent);
                return true;
            case R.id.mnuGaleria:
                Intent camaraInten = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(camaraInten, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String saveImg(String Name, String Path, Bitmap bitmapImg ){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(Path, Context.MODE_PRIVATE);
        File file = new File(directory, Name + ".jpg");
        if (!file.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                return file.toString();
            } catch (java.io.IOException e) {
                return "";
            }
        } else
            return file.toString();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Result.setText(""); Result2.setText("");

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap imagen = (Bitmap) data.getExtras().get("data");

                int dimension = Math.min(imagen.getWidth(), imagen.getHeight());
                imagen = ThumbnailUtils.extractThumbnail(imagen, imageSize, imageSize);
                imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                imagenVi.setImageBitmap(imagen);


                IDImg= UUID.randomUUID().toString();
                pathFile  = saveImg(IDImg,"imgsTFL",imagen);
                if(pathFile!=""){
                    classifyImage(IDImg, imagen, pathFile);
                }else{
                    Toast.makeText(getApplicationContext(),"Error al Guardar Imagen",Toast.LENGTH_LONG).show();
                }


            } else {
                Uri dat = data.getData();
                Bitmap imagen = null;
                try {
                    imagen = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                    imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                    imagenVi.setImageBitmap(imagen);

                    IDImg= UUID.randomUUID().toString();
                    pathFile  = saveImg(IDImg,"imgsTFL",imagen);
                    if(pathFile!=""){
                        classifyImage(IDImg, imagen, pathFile);
                    }else{
                        Toast.makeText(getApplicationContext(),"Error al Guardar Imagen",Toast.LENGTH_LONG).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void classifyImage(String ID, Bitmap images, String PathImg) {
        try {


            FermentedCocoaBean model = FermentedCocoaBean.newInstance(getApplicationContext());
            TensorImage image = TensorImage.fromBitmap(images);
            FermentedCocoaBean.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();


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

            Calendar calendar;
            calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


            Registro registro = new Registro(ID,
                    String.format("%.2f", (100 * recognition.getScore())),
                    recognition.getLabel(),dateFormat.format(calendar.getTime()), PathImg);

            if(registrosDbHelper.saveRegistro(registro)>0)
                Toast.makeText(getApplicationContext(),"Resultado almacenado " ,Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(),"Resultado NO almacenado",Toast.LENGTH_LONG).show();;


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