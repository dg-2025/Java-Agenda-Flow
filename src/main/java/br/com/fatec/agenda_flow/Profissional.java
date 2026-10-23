package br.com.fatec.agenda_flow;

import java.util.HashSet;
import java.util.Objects;

public class Profissional {
    private int id;
    private String nome;
    private boolean ativo;
    private HashSet<Servico> servicos;
    private HashSet<HorarioTrabalho> horarios;

    public Profissional() {
        this.servicos = new HashSet<>();
        this.horarios = new HashSet<>();
        this.ativo = true;
    }

    public Profissional(int id, String nome) {
        this();
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public HashSet<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(HashSet<Servico> servicos) {
        this.servicos = servicos;
    }

    public HashSet<HorarioTrabalho> getHorarios() {
        return horarios;
    }

    public void setHorarios(HashSet<HorarioTrabalho> horarios) {
        this.horarios = horarios;
    }

    public void adicionarServico(Servico s) {
        if (this.servicos == null) {
            this.servicos = new HashSet<>();
        }
        this.servicos.add(s);
    }

    public void removerServico(Servico s) {
        if (this.servicos != null) {
            this.servicos.remove(s);
        }
    }

    public void adicionarHorario(HorarioTrabalho h) {
        if (this.horarios == null) {
            this.horarios = new HashSet<>();
        }
        this.horarios.add(h);
    }

    public boolean atendeServico(Servico s) {
        if (this.servicos == null) {
            return false;
        }
        return this.servicos.contains(s);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Profissional other = (Profissional) obj;
        return this.id == other.id;
    }
}
