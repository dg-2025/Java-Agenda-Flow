package br.com.fatec.agendaflow.model;

import br.com.fatec.agendaflow.exception.RegraNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Registro que liga cliente, profissional, serviço, data e horário. */
public class Agendamento {

    private int id;
    private int clienteId;
    private int profissionalId;
    private int servicoId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAgendamento status = StatusAgendamento.AGENDADO;
    private double valor;
    private LocalDateTime dataCriacao;

    // Nomes vindos das tabelas relacionadas, só para exibir nas telas
    private String clienteNome;
    private String profissionalNome;
    private String servicoNome;

    public Agendamento() {
    }

    public Agendamento(int clienteId, int profissionalId, int servicoId, LocalDate data,
                       LocalTime horaInicio, int duracaoMinutos, double valor) {
        this.clienteId = clienteId;
        this.profissionalId = profissionalId;
        this.servicoId = servicoId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaInicio.plusMinutes(duracaoMinutos);
        this.valor = valor;
        this.status = StatusAgendamento.AGENDADO;
        this.dataCriacao = LocalDateTime.now();
    }

    /** RF-11 / RN-06: o intervalo [inicio, fim) bate com este agendamento? */
    public boolean conflitaCom(LocalTime inicio, LocalTime fim) {
        return status.ocupaHorario()
                && inicio.isBefore(horaFim)
                && horaInicio.isBefore(fim);
    }

    /** RN-08: só um agendamento "Agendado" pode mudar de status. */
    public void alterarStatus(StatusAgendamento novo) {
        if (status.isFinalizado()) {
            throw new RegraNegocioException("Este agendamento já foi finalizado como \""
                    + status.getTexto() + "\" e não pode ser alterado.");
        }
        if (novo == StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("O agendamento já está como \"Agendado\".");
        }
        this.status = novo;
    }

    public void cancelar() { alterarStatus(StatusAgendamento.CANCELADO); }
    public void concluir() { alterarStatus(StatusAgendamento.CONCLUIDO); }
    public void registrarFalta() { alterarStatus(StatusAgendamento.FALTA); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getProfissionalId() { return profissionalId; }
    public void setProfissionalId(int profissionalId) { this.profissionalId = profissionalId; }

    public int getServicoId() { return servicoId; }
    public void setServicoId(int servicoId) { this.servicoId = servicoId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getProfissionalNome() { return profissionalNome; }
    public void setProfissionalNome(String profissionalNome) { this.profissionalNome = profissionalNome; }

    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
}
