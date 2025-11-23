package uhu.mb.practica1;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;

public class DocumentoBusqueda {

    @Id
    @Field
    private String id;

    @Field("title")
    private String titulo;

    @Field("abstract")
    private String resumen;
    
    // AÑADE ESTA PROPIEDAD: Score de relevancia (float o double)
    @Field("score") 
    private float score;

    // --- Getters y Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    
    // AÑADE EL GETTER Y SETTER PARA EL SCORE
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    // Constructor vacío (necesario para SolrJ)
    public DocumentoBusqueda() {}
}