package com.concessionaria.api.entidade;

public class Usuario {

    private int id;
    private String username;
    private String papel; // "GERENTE" ou "VENDEDOR"
    private Integer funcionarioId;

    public Usuario(int id, String username, String papel, Integer funcionarioId) {
        this.id = id;
        this.username = username;
        this.papel = papel;
        this.funcionarioId = funcionarioId;
    }

    public int getId()                { return id; }
    public String getUsername()       { return username; }
    public String getPapel()          { return papel; }
    public Integer getFuncionarioId() { return funcionarioId; }
}
