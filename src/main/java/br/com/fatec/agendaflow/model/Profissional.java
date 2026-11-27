package br.com.fatec.agendaflow.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pessoa que realiza os serviços. Guarda (agregação) os serviços que sabe
 * fazer e (composição) os seus horários de trabalho.
 */
public class Profissional {

    private int id;
    private String nome;
    private boolean ativo = true;
    private final Set<Integer> servicoIds = new LinkedHashSet<>();

    public Profissional() {
    }

    public Profissional(int id, String nome, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.ativo = ativo;
    }

    /** RF-08 / RN-10: o profissional só aparece para serviços vinculados a ele. */
    public boolean atendeServico(int servicoId) {
        return servicoIds.contains(servicoId);
    }

    public void adicionarServico(int servicoId) { servicoIds.add(servicoId); }
    public void removerServico(int servicoId) { servicoIds.remove(servicoId); }
    public Set<Integer> getServicoIds() { return servicoIds; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return nome;
    }
}
