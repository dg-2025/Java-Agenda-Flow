package br.com.fatec.agenda_flow;

import br.com.fatec.agenda_flow.factory.UsuarioFactory;
import br.com.fatec.agenda_flow.observer.NotificadorStatusObserver;
import br.com.fatec.agenda_flow.strategy.DescontoFidelidadeStrategy;

public class Principal {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA AGENDA FLOW COM PADROES GOF ===");

        Cliente c1 = (Cliente) UsuarioFactory.criarUsuario("CLIENTE", 1, "Carlos Alberto", "carlos@email.com", "12345678");
        Administrador a1 = (Administrador) UsuarioFactory.criarUsuario("ADMIN", 2, "Mariana Admin", "admin@email.com", "admin123");

        Servico s1 = new Servico(1, "Corte de Cabelo", "Corte masculino tradicional", 30, 50.00f, true);

        Profissional p1 = new Profissional(10, "Fernando Barbeiro");
        p1.adicionarServico(s1);
        p1.adicionarHorario(new HorarioTrabalho(2, "08:00", "18:00"));

        Agendamento ag = new Agendamento(100, c1, p1, s1, "2026-10-25", "10:00", "10:30");

        ag.adicionarObserver(new NotificadorStatusObserver());

        System.out.println("Cliente: " + ag.getCliente().getNome());
        System.out.println("Valor Original: R$ " + ag.getServico().getValor());
        System.out.println("Valor c/ Preco Integral: R$ " + ag.getValorFinal());

        ag.setEstrategiaPreco(new DescontoFidelidadeStrategy(10f));
        System.out.println("Valor c/ Desconto Fidelidade: R$ " + ag.getValorFinal());

        System.out.println("Alterando status do agendamento...");
        ag.concluir();
    }
}
