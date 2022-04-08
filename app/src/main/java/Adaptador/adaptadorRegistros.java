package Adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import data.Registro;
import uteq.solutions.cocoafermentedinference.R;


public class adaptadorRegistros extends RecyclerView.Adapter<adaptadorRegistros.registroViewHolder> {

    private Context Ctx;
    private List<Registro> lstUsuarios;

    public adaptadorRegistros(Context mCtx, List<Registro> usuarios) {
        this.lstUsuarios = usuarios;
        Ctx=mCtx;
    }

    @Override
    public registroViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(Ctx);
        View view = inflater.inflate(R.layout.item_registro, null);
        return new registroViewHolder(view);
    }



    @Override
    public void onBindViewHolder(registroViewHolder holder, int position) {

        Registro usuario = lstUsuarios.get(position);

        holder.textViewCategoria.setText("Categoría: " + usuario.getCategoria());
        holder.textViewScore.setText("Score: " + usuario.getScore());
        holder.textViewFecha.setText("Fecha: " + usuario.getFecha());

        Glide.with(Ctx)
                .load(usuario.getImgUri())
                .into(holder.imageView);

    }


    @Override
    public int getItemCount() {
        return lstUsuarios.size();
    }


    class registroViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textViewScore, textViewFecha, textViewCategoria;
        ImageView imageView;

        public registroViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            textViewScore= itemView.findViewById(R.id.txtScore);
            textViewCategoria = itemView.findViewById(R.id.txtCategoria);
            textViewFecha = itemView.findViewById(R.id.txtFecha);
            imageView = itemView.findViewById(R.id.imgBean);
        }

        @Override
        public void onClick(View view) {

        }
    }

}