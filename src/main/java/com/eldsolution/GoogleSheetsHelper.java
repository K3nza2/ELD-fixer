package com.eldsolution;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class GoogleSheetsHelper {
    private Sheets sheetsService;
    private String spreadsheetId;
    private String range;

    public GoogleSheetsHelper(String credentialsJson) throws Exception {
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
            .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        this.sheetsService = new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials)
        )
        .setApplicationName("Slackbot")
        .build();
    }
    public void SetSpreadsheet (String SpreadSheetId, String range){
        this.spreadsheetId = SpreadSheetId;
        this.range = range;
    }
    public Sheets getSheetService() {
        return sheetsService;
    }
    public String getSpreadSheetId (){
        return spreadsheetId;
    }
    public String getRange() {
        return range;
    }
}
