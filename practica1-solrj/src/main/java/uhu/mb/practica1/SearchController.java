package uhu.mb.practica1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    // Ahora inyectamos la CLASE BusquedaRepository, que ahora es un @Service
    private final BusquedaRepository repository;

    @Autowired
    public SearchController(BusquedaRepository repository) {
        this.repository = repository;
    }

    // Maneja la ruta raíz ("/") para mostrar el formulario
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("resultados", Collections.emptyList());
        model.addAttribute("query", "");
        return "search"; // Llama a src/main/resources/templates/search.html
    }

    // Maneja la petición de búsqueda ("search?query=texto")
    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query, Model model) {
        
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("resultados", Collections.emptyList());
            model.addAttribute("query", "");
            return "search";
        }

        try {
            // Llama al método del Service (BusquedaRepository)
            List<DocumentoBusqueda> resultados = repository.buscarOptimizada(query);
            
            model.addAttribute("resultados", resultados);
            model.addAttribute("query", query);

        } catch (Exception e) {
            // Captura errores de Solr, como problemas de conexión.
            model.addAttribute("error", "Error de conexión o consulta a Solr: " + e.getMessage());
            model.addAttribute("resultados", Collections.emptyList());
        }

        return "search"; // Vuelve a la plantilla con los datos.
    }
}