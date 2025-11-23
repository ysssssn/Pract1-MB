package uhu.mb.practica1;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Value("${spring.data.solr.host}") // Lee la URL de application.properties
    private String solrHost;

    @Bean
    public SolrClient solrClient() {
        // Construye el cliente nativo de SolrJ
        return new HttpSolrClient.Builder(solrHost).build();
    }
}