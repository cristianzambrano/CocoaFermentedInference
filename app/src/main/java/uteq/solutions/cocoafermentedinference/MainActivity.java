package uteq.solutions.cocoafermentedinference;

import androidx.annotation.NonNull;
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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;


import data.Registro;
import data.RegistrosDbHelper;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private String mOpenFileId;

    TextView Result;
    TextView Result2;
    ImageView imagenVi;
    int imageSize = 224;
    String pathFile, IDImg;
    Button btnuevo, btguardar, btexportar;

    boolean Infered=false;
    double InferedScore=0;
    String InferedCat ="";
    String InferedDate="";

    Interpreter  interpreter;
    private RegistrosDbHelper registrosDbHelper;

    ArrayList<String> categorias;
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

        btnuevo = findViewById(R.id.btn_nuevo);
        btguardar = findViewById(R.id.save_btn);
        btexportar = findViewById(R.id.btfirebase);

        btnuevo.setEnabled(true);
        btguardar.setEnabled(false);
        btexportar.setEnabled(false);

        categorias  = new ArrayList<String>(4);
        categorias.add("Buena Fermentación");
        categorias.add("Mediana Fermentación");
        categorias.add("Mohoso");
        categorias.add("Violeta");

        downloadModel();

    }

    public void obBtExportar(View view) {
        if(Infered){
            if(pathFile!="") {
                Intent myIntent = new Intent(this, UploadImgToStorage.class);
                myIntent.putExtra("pathFile",pathFile);
                startActivity(myIntent);

            }
        }else
            onBtNuevo(null);

    }

    public void onBtGuardarLocal(View view) {
     if(Infered){

         Bitmap imagen=((BitmapDrawable)imagenVi.getDrawable()).getBitmap();
         IDImg= UUID.randomUUID().toString();
         pathFile  = saveImg(IDImg,"imgsTFL",imagen);
         if(pathFile!=""){
             Registro registro = new Registro(IDImg,
                     String.format("%.2f", (100 * InferedScore)),
                     InferedCat,InferedDate, pathFile);

             if(registrosDbHelper.saveRegistro(registro)>0) {
                 Toast.makeText(getApplicationContext(), "Resultado almacenado!!", Toast.LENGTH_LONG).show();
                 btguardar.setEnabled(false); btexportar.setEnabled(true);
             }else
                 Toast.makeText(getApplicationContext(),"Resultado NO almacenado",Toast.LENGTH_LONG).show();;

         }else{
             Toast.makeText(getApplicationContext(),"Error al Guardar Imagen",Toast.LENGTH_LONG).show();
         }

     }else{
         onBtNuevo(null);
     }
    }


    public void onBtNuevo(View view) {
        Infered=false;
        InferedScore=0;
        InferedCat ="";

        pathFile="";

        btnuevo.setEnabled(true);
        btguardar.setEnabled(false);
        btexportar.setEnabled(false);

        Result.setText("Primer Resultado: 0.00%");
        Result2.setText("Resultados secundarios: 0.00%");
        imagenVi.setImageResource(R.drawable.iconcocoaapp);


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
            case R.id.mmnuDownloadModelo:
                downloadModel();
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

        onBtNuevo(null);

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap imagen = (Bitmap) data.getExtras().get("data");

                int dimension = Math.min(imagen.getWidth(), imagen.getHeight());
                //imagen = ThumbnailUtils.extractThumbnail(imagen, imageSize, imageSize);
                //imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                imagenVi.setImageBitmap(imagen);

                classifyImage(imagen);

            }else {
                Uri dat = data.getData();
                Bitmap imagen = null;
                try {
                    imagen = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                    //imagen = Bitmap.createScaledBitmap(imagen, imageSize, imageSize, false);
                    imagenVi.setImageBitmap(imagen);

                    classifyImage(imagen);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void downloadModel(){

        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("fermentedcocoaclassifier", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        Toast.makeText(getApplicationContext(),
                                "Modelo descargado: " + model.getName() +
                                        ". Size: " + model.getSize(),
                                Toast.LENGTH_LONG).show();
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                             interpreter = new Interpreter(modelFile);

                        }
                    }
                });
    }

    public int getIndexMaxScore(FloatBuffer probabilities){
       float MaxScore=0; int pos=0;
        for (int i = 0; i < probabilities.capacity(); i++) {
            float v=probabilities.get(i);
            if(probabilities.get(i) >= MaxScore) {
                pos = i;
                MaxScore=probabilities.get(i);
            }
        }
        return pos;
    }


    public ByteBuffer getImageAsByteBuffer(Bitmap image){
        ImageProcessor imageProcessor;
        TensorImage xceptionTfliteInput;
        imageProcessor =
                    new ImageProcessor.Builder()
                            .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                            .add(new NormalizeOp(0, 255))
                            .build();
        xceptionTfliteInput = new TensorImage(DataType.FLOAT32);

        xceptionTfliteInput.load(image);
        xceptionTfliteInput = imageProcessor.process(xceptionTfliteInput);
        return  xceptionTfliteInput.getBuffer();
}


    public void classifyImage(Bitmap image) {
        ByteBuffer input = getImageAsByteBuffer(image);
        int bufferSize = 4 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();

        int first = getIndexMaxScore(probabilities);
        Result.setText(categorias.get(first) + ": " + String.format("%.2f", (100 * probabilities.get(first))) + "%");

        Calendar calendar;
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        InferedScore = probabilities.get(first);
        InferedCat = categorias.get(first);
        InferedDate = dateFormat.format(calendar.getTime());
        Infered=true;
        btguardar.setEnabled(true);


        String texto="";
        for (int i = 0; i < probabilities.capacity(); i++){
            if(i!=first){
                texto =  texto + categorias.get(i) + ": " +  String.format("%.2f", (100 * probabilities.get(i))) + "%\n";
            }
        }
        Result2.setText(texto);



    }

}