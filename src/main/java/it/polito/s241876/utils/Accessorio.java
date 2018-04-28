package it.polito.s241876.utils;

public class Accessorio {
    private String id;
    private String nome;
    private String categoria;
    private String istruzioni_uso;

    public Accessorio(){}

    public Accessorio(String id, String nome, String categoria, String istruzioni_uso){
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.istruzioni_uso = istruzioni_uso;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getIstruzioni_uso() {
        return istruzioni_uso;
    }

    public void setIstruzioni_uso(String istruzioni_uso) {
        this.istruzioni_uso = istruzioni_uso;
    }
}
