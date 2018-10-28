package com.petinho.lucas.petinho.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.helper.ConfiguracaoFirebase;

public class AnuciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anucios);

        //Configuracoes
        autenticacao = ConfiguracaoFirebase.getReferenciaAutenticacao();
        //autenticacao.signOut();
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
        }

        return super.onOptionsItemSelected(item);
    }
}
