package uteq.solutions.cocoafermentedinference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import java.util.ArrayList;

import Adaptador.adaptadorRegistros;
import data.Registro;
import data.RegistrosDbHelper;
import uteq.solutions.cocoafermentedinference.R;


public class ListaregistroActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private RegistrosDbHelper registrosDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listaregistro_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());




        ArrayList<Registro> lstRegistros = new ArrayList<Registro> ();
        registrosDbHelper = new RegistrosDbHelper(this.getApplicationContext());
        lstRegistros = registrosDbHelper.getArrayAllRegistros();

        adaptadorRegistros adapatorRegistro = new adaptadorRegistros(this, lstRegistros);

        int resId = R.anim.layout_animation_down_to_up;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(),
                resId);
        recyclerView.setLayoutAnimation(animation);

        recyclerView.setAdapter(adapatorRegistro);


    }
}