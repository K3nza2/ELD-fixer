package com.eldsolution;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        System.out.println(response1.body());
        String ELD = "87A041770089";
        String Begin = "2026-05-22T05:00:00.000Z";
        String end = "2026-05-25T04:59:59.999Z";
        String TZO = "-300";
        String Graph = "OBDOdometer";
        String Origin = "siteOperationsManagerDeviceanalysisPackets";

        HttpRequest get_anaview = HttpRequest.newBuilder()
            .uri(URI.create(url+"proxy/devicehealthoperations/getanalysisview"))
            .header("Content-type", "application/x-www-form-urlencoded")
            .header("Cookie", cookie)
            .POST(HttpRequest.BodyPublishers.ofString("SerialNumber="+ELD+"&Begin="+Begin+"&End="+end+"&TZOffset="+TZO+"&GraphColumn="+Graph+"&Origin="+Origin))
            .build();
            //UNFINISHED
        HttpResponse<String> anaresponse = client.send(get_anaview,HttpResponse.BodyHandlers.ofString());
        String body = anaresponse.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(body);
        JsonNode tabledata = json.get("Data").get("TableData").get(0);
        System.out.println(tabledata); // i need Message Reason, Bluetooth Firmware Version and BLE Client
        String BLEClient = tabledata.path("RowData").path("BLEClient").asText("null");//vreca vrednost koja nije null ako uredjaj ima BLE
        System.out.println(BLEClient);
        String MessageReason = tabledata.path("RowData").path("MessageReason").asText("null");
        System.out.println(MessageReason);
        String BLEFirmware = tabledata.path("RowData").path("BluetoothFirmwareVersion").asText("null");
        System.out.println(BLEFirmware);
    }
}
