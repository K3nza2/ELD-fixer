package com.eldsolution;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.MessageEvent;

import io.github.cdimascio.dotenv.Dotenv;
//Task: Add logic for tracking if the device is working or not through skyonics and if not send reset ble command
//if it still does not work add new device to sheet and hasmap and add a counter to it
public class FirstScript {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String app_level_token = dotenv.get("APP_LEVEL_TOKEN");
        String bot_level_token = dotenv.get("BOT_LEVEL_TOKEN");
        final String  SpreadsheetId = dotenv.get("SPREADSHEET_ID");
        final String Range = dotenv.get("RANGE");

        App app = new App();
        System.out.println("Pocinjem inicijalizaciju sheets-a...");

        //Set up sheets and connect
        GoogleSheetsHelper sheetsHelper = new GoogleSheetsHelper(dotenv.get("CREDENTIALS_JSON"));
        sheetsHelper.SetSpreadsheet(SpreadsheetId, Range);
        System.out.println("Sheets je inicijalizovan!");
        Sheets service = sheetsHelper.getSheetService();
        ValueRange result = service.spreadsheets().values().get(SpreadsheetId, Range).execute();
        Map<String, Integer> deviceMap = new HashMap<>();
        List<List<Object>> values = result.getValues();
        
        //creates a hashmap that reads all values in spreadsheet
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);

            if (!row.isEmpty()) {
                String deviceId = row.get(0).toString();
                deviceMap.put(deviceId, i + 1);
            }
        }

        System.out.println("Stampam sta cita sa sheet-a:" +result);
        System.out.println("Pocinjem sa cekanjem poruka na slack-u...");
        app.event(MessageEvent.class, (payload, ctx) -> {
            MessageEvent event = payload.getEvent();
            
            String text = event.getText();
            String channel = event.getChannel();
            String user = event.getUser();
            String ts = event.getTs();
            
            Slack slack = Slack.getInstance();

            String id_uredjaja = text.substring(text.length()-12);
            if(deviceMap.containsKey(id_uredjaja)) {
                System.out.println("Nasao je uredjaj:" + id_uredjaja);
                int rowindex = deviceMap.get(id_uredjaja);
                String counterRange = "'Sheet1'!D" + rowindex;
                ValueRange counterResult = service.spreadsheets().values().get(SpreadsheetId, counterRange).execute();
                int counter = 0;
                if(counterResult.getValues() != null && !counterResult.getValues().isEmpty()) {
                    counter = Integer.parseInt(counterResult.getValues().get(0).get(0).toString());
                }
                counter ++;
                ValueRange body = new ValueRange().setValues(List.of(List.of(counter)));
                System.out.println("Povecavam counter! proveri da li se povecao");
                service.spreadsheets().values().update(SpreadsheetId, counterRange, body).setValueInputOption("RAW").execute();
                try{
                    slack.methods(bot_level_token).reactionsAdd(r -> r
                    .channel(event.getChannel())
                    .timestamp(event.getTs())
                    .name("sheet1") 
                );
                } catch (Exception e) {
                System.out.println("Reaction error: " + e.getMessage());
                }
                
            }
            else {
                System.out.println("Nije nasao uredjaj");
                try {
                slack.methods(bot_level_token).reactionsAdd(r -> r
                    .channel(event.getChannel())
                    .timestamp(event.getTs())
                    .name("eyes")
                );
            } catch (Exception e) {
                System.out.println("Reaction error: " + e.getMessage());
                }
            }
            System.out.println("Poruka: " + text);
            System.out.println("Channel: " + channel);
            System.out.println("User: " + user);
            System.out.println("timestamp:" + ts);

            return ctx.ack();
        });
        SocketModeApp socketModeApp = new SocketModeApp(app_level_token, app);
        socketModeApp.start();
        
    }
}
