package br.com.fiap.cliente;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import br.com.fiap.agenda.Agenda;
import br.com.fiap.agenda.Contato;

/** 
 * Classe do Gerenciador de Agenda, consome o REST AgendaServer 
 */
public class AgendaRestClient {
	
	/* strings para conexao ao Servico REST */
	private static final String REST_CONTATOS_URL = "/contatos";
	
	/** Nivel de Log do Cliente REST. */
	private static final Level CLIENT_LOG_LEVEL = Level.OFF;
	
	/** URL do servidor REST */
	private String restServerUrl = "http://localhost:8888/agenda";
	
	/** ClientResource para a Agenda */
	private ClientResource agendaResource  = null;
	/** ClientResource para um Contato */
	private ClientResource contatoResource = null;

	/** Construtor default. */
	public AgendaRestClient() {
		setServerURL(this.restServerUrl);
	}

	/** 
	 * Configura a URL do Servidor da Agenda.
	 * @param url URL do servidor REST
	 */
	public void setServerURL(String url) {
		this.restServerUrl = url;
		agendaResource =
			getServletResource(restServerUrl + REST_CONTATOS_URL);
	}
	
	/**
	 * Retorna a URL do Servidor da Agenda.
	 * @return URL do servidor REST.
	 */
	public String getServerURL() {
		return this.restServerUrl;
	}
	
	/**
	 * Adiciona um Contato na Agenda. 
	 * @param contato Contato a ser inserido
	 * @return True se bem sucedido, false se houve erro.
	 */
	public boolean inserir(Contato contato) {
		return postContato(contato);
	}

	/**
	 * Remove um Contato da Agenda.
	 * @param email Email do Contato a ser excluido
	 * @return True se bem sucedido, false se houve erro.
	 */
	public boolean excluir(String email) {
		return deleteContato(email);
	}

	/**
	 * Retorna um Contato da Agenda.
	 * @param email Email do Contato a ser consultado
	 * @return Contato da agenda, ou null se houve erro.
	 */
	public Contato consultar(String email) {
		return getContato(email);
	}

	/**
	 * Lista todos os Contatos da Agenda.
	 * @return Lista de Contatos, ou lista vazia se Agenda vazia ou
	 *  se houve erro.
	 */
	public Contato[] listar() {
		return getListaContatos();
	}

	/**
	 * Cria um objeto ClientResource a partir de uma URL.
	 * @param url URL do resource
	 * @return Objeto ClientResource.
	 */
	private ClientResource getServletResource(String url) {
	
		ClientResource resource =
			new ClientResource(url);
		
		resource.getLogger().setLevel(CLIENT_LOG_LEVEL);
		return resource;
	}
	
	/**
	 * Converte um Contato para XML e envia ao servidor REST, via POST.
	 * @param contato Contato a ser postado.
	 * @return True se bem sucedido, false se houve erro.
	 */
	private boolean postContato(Contato contato) {
		try {
			agendaResource.post(getRepresentation(contato));
			return (agendaResource.getStatus().isSuccess());
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * Remove um Contato do servidor REST, via DELETE.
	 * @param email Email do Contato a ser excluido
	 * @return True se bem sucedido, false se houve erro.
	 */
	private boolean deleteContato(String email) {

		long idContato = 0;
		
		try {
			idContato = getContatoId(email);
			
			if (idContato > 0) {
				contatoResource =
					getServletResource(restServerUrl + REST_CONTATOS_URL
							+ "/" + String.valueOf(idContato));
			
				contatoResource.delete();
				
				if (contatoResource.getStatus().isSuccess()) {
					return true;
				}
			}
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		return false;
	}
	
	/**
	 * Retorna um Contato do servidor REST, via GET.
	 * @param email Email do Contato a ser consultado
	 * @return Contato da agenda, ou null se houve erro.
	 */
	private Contato getContato(String email) {

		Contato contato = null;
		long idContato = 0;
		
		try {
			idContato = getContatoId(email);
			
			if (idContato > 0) {
				contatoResource =
					getServletResource(restServerUrl + REST_CONTATOS_URL
							+ "/" + String.valueOf(idContato));
			
				contato = getContatoByRep(contatoResource.get());

				if (!contatoResource.getStatus().isSuccess()) {
					contato = null;
				}
			}
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		return contato;
	}

	/**
	 * Lista todos os Contatos do servidor REST, via GET.
	 * @return Lista de Contatos, ou lista vazia se Agenda vazia ou
	 *  se houve erro.
	 */
	private Contato[] getListaContatos() {
		Contato listaContatos[] = new Contato[0];
		try {
			Agenda agenda = getAgendaByRep(agendaResource.get());

			if (agendaResource.getStatus().isSuccess()
					&& agenda != null
					&& agenda.getContatos().size() > 0) {
				
				listaContatos =
					new Contato[agenda.getContatos().size()];
				
				int i = 0;
				for (Contato contato : agenda.getContatos()) {
					listaContatos[i++] = contato;
				}
			}
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		return listaContatos;
	}
	
	/**
     * Retorna a representacao de um Contato.
     * @param contato Objeto da classe Contato
     * @return Representacao em XML do objeto.
     */
    private Representation getRepresentation(Contato contato) {
    	
    	JaxbRepresentation<Contato> representation =
    		new JaxbRepresentation<Contato>(contato);

    	representation.setFormattedOutput(true);
        return representation;
    }

    /**
     * Retorna um objeto Contato a partir de uma representacao XML.
     * @param repContato Representacao XML do Contato.
     * @return Objeto Contato, ou null se houve erro.
     * @throws IOException
     */
    private Contato getContatoByRep(Representation repContato)
    		throws IOException {

		JaxbRepresentation<Contato> jaxbRep =
			new JaxbRepresentation<Contato>(repContato,Contato.class);
	
		return jaxbRep.getObject();
    }

    /**
     * Retorna um objeto Agenda a partir de uma representacao XML.
     * @param repAgenda Representacao XML da Agenda.
     * @return Objeto Agenda, ou null se houve erro.
     */
    private Agenda getAgendaByRep(Representation repAgenda) {

    	try {

    		JaxbRepresentation<Agenda> jaxbRep =
    			new JaxbRepresentation<Agenda>(repAgenda,
    					Agenda.class);
	
    		return jaxbRep.getObject();
    		
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    /**
     * Retorna o ID de um Contato a partir de seu email.
     * @param email Email do Contato a ser consultado
     * @return Id do Contato, ou zero se houve erro.
     */
	private long getContatoId(String email) {

		Contato listaContatos[] = getListaContatos();

		for (Contato contato : listaContatos) {
			if (contato.getEmail() != null
				&& contato.getEmail().equalsIgnoreCase(email)) {
				return contato.getId();
			}
		}
		return 0;
	}
}
