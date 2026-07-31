package com.eldsolution;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.apache.hc.core5.net.URIBuilder;



public class LaunchCommand {
    private static final String URL = "https://geometrissubscribersapi.azurewebsites.net/api/proxy/APIDeviceCommands";
    private String apikey;
    private String eld;
    private static final HttpClient client  = HttpClient.newHttpClient();
    Map<String, String> customerMap = Map.of(
        "LIONEIGHT DOO", "563o77hmgg6e7fszb2hzooh7qy",
        "Optima ELD", "vrendtlyjp7e5od7ivgfcsykce",
        "Darex", "pym4jckmfvje7gzlfu3ybwfuv4",
        "Routemate app", "r3mco5wkylrenpzrab2ukdvgha"
    );

    public LaunchCommand(String customerName,String ELD){
        apikey = customerMap.get(customerName);
        eld = ELD;
    }
    public void sendCommand () throws IOException, InterruptedException, URISyntaxException{
        URI uriFirstCommand = new URIBuilder(URL)
            .addParameter("serialNumber", eld)
            .addParameter("command", "SETPARAMS 542=1")
            .addParameter("mode", "0")
            .build();

            HttpRequest firstRequest = HttpRequest.newBuilder()
            .uri(uriFirstCommand)
            .header("accept", "application/json")
            .header("Basic", apikey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

            HttpResponse<String> responseFirstCommand = client.send(firstRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("First command status code" + responseFirstCommand.statusCode());

            Thread.sleep(120000); //120 seconds pause between next command

        if (eld.charAt(2)=='B'){
            System.out.println("FOUND B SERIES DEVICE");
            URI uriSeriesB = new URIBuilder(URL)
            .addParameter("serialNumber", eld)
            .addParameter("command", "BLETEST 34")
            .addParameter("mode", "0")
            .build();

            HttpRequest secondRequestSeriesB = HttpRequest.newBuilder()
            .uri(uriSeriesB)
            .header("accept", "application/json")
            .header("Basic", apikey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

            HttpResponse<String> responseSecondCommand = client.send(secondRequestSeriesB, HttpResponse.BodyHandlers.ofString());
            System.out.println("Second command status code:" + responseSecondCommand.statusCode());
        }
        else {
            System.out.println("FOUND X/A/U SERIES DEVICE");
            URI uriSeriesX = new URIBuilder(URL)
            .addParameter("serialNumber", eld)
            .addParameter("command", "BLETEST 9")
            .addParameter("mode", "0")
            .build();

            HttpRequest secondRequestSeriesX = HttpRequest.newBuilder()
            .uri(uriSeriesX)
            .header("accept", "application/json")
            .header("Basic", apikey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

            HttpResponse<String> responseSecondCommand = client.send(secondRequestSeriesX, HttpResponse.BodyHandlers.ofString());
            System.out.println("Second command status code:" + responseSecondCommand.statusCode());
        }
        Thread.sleep(130000);
    }
}
