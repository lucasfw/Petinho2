package com.petinho.lucas.petinho.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class AnuciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnuciosPublicos;
    private Button buttonRegiao, buttonCategoria;
    private AdapterAnucios adapterAnucios;
    private List<Anucio> listaAnucios = new ArrayList<>();
    private DatabaseReference anuciosPublicosRef;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private Boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anucios);

        inicializarComponentes();

        //Configuracoes Iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anuciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anucios");

        //Configurar recyclerView
        recyclerAnuciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuciosPublicos.setHasFixedSize(true);
        adapterAnucios = new AdapterAnucios(listaAnucios,this);
        recyclerAnuciosPublicos.setAdapter(adapterAnucios);
        //autenticacao.signOut();

        recuperarAnuciosPublicos();

        //Aplicar o evento de clique
        recyclerAnuciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnuciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Anucio anucioSelecionado = listaAnucios.get(position);
                                Intent i = new Intent(AnuciosActivity.this, DetalhesActivity.class);
                                i.putExtra("anucioSelecionado", anucioSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );
    }

    public void filtrarPorEstado(View view){

        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");

        //Configurar o Spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        //Configura spinner de estados
        final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnuciosPorEstado();
                filtrandoPorEstado = true;
            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();

    }

    public void filtrarPorCategoria(View view){

        if(filtrandoPorEstado == true){
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejada");

            //Configurar o Spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

            //Configura spinner de categorias
            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] estados = getResources().getStringArray(R.array.categorias);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    estados
            );
            adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);

            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnuciosPorCategoria();
                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();
        }else{
            Toast.makeText(this,"Escolha primeiro uma região!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void recuperarAnuciosPorEstado(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúcios")
                .setCancelable(false)
                .build();
        dialog.show();

        //Configurar nó por estado
        anuciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anucios")
                .child(filtroEstado);

        anuciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaAnucios.clear();
                for (DataSnapshot categorias: dataSnapshot.getChildren()){
                    for (DataSnapshot anucios: categorias.getChildren()){
                        Anucio anucio = anucios.getValue(Anucio.class);
                        listaAnucios.add(anucio);
                    }
                }
                Collections.reverse(listaAnucios);
                adapterAnucios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarAnuciosPorCategoria(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúcios")
                .setCancelable(false)
                .build();
        dialog.show();

        //Configurar nó por estado
        anuciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anucios")
                .child(filtroEstado)
                .child(filtroCategoria);

        anuciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaAnucios.clear();
                for (DataSnapshot anucios: dataSnapshot.getChildren()){
                    Anucio anucio = anucios.getValue(Anucio.class);
                    listaAnucios.add(anucio);
                }
                Collections.reverse(listaAnucios);
                adapterAnucios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarAnuciosPublicos(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúcios")
                .setCancelable(false)
                .build();
        dialog.show();

        listaAnucios.clear();
        anuciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot estados: dataSnapshot.getChildren()){
                    for (DataSnapshot categorias: estados.getChildren()){
                        for (DataSnapshot anucios: categorias.getChildren()){
                            Anucio anucio = anucios.getValue(Anucio.class);
                            listaAnucios.add(anucio);
                        }
                    }
                }
                Collections.reverse(listaAnucios);
                adapterAnucios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Pode alterar os itens de menu que ja foram carregados
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if( autenticacao.getCurrentUser() == null){//usuario deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);
        }else{//Usuario logado
            menu.setGroupVisible(R.id.group_logado, true);
            menu.setGroupVisible(R.id.group_perdidos, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_cadastrar :
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case R.id.menu_sair :
                autenticacao.signOut();
                invalidateOptionsMenu();
                Toast.makeText(AnuciosActivity.this, "Deslogado com sucesso.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_anucios :
                startActivity(new Intent(getApplicationContext(),MeusAnuciosActivity.class));
                break;
            case R.id.menu_perdidos :
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void inicializarComponentes(){
        recyclerAnuciosPublicos = findViewById(R.id.recyclerAnuciosPublicos);
    }
}
