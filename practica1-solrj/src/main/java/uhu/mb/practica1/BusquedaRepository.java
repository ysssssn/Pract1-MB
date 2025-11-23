package uhu.mb.practica1;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import org.apache.solr.client.solrj.SolrServerException;
import java.io.IOException;

import java.util.List;

@Service
public class BusquedaRepository { 

    // Ya no necesitamos la constante CORE_NAME (o la eliminamos)
    private final SolrClient solrClient;

    @Autowired
    public BusquedaRepository(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    // El método ahora lanza las excepciones nativas de SolrJ
    public List<DocumentoBusqueda> buscarOptimizada(String queryText) throws SolrServerException, IOException {
        
        SolrQuery query = new SolrQuery();
        
        // --- Estrategia de Búsqueda Optimizada con Boosting ---
        query.setParam("q", queryText); 
        query.setParam("defType", "edismax");
        query.setParam("qf", "title^2.0 abstract^1.0");
        query.setRows(1100);
        
        // CRÍTICO: Usamos query(query) sin argumento de core, ya que el cliente está enlazado a /solr/med
        QueryResponse response = solrClient.query(query); 
        
        return response.getBeans(DocumentoBusqueda.class);
    }
}