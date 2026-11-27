package br.com.fatec.agendaflow.exception;

/** RNF-05 / RN-09: usuário sem permissão tentou uma ação restrita (equivale ao HTTP 403). */
public class AcessoNegadoException extends RegraNegocioException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
