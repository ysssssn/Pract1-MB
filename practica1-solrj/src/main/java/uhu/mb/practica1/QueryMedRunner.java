package uhu.mb.practica1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class QueryMedRunner {

	private static final String SOLR_BASE = "http://localhost:8983/solr";
	private static final String CORE = "med";

	private static final String MED_QRY_PATH = "./collection/MED.QRY"; // ruta
	private static final String OUTPUT_PATH = "./results/solr_results_trec.txt"; // fichero de salida
	private static final String RUN_TAG = "MIO"; // etiqueta para trec_eval
	private static final int TOP_K = 10; // numero de resultados por consulta

	public static void main(String[] args) throws Exception {
		new File("./results").mkdirs();

		try (SolrClient client = new HttpSolrClient.Builder(SOLR_BASE).build()) {

			List<QueryItem> queries = parseMedQry(MED_QRY_PATH);

			try (BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.UTF_8))) {

				for (QueryItem q : queries) {

					String fullQueryText = ClientUtils.escapeQueryChars(q.text);

					String solrQ = String.format("title:(%s) OR abstract:(%s)", fullQueryText, fullQueryText);

					SolrQuery query = new SolrQuery();
					query.setQuery(solrQ);
					query.setRows(TOP_K);
					query.setFields("id", "score");

					QueryResponse rsp = client.query(CORE, query);
					SolrDocumentList docs = rsp.getResults();

					int rank = 1;
					for (SolrDocument d : docs) {
						String id = String.valueOf(d.getFieldValue("id"));
						Object s = d.getFieldValue("score");
						String score = (s == null) ? "0.0" : s.toString();

						out.write(String.format("%s Q0 %s %d %s %s\n", q.id, id, (rank - 1), score, RUN_TAG));
						rank++;
					}
				}
			}

			System.out.println("Consultas lanzadas y resultados guardados en: " + OUTPUT_PATH);
		}
	}

	private static List<QueryItem> parseMedQry(String path) throws IOException {
		List<QueryItem> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {

			String line;
			String currentId = null;
			boolean inW = false;
			StringBuilder wBuf = new StringBuilder();

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.startsWith(".I ")) {

					if (currentId != null) {
						list.add(new QueryItem(currentId, wBuf.toString().trim()));
						wBuf.setLength(0);
					}
					currentId = line.substring(3).trim();
					inW = false;
					continue;
				}

				if (line.equals(".W")) {
					inW = true;
					continue;
				}

				if (line.startsWith(".") && line.length() >= 2 && Character.isLetter(line.charAt(1))) {
					inW = false;
					continue;
				}

				if (inW) {
					if (wBuf.length() > 0)
						wBuf.append(' ');
					wBuf.append(line);
				}
			}

			if (currentId != null) {
				list.add(new QueryItem(currentId, wBuf.toString().trim()));
			}
		}
		return list;
	}

	private static class QueryItem {
		String id;
		String text;

		QueryItem(String id, String text) {
			this.id = id;
			this.text = text;
		}
	}
}