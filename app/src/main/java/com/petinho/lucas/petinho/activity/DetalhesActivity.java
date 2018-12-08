package com.petinho.lucas.petinho.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.model.Anucio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class DetalhesActivity extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView titulo;
    private TextView descricao;
    private TextView estado;
    private TextView preco;
    private TextView telefone;
    private Anucio anucioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        //Configurar toolbar
        getSupportActionBar().setTitle("Detalhe Animal");
        //Inicializar componentes da interface
        inicializarComponentes();

        //Recupera anucio para exibicao
        anucioSelecionado = (Anucio) getIntent().getSerializableExtra("anucioSelecionado");

        if(anucioSelecionado != null){
            titulo.setText(anucioSelecionado.getTitulo());
            descricao.setText(anucioSelecionado.getDescricao());
            telefone.setText(anucioSelecionado.getTelefone());
            //estado.setText(anucioSelecionado.getEstado());
            preco.setText(anucioSelecionado.getValor());

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = anucioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };

            carouselView.setPageCount(anucioSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);
        }
    }

    public void visualizarTelefone(View view){
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anucioSelecionado.getTelefone(), null));
        startActivity(i);
    }

    private void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselView);
        titulo = findViewById(R.id.textTituloDetalhe);
        telefone = findViewById(R.id.textTelefoneDetalhe);
        descricao = findViewById(R.id.textDescricaoDetalhe);
        estado = findViewById(R.id.textEstadoDetalhe);
        preco = findViewById(R.id.textPrecoDetalhe);
    }
}
