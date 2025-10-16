package uhu.mb.practica1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public class IndexMedAll {

	private static final String SOLR_BASE = "http://localhost:8983/solr";
	private static final String CORE = "med";
	private static final String MED_PATH = "./collection/MED.ALL";

	public static void main(String[] args) throws Exception {
		try (SolrClient client = new HttpSolrClient.Builder(SOLR_BASE).build()) {
			indexMedAll(client, MED_PATH, CORE);
			client.commit(CORE);
			System.out.println("Indexación completada.");
		}
	}

	private static void indexMedAll(SolrClient client, String path, String core)
			throws IOException, SolrServerException {

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {

			String line;
			String currentId = null;
			boolean inW = false;
			StringBuilder wBuffer = new StringBuilder();

			List<SolrInputDocument> batch = new ArrayList<>();
			final int BATCH_SIZE = 500;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.startsWith(".I ")) {
					if (currentId != null) {
						batch.add(buildDoc(currentId, wBuffer.toString()));
						wBuffer.setLength(0);
						inW = false;
						if (batch.size() >= BATCH_SIZE) {
							sendBatch(client, core, batch);
							batch.clear();
						}
					}
					currentId = line.substring(3).trim();
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
					if (wBuffer.length() > 0)
						wBuffer.append(' ');
					wBuffer.append(line);
				}
			}

			if (currentId != null) {
				batch.add(buildDoc(currentId, wBuffer.toString()));
			}

			if (!batch.isEmpty()) {
				sendBatch(client, core, batch);
			}
		}
	}

	private static SolrInputDocument buildDoc(String id, String wText) {
		String title = "";
		String abs = "";

		if (wText != null && !wText.isEmpty()) {
			int firstDot = wText.indexOf('.');
			if (firstDot >= 0) {
				title = wText.substring(0, firstDot + 1).trim();
				abs = wText.substring(firstDot + 1).trim();
			} else {
				title = wText.trim();
			}
		}

		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", id);
		doc.addField("title", title);
		doc.addField("abstract", abs);
		return doc;
	}

	private static void sendBatch(SolrClient client, String core, List<SolrInputDocument> batch)
			throws SolrServerException, IOException {
		UpdateResponse rsp = client.add(core, batch);
		System.out.println("Añadidos " + batch.size() + " docs. Status=" + rsp.getStatus());
	}
}
