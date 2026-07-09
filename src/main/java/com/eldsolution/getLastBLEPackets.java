package com.eldsolution;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class getLastBLEPackets {
    public static void main(String[] args) throws IOException, InterruptedException{
        String url = "https://www.skyonics.net/api/";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url+"proxy/userlogin/login"))
            .header("Content-Type","application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("UserName=lioneightops&Password=hR3%25Syd9"))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        List<String> cookie_extract = response.headers().allValues("Set-Cookie");
        List<String> cookies = new ArrayList<>();
        for(String cookie : cookie_extract){
            cookies.add(cookie.split(";")[0]);
        }

        String cookie = String.join("; ",cookies);
        
        HttpRequest request1 = HttpRequest.newBuilder()
            .uri(URI.create(url+"proxy/devicehealthoperations/getcustomers"))
            .header("Content-Type", "application/json")
            .header("Cookie", cookie)
            .GET()
            .build();
        HttpResponse<String> response1 = client.send(request1,HttpResponse.BodyHandlers.ofString());
        System.out.println(response1.statusCode());
        String ELD = "87X051180474";
        Instant end_instance = Instant.now();
        Instant begin_instance = end_instance.minus(Duration.ofDays(2)); // od trenutka kada se pozove funkcija pa minus dva dana
        String Begin = begin_instance.toString(); //2026-05-25T04:59:59.999Z je format
        String end = end_instance.toString();
        String TZO = "-300";
        String Graph = "OBDOdometer";
        String Origin = "siteOperationsManagerDeviceanalysisPackets";

        HttpRequest get_device_info = HttpRequest.newBuilder()
            .uri(URI.create(url + "proxy/devicehealthoperations/getdevicehealthinfo?deviceSerialNumber=" + ELD))
            .header("Content-Type", "application/json")
            .header("Cookie", cookie)
            .GET()
            .build();
        HttpResponse<String> device_health_info = client.send(get_device_info, HttpResponse.BodyHandlers.ofString());
        String body_device_health_info = device_health_info.body();
        ObjectMapper device_health_info_mapper = new ObjectMapper();
        JsonNode device_health_json = device_health_info_mapper.readTree(body_device_health_info);
        String customerID = device_health_json.path("Data").path("CustomerId").asText("null");
        String customerName = device_health_json.path("Data").path("CustomerName").asText("null");
        System.out.println(customerID);
        System.out.println(customerName);
        
 
        HttpRequest get_anaview = HttpRequest.newBuilder()
            .uri(URI.create(url+"proxy/devicehealthoperations/getanalysisview"))
            .header("Content-type", "application/x-www-form-urlencoded")
            .header("Cookie", cookie)
            .POST(HttpRequest.BodyPublishers.ofString("SerialNumber="+ELD+"&Begin="+Begin+"&End="+end+"&TZOffset="+TZO+"&GraphColumn="+Graph+"&Origin="+Origin))
            .build();
        HttpResponse<String> anaresponse = client.send(get_anaview,HttpResponse.BodyHandlers.ofString());
        String body = anaresponse.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(body);
        //JsonNode tabledata = json.get("Data").get("TableData").get(0); newest packet
        JsonNode tabledata = json.get("Data").get("TableData");
        for (int i = 0; i < 10; i ++){
            String MessageReason = tabledata.get(i).path("RowData").path("MessageReason").asText("null");
            System.out.println(MessageReason);
            if (MessageReason.equals("ON_PERIODIC")){
                String BLEClient = tabledata.get(i).path("RowData").path("BLEClient").asText("null");
                System.out.println(BLEClient);
                String BluetoothFirmwareVersion = tabledata.get(0).path("RowData").path("BluetoothFirmwareVersion").asText("null");
                System.out.println(BluetoothFirmwareVersion);
                break;
            }
        }
        //Poboljsati logiku za proveru paketa koji je stigao a zatim osposobiti pustanje komandi
    }
}
