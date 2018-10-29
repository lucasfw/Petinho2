package com.petinho.lucas.petinho.model;

import com.google.firebase.database.DatabaseReference;
import com.petinho.lucas.petinho.helper.ConfiguracaoFirebase;

import java.util.List;

public class Anucio {
    private String idAnucio;
    private String estado;
    private String categoria;
    private String titulo;
    private String valor;
    private String telefone;
    private String descricao;
    private List<String> fotos;

    public Anucio() {
        DatabaseReference anucioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anucios");
        setIdAnucio(anucioRef.push().getKey());
    }

    public void salvar(){
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anucioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anucios");
        anucioRef.child(idUsuario)
                .child(getIdAnucio())
                .setValue(this);
    }

    public String getIdAnucio() {
        return idAnucio;
    }

    public void setIdAnucio(String idAnucio) {
        this.idAnucio = idAnucio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }


}
