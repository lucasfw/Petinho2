package com.petinho.lucas.petinho.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.helper.ConfiguracaoFirebase;
import com.petinho.lucas.petinho.helper.Permissoes;
import com.petinho.lucas.petinho.model.Anucio;
import com.santalu.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnucioActivity extends AppCompatActivity
                                        implements View.OnClickListener{

    private EditText campoTitulo, campoDescricao;
    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;
    private EditText campoValor;
    private MaskEditText campoTelefone;
    private Anucio anucio;
    private StorageReference storage;
    private AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaURLFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anucio);

        //Configuraçoes iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        //Validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();

        carregarDadosSpinner();
    }

    public void salvarAnucio(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúcio")
                .setCancelable(false)
                .build();
        dialog.show();

        //Salvar imagem no storage
        for (int i=0; i< listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }

    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){
        //Criar só no storage
        final StorageReference imagemAnucio = storage.child("imagens")
                .child("anucios")
                .child(anucio.getIdAnucio())
                .child("imagem" + contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnucio.putFile( Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();



                //Task<Uri> firebaseUrl = taskSnapshot.getStorage().getDownloadUrl();
               //String urlConvertida = firebaseUrl.toString();
                listaURLFotos.add(downloadUrl.toString());

                if(totalFotos== listaURLFotos.size()){
                    anucio.setFotos(listaURLFotos);
                    anucio.salvar();
                    dialog.dismiss();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload");
            }
        });
    }

    private Anucio configurarAnucio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();

        Anucio anucio = new Anucio();
        anucio.setEstado(estado);
        anucio.setCategoria(categoria);
        anucio.setTitulo(titulo);
        anucio.setValor(valor);
        anucio.setTelefone(telefone);
        anucio.setDescricao(descricao);

        return anucio;
    }

    public void validarDadosAnucio(View view){
        anucio = configurarAnucio();
        //String valor = String.valueOf(campoValor.getRawValue());


        if(listaFotosRecuperadas.size() != 0){
            if(!anucio.getEstado().isEmpty()){
                if(!anucio.getCategoria().isEmpty()){
                    if(!anucio.getTitulo().isEmpty()){
                        if(!anucio.getValor().isEmpty()){
                            if(!anucio.getTelefone().isEmpty()){
                                if(!anucio.getDescricao().isEmpty()){
                                    salvarAnucio();
                                }else{
                                    exibirMensagemErro("Preencha o campo descricao!");
                                }
                            }else{
                                exibirMensagemErro("Preencha o campo telefone com ao menos 10 dígitos!");
                            }
                        }else{
                            exibirMensagemErro("Preencha o campo valor!");
                        }
                    }else{
                        exibirMensagemErro("Preencha o campo título!");
                    }
                }else{
                    exibirMensagemErro("Preencha o campo categoria!");
                }
            }else{
                exibirMensagemErro("Preencha o campo estado!");
            }
        }else{
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    public void exibirMensagemErro(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCadastro1 :
                escolherImagem(1);
                break;
            case R.id.imageCadastro2 :
                escolherImagem(2);
                break;
            case R.id.imageCadastro3 :
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            //Recuperar Imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if( requestCode == 1){
                imagem1.setImageURI(imagemSelecionada);

            }else if(requestCode == 2){
                imagem2.setImageURI(imagemSelecionada);
            }else if(requestCode == 3){
                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);
        }
    }

    private void carregarDadosSpinner(){

        /*String[] estados = new String[]{
            "SP", "MT"
        };*/

        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapter);

        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);

    }

    private void inicializarComponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
