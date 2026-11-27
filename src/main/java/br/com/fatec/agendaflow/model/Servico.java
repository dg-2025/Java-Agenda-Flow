package br.com.fatec.agendaflow.model;

/** Procedimento oferecido pelo salão (corte, escova, manicure...). */
public class Servico {

    private int id;
    private String nome;
    private String descricao;
    private int duracaoMinutos;
    private double valor;
    private boolean ativo = true;

    public Servico() {
    }

    public Servico(int id, String nome, String descricao, int duracaoMinutos, double valor, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.valor = valor;
        this.ativo = ativo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return nome;
    }
}
