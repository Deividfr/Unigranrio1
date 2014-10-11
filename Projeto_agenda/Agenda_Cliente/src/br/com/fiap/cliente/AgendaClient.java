package br.com.fiap.cliente;

/**
 * Classe Consumidora do REST Agenda
 */
public class AgendaClient {

	/**
	 * Metodo principal da aplicacao.
	 * @param args Argumentos de linha de comando
	 */
	public static void main(String[] args) {
		AgendaUI agendaUI = new AgendaUI();
		agendaUI.execute();
	}
}
