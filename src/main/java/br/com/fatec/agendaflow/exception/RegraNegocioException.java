package br.com.fatec.agendaflow.exception;

/** Violação de uma regra de negócio. A mensagem é mostrada direto na tela. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
