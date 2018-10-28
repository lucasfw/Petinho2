package com.petinho.lucas.petinho.activity;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.petinho.lucas.petinho.R;

import java.util.Locale;

public class CadastrarAnucioActivity extends AppCompatActivity {

    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anucio);

        inicializarComponentes();
    }

    public void salvarAnucio(View view){
        String valor = campoValor.getHintString();
        Log.d("salvar", "salvarAnucio: " + valor);

    }

    private void inicializarComponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);

        //Configura Localidade
        Locale locale = new Locale("pt","BR");
        campoValor.setLocale(locale);
    }
}
