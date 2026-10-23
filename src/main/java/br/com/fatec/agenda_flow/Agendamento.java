package br.com.fatec.agenda_flow;

public class Agendamento {
    private int id;
    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private String data;
    private String horaInicio;
    private String horaFim;
    private StatusAgendamento status;

    public Agendamento() {
    }

    public Agendamento(int id, Cliente cliente, Profissional profissional, Servico servico, String data, String horaInicio, String horaFim) {
        this.id = id;
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = StatusAgendamento.AGENDADO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public void cancelar() {
        this.status = StatusAgendamento.CANCELADO;
    }

    public void concluir() {
        this.status = StatusAgendamento.CONCLUIDO;
    }
}
