package com.eldsolution;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class getLastBLEPackets {
    public static void main(String[] args) throws IOException, InterruptedException{
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.skyonics.net/api/proxy/userlogin/login"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("UserName=lioneightops&Password=hR3%25Syd9"))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<String> cookies = response.headers().allValues("Set-Cookie");
        List<String> cookies_separated = new ArrayList<>();
        for(String cookie : cookies){
            cookies_separated.add(cookie.split(";")[0]);
        }
        System.out.println(cookies_separated);
        
        //UNFINISHED, NEEDS COMPLETE OVERHAUL THIS IS GARBAGE

        
        //System.out.println(response.headers().allValues("Set-Cookie"));
    }
}
