package com.petinho.lucas.petinho.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.adapter.AdapterAnucios;
import com.petinho.lucas.petinho.helper.ConfiguracaoFirebase;
import com.petinho.lucas.petinho.helper.RecyclerItemClickListener;
import com.petinho.lucas.petinho.model.Anucio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MeusAnuciosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnucios;
    private List<Anucio> anucios = new ArrayList<>();
    private AdapterAnucios adapterAnucios;
    private DatabaseReference anucioUsuarioRef;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anucios);

        //Configuracoes iniciais
        anucioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anucios")
                .child(ConfiguracaoFirebase.getIdUsuario());

        inicializarComponentes();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnucioActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurar recyclerView
        recyclerAnucios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnucios.setHasFixedSize(true);
        adapterAnucios = new AdapterAnucios(anucios,this);
        recyclerAnucios.setAdapter(adapterAnucios);

        //Recupera anúcios para o usuário
        recuperarAnucios();

        //Adicionar evento de click no recyclerview
        recyclerAnucios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnucios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Anucio anucioSelecionado = anucios.get(position);
                                anucioSelecionado.remover();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }

                )
        );
    }

    private void recuperarAnucios(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúcios")
                .setCancelable(false)
                .build();
        dialog.show();

        anucioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anucios.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    anucios.add(ds.getValue(Anucio.class));
                }
                Collections.reverse(anucios);
                adapterAnucios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void inicializarComponentes(){
        recyclerAnucios = findViewById(R.id.recyclerAnucios);
    }

}
