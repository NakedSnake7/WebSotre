package com.WeedTitlan.server.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "WeedTitlan Sync";

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("#{systemEnvironment['GOOGLE_SHEET_CREDENTIALS_JSON']}")
    private String credentialsJson;

    @Value("${google.sheets.range:Hoja1!A:B}")
    private String rango;

    public Map<String, Integer> obtenerStockDesdeSheet() throws Exception {

        if (credentialsJson == null || credentialsJson.isEmpty()) {
            throw new RuntimeException("⚠ Variable de entorno GOOGLE_SHEET_CREDENTIALS_JSON no definida");
        }

        // Convertimos la variable de entorno en InputStream
        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials credential = ServiceAccountCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        Sheets sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credential)
        ).setApplicationName(APPLICATION_NAME).build();

        ValueRange response = sheetsService.spreadsheets()
                .values()
                .get(spreadsheetId, rango)
                .execute();

        Map<String, Integer> inventario = new HashMap<>();

        if (response.getValues() != null) {
            for (List<Object> fila : response.getValues()) {
                if (fila.size() >= 2) {
                    String nombre = fila.get(0).toString();
                    Integer stock;
                    try {
                        stock = Integer.valueOf(fila.get(1).toString());
                    } catch (NumberFormatException e) {
                        stock = 0;
                    }
                    inventario.put(nombre, stock);
                }
            }
        }

        return inventario;
    }
}
