package com.WeedTitlan.server.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ProveedorStockService {

    // Variable de entorno con JSON de credenciales
    @Value("${google.sheets.spreadsheet-id:}") 
    private String spreadsheetId;

    // Lista de rangos de todas las tablas
    private static final String[] RANGOS_TABLAS = {
        "'Hoja 1'!A4:B15", "'Hoja 1'!D4:E15", "'Hoja 1'!G4:H15", "'Hoja 1'!J4:K15", "'Hoja 1'!M4:N15",
        "'Hoja 1'!A18:B29", "'Hoja 1'!D18:E29", "'Hoja 1'!G18:H29", "'Hoja 1'!J18:K29", "'Hoja 1'!M18:N29",
        "'Hoja 1'!A36:B47", "'Hoja 1'!D36:E47", "'Hoja 1'!G36:H47", "'Hoja 1'!J36:K47", "'Hoja 1'!M36:N47",
        "'Hoja 1'!A50:B61", "'Hoja 1'!D50:E61", "'Hoja 1'!G50:H61", "'Hoja 1'!J50:K61", "'Hoja 1'!M50:N61",
        "'Hoja 1'!A68:B79", "'Hoja 1'!D68:E79", "'Hoja 1'!M74:N75"
    };

    public static class ProductoStock {
        private String nombre;
        private int stock;

        public ProductoStock(String nombre, int stock) {
            this.nombre = nombre;
            this.stock = stock;
        }

        public String getNombre() { return nombre; }
        public int getStock() { return stock; }
    }

    public Map<String, List<ProductoStock>> obtenerStockPorMarca() {
        Map<String, List<ProductoStock>> inventarioPorMarca = new LinkedHashMap<>();

        try {
            if (spreadsheetId.isBlank()) {
                System.out.println("⚠ Google Sheets deshabilitado: spreadsheet ID no configurado.");
                return inventarioPorMarca;
            }

            // Leer variable de entorno
            String credentialsJson = System.getenv("GOOGLE_SHEET_CREDENTIALS_JSON");

            InputStream credentialsStream;
            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                // Producción: usar variable de entorno
                credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
            } else {
                // Local: fallback a archivo físico
                credentialsStream = getClass().getResourceAsStream("/credenciales.json");
                if (credentialsStream == null) {
                    throw new RuntimeException("No se pudo encontrar credenciales.json en classpath ni variable de entorno GOOGLE_SHEET_CREDENTIALS_JSON definida");
                }
            }

            GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

            Sheets sheetService = new Sheets.Builder(
                    credential.getTransport(),
                    credential.getJsonFactory(),
                    credential
            ).setApplicationName("WeedTlan-StockSync").build();

            for (String rango : RANGOS_TABLAS) {
                ValueRange response = sheetService.spreadsheets().values()
                        .get(spreadsheetId, rango)
                        .execute();

                List<List<Object>> filas = response.getValues();
                if (filas == null || filas.isEmpty()) continue;

                String marca = filas.get(0).get(1).toString().trim();
                if (marca.isEmpty()) continue;

                inventarioPorMarca.putIfAbsent(marca, new ArrayList<>());
                List<ProductoStock> listaProductos = inventarioPorMarca.get(marca);

                for (int i = 1; i < filas.size(); i++) {
                    List<Object> fila = filas.get(i);
                    if (fila.size() < 2) continue;

                    String stockStr = fila.get(0).toString().trim();
                    String nombreProducto = fila.get(1).toString().trim();

                    if (stockStr.equalsIgnoreCase("TOTAL") || nombreProducto.isEmpty()) continue;

                    int stock = 0;
                    try { stock = Integer.parseInt(stockStr); } catch (Exception ignored) {}

                    listaProductos.add(new ProductoStock(nombreProducto, stock));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error al conectar con Google Sheets:");
            e.printStackTrace();
        }

        return inventarioPorMarca;
    }
}
