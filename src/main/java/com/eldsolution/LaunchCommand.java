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
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class LaunchCommand {
    String BASE_URL = "https://geometrissubscribersapi.azurewebsites.net/api";
    String apikey;
    String eld;
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



}
