package br.com.fatec.agenda_flow;

public class Principal {

    public static void main(String[] args) {
        System.out.println("--- Teste do Modelo Orientado a Objetos ---");

        Cliente c1 = new Cliente(1, "Carlos Alberto", "carlos@email.com", "12345678");
        Administrador a1 = new Administrador(2, "Mariana Admin", "admin@email.com", "admin123");

        Servico s1 = new Servico(1, "Corte de Cabelo", "Corte masculino tradicional", 30, 45.00f, true);
        Servico s2 = new Servico(2, "Barba", "Modelagem de barba", 20, 35.00f, true);

        Profissional p1 = new Profissional(10, "Fernando Barbeiro");
        p1.adicionarServico(s1);
        p1.adicionarServico(s2);
        p1.adicionarHorario(new HorarioTrabalho(2, "08:00", "18:00"));

        System.out.println("Profissional: " + p1.getNome());
        System.out.println("Atende Corte de Cabelo? " + p1.atendeServico(s1));

        Agendamento ag = new Agendamento(100, c1, p1, s1, "2026-10-25", "10:00", "10:30");

        System.out.println("Agendamento criado ID: " + ag.getId());
        System.out.println("Cliente: " + ag.getCliente().getNome());
        System.out.println("Servico: " + ag.getServico().getNome() + " | Valor: R$ " + ag.getServico().getValor());
        System.out.println("Status Inicial: " + ag.getStatus());

        ag.concluir();
        System.out.println("Status Apos Conclusao: " + ag.getStatus());
    }
}
