package br.com.fatec.agendaflow.exception;

/** RF-11: o horário escolhido não está (ou deixou de estar) livre. */
public class HorarioIndisponivelException extends RegraNegocioException {

    public HorarioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
