package com.petinho.lucas.petinho.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.petinho.lucas.petinho.R;

public class CadastrarAnimalPerdidoActivity extends AppCompatActivity {

    private EditText nomeAnimal, descricao, telefoneContato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_animal_perdido);
    }
}
