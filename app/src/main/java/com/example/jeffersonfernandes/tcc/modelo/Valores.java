package com.example.jeffersonfernandes.tcc.modelo;

import java.io.Serializable;

public class Valores implements Serializable {

    private int id;
    private float corrente;
    private float potencia;
    private float tensao;
    private float temperatura;
    private String data;
    private String hora;

    public Valores (){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCorrente() {
        return corrente;
    }

    public void setCorrente(float corrente) {
        this.corrente = corrente;
    }

    public float getPotencia() {
        return potencia;
    }

    public void setPotencia(float potencia) {
        this.potencia = potencia;
    }

    public float getTensao() {
        return tensao;
    }

    public void setTensao(float tensao) {
        this.tensao = tensao;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(float temperatura) {
        this.temperatura = temperatura;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
