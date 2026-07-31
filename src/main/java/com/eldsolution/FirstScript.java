package com.eldsolution;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.cdimascio.dotenv.Dotenv;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.MessageEvent;

public class FirstScript {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String credentials = dotenv.get("CREDENTIALS_JSON");
        String app_level_token = dotenv.get("APP_LEVEL_TOKEN");
        String bot_level_token = dotenv.get("BOT_LEVEL_TOKEN");
        final String  SpreadsheetId = dotenv.get("SPREADSHEET_ID");
        final String Range = dotenv.get("RANGE");
        Map<String, String> skyonicsMap = Map.of(
        "LIONEIGHT DOO", "LIONEIGHT",
        "Optima ELD", "OPTIMA",
        "Darex", "DAREX",
        "Routemate app", "RM"
        );
        App app = new App();
        
        System.out.println("Starting to read messages on slack...");
        app.event(MessageEvent.class, (payload, ctx) -> {
            ctx.ack();
            String eventID = payload.getEventId();
            if(!processedEvents.add(eventID)){
                System.out.println("Ignoring duplicate events" +eventID);
                return null;
            }
            try{
                System.out.println("Strating initialization of the sheet...");

                //Set up sheets and connect
                GoogleSheetsHelper sheetsHelper = new GoogleSheetsHelper(credentials);
                sheetsHelper.SetSpreadsheet(SpreadsheetId, Range);
                System.out.println("Sheet initialized!");
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

                System.out.println("Loaded Sheet");
                MessageEvent event = payload.getEvent();
            
                String text = event.getText();
                String channel = event.getChannel();
                String user = event.getUser();
                String ts = event.getTs();

                System.out.println("Message: " + text);
                System.out.println("Channel: " + channel);
                System.out.println("User: " + user);
                System.out.println("timestamp:" + ts);
            
                Slack slack = Slack.getInstance();

                String id_uredjaja = text.substring(text.length()-12);
                if(deviceMap.containsKey(id_uredjaja)) {
                    System.out.println("Found device:" + id_uredjaja);
                    int rowindex = deviceMap.get(id_uredjaja);
                    String counterRange = "'Sheet1'!D" + rowindex;
                    ValueRange counterResult = service.spreadsheets().values().get(SpreadsheetId, counterRange).execute();
                    int counter = 0;
                    if(counterResult.getValues() != null && !counterResult.getValues().isEmpty()) {
                        counter = Integer.parseInt(counterResult.getValues().get(0).get(0).toString());
                    }
                    counter ++;
                    ValueRange body = new ValueRange().setValues(List.of(List.of(counter)));
                    System.out.println("Incrementing device counter!");
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
                    System.out.println("Device not found on the sheet! Starting the procedure.");
                    try {
                    slack.methods(bot_level_token).reactionsAdd(r -> r
                        .channel(event.getChannel())
                        .timestamp(event.getTs())
                        .name("eyes")
                    );
                    } catch (Exception e) {
                        System.out.println("Reaction error: " + e.getMessage());
                    }
                    System.out.println("CHECKING FOR BLE CLIENT DATA...");
                    getLastBLEPackets lastBLEPacket = new getLastBLEPackets();
                    lastBLEPacket.setELD(id_uredjaja);
                    String customerName = lastBLEPacket.getCustomerName();
                    List<String> lastPacket = lastBLEPacket.getPacket();
                    System.out.println(lastPacket);
                    if (lastPacket.get(0).equals("IGN_OFF")){
                        try{
                            slack.methods(bot_level_token).reactionsAdd(r -> r
                            .channel(event.getChannel())
                            .timestamp(event.getTs())
                            .name("x")
                            );
                        }
                        catch (Exception e){
                            System.out.println("Reaction error: " + e.getMessage());
                        }
                    }
                    else if (lastPacket.get(0).equals("null")){
                        System.out.println("COULDN'T FIND CORRECT PACKETS, CHECK SKYONICS!");
                    }
                    else if (lastPacket.get(0).equals("ON_PERIODIC") && !lastPacket.get(2).equals("null")){
                        try{
                            slack.methods(bot_level_token).reactionsAdd(r -> r
                            .channel(event.getChannel())
                            .timestamp(event.getTs())
                            .name("white_check_mark")
                            );
                        }
                        catch (Exception e){
                            System.out.println("Reaction error: " + e.getMessage());
                        }
                    }
                    else if (lastPacket.get(0).equals("ON_PERIODIC") && lastPacket.get(2).equals("null")){
                        System.out.println("Starting to send commands!");
                        LaunchCommand commander = new LaunchCommand(customerName, id_uredjaja);
                        commander.sendCommand();
                        lastPacket = lastBLEPacket.getPacket();
                        if (lastPacket.get(0).equals("IGN_OFF")){
                        try{
                            slack.methods(bot_level_token).reactionsAdd(r -> r
                            .channel(event.getChannel())
                            .timestamp(event.getTs())
                            .name("x")
                            );
                        }
                        catch (Exception e){
                            System.out.println("Reaction error: " + e.getMessage());
                            }
                        }
                        else if (lastPacket.get(0).equals("ON_PERIODIC") && !lastPacket.get(2).equals("null")){
                            try{
                                slack.methods(bot_level_token).reactionsAdd(r -> r
                                .channel(event.getChannel())
                                .timestamp(event.getTs())
                                .name("white_check_mark")
                                );
                            }
                            catch (Exception e){
                                System.out.println("Reaction error: " + e.getMessage());
                            }
                        }
                        else{ //adding new device to the sheet
                            String issue;
                            if(lastPacket.get(1).equals("65535") && lastPacket.get(2).equals("null")){
                                issue = "BLE CLIENT BLE Firmware 65535";
                            }
                            else{
                                issue = "BLE Client";
                            }
                            ValueRange body = new ValueRange().setValues(
                            List.of(
                                List.of(
                                    id_uredjaja,
                                    skyonicsMap.get(customerName),
                                    "NIJE Sredjen",
                                    "1",
                                    issue
                                )
                            )
                            );
                            service.spreadsheets().values()
                            .append(SpreadsheetId, "'Sheet1'!A:E", body)
                            .setValueInputOption("RAW")
                            .setInsertDataOption("INSERT_ROWS")
                            .execute();
                            try{
                                slack.methods(bot_level_token).reactionsAdd(r -> r
                                .channel(event.getChannel())
                                .timestamp(event.getTs())
                                .name("sheet1")
                                );
                            }
                            catch (Exception e) {
                                System.out.println("Reaction error: " + e.getMessage());
                            }
                        }
                    
                    }
                    else {
                        System.out.println("Invalid data!");
                    }
                }

            }
            catch (Exception e) {
                System.out.println("ERROR PROCESSING EVENT:" + e.getMessage());
                e.printStackTrace();
            }
            return null; //Should never reach this code
        });
        SocketModeApp socketModeApp = new SocketModeApp(app_level_token, app);
        socketModeApp.start();
        
    }
    private static final Set<String> processedEvents =
        ConcurrentHashMap.newKeySet();
}
