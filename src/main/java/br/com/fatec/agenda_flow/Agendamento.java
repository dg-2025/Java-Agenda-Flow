package br.com.fatec.agenda_flow;

import br.com.fatec.agenda_flow.observer.AgendamentoObserver;
import br.com.fatec.agenda_flow.strategy.CalculoPrecoStrategy;
import br.com.fatec.agenda_flow.strategy.PrecoIntegralStrategy;
import java.util.ArrayList;
import java.util.List;

public class Agendamento {
    private int id;
    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private String data;
    private String horaInicio;
    private String horaFim;
    private StatusAgendamento status;
    private CalculoPrecoStrategy estrategiaPreco;
    private List<AgendamentoObserver> observadores;

    public Agendamento() {
        this.status = StatusAgendamento.AGENDADO;
        this.estrategiaPreco = new PrecoIntegralStrategy();
        this.observadores = new ArrayList<>();
    }

    public Agendamento(int id, Cliente cliente, Profissional profissional, Servico servico, String data, String horaInicio, String horaFim) {
        this();
        this.id = id;
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public void adicionarObserver(AgendamentoObserver o) {
        this.observadores.add(o);
    }

    public void removerObserver(AgendamentoObserver o) {
        this.observadores.remove(o);
    }

    private void notificarStatus(StatusAgendamento antigo, StatusAgendamento novo) {
        for (AgendamentoObserver o : observadores) {
            o.onStatusChanged(this, antigo, novo);
        }
    }

    public float getValorFinal() {
        if (servico == null) return 0f;
        return estrategiaPreco.calcular(servico.getValor());
    }

    public void setEstrategiaPreco(CalculoPrecoStrategy estrategia) {
        this.estrategiaPreco = estrategia;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }
    public StatusAgendamento getStatus() { return status; }

    public void setStatus(StatusAgendamento status) {
        StatusAgendamento antigo = this.status;
        this.status = status;
        notificarStatus(antigo, status);
    }

    public void cancelar() {
        setStatus(StatusAgendamento.CANCELADO);
    }

    public void concluir() {
        setStatus(StatusAgendamento.CONCLUIDO);
    }
}
