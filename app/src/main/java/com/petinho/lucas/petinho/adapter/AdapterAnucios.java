package com.petinho.lucas.petinho.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.model.Anucio;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterAnucios extends RecyclerView.Adapter<AdapterAnucios.MyViewHolder> {

    private List<Anucio> anucios;
    private Context context;

    public AdapterAnucios(List<Anucio> anucios, Context context) {
        this.anucios = anucios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_anucio, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Anucio anucio = anucios.get(position);
        holder.titulo.setText(anucio.getTitulo());
        holder.valor.setText(anucio.getDescricao());

        //Pega primeira imagem da lista
        List<String> urlFotos = anucio.getFotos();
        String urlCapa = urlFotos.get(0);
        //Picasso.get().load(urlCapa).into(holder.foto);
        Picasso.get().load(urlCapa).into(holder.foto);
    }

    @Override
    public int getItemCount() {
        return anucios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView titulo;
        TextView valor;
        ImageView foto;

        public MyViewHolder(View itemView){
            super(itemView);

            titulo = itemView.findViewById(R.id.textTitulo);
            valor = itemView.findViewById(R.id.textPreco);
            foto = itemView.findViewById(R.id.imageAnucio);
        }
    }
}
