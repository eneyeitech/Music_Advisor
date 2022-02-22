package advisor;

import advisor.presentation.MusicAdvisor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Server {
    private final String API = "https://accounts.spotify.com/api/token";
    private String code;
    private String spotifyAccessServer;
    private final String CLIENT_ID = "b68e796f69654238a1631d9a19c67898";
    private final String CLIENT_SECRET = "4868231c3e094fc5b83fe6738040c85a";
    private final MusicAdvisor musicAdvisor;
    public String ACCESS_TOKEN = "";
    public String ACCESS_CODE = "";
    public String SERVER_PATH = "https://accounts.spotify.com";
    public String REDIRECT_URI = "http://localhost:8080";

    public Server(MusicAdvisor musicAdvisor, String sp) {
        this.musicAdvisor = musicAdvisor;
        SERVER_PATH = sp;
    }

    public void createServer(String spotifyAccessServer) throws IOException {
        this.spotifyAccessServer = spotifyAccessServer;
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/",
                new HttpHandler() {
                    public void handle(HttpExchange exchange) throws IOException {


                        String query = exchange.getRequestURI().getQuery();
                        //System.out.println(query);
                        System.out.println("waiting for code...");
                        if (query != null) {
                            String[] entry = query.split("=");
                            if (entry.length > 1) {
                                if (entry[0].equalsIgnoreCase("code")) {
                                    code = entry[1];
                                    //System.out.println(code);
                                    //System.out.println("Got the code. Return back to your program.");
                                    String hello = "Got the code. Return back to your program.";
                                    exchange.sendResponseHeaders(200, hello.length());
                                    exchange.getResponseBody().write(hello.getBytes());
                                    exchange.getResponseBody().close();
                                    System.out.println("code received");
                                    //server.stop(1);
                                    try {
                                        System.out.println("making http request for access_token...");
                                        System.out.println("response:");
                                        getAccessToken();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    String hello = "Authorization code not found. Try again.";
                                    exchange.sendResponseHeaders(200, hello.length());
                                    exchange.getResponseBody().write(hello.getBytes());
                                    exchange.getResponseBody().close();
                                    //System.out.println("Authorization code not found. Try again.");
                                }
                            }else{
                                String hello = "Authorization code not found. Try again.";
                                exchange.sendResponseHeaders(200, hello.length());
                                exchange.getResponseBody().write(hello.getBytes());
                                exchange.getResponseBody().close();
                                //System.out.println("Authorization code not found. Try again.");
                            }
                        }

                    }
                }
        );

        server.start();
        //server.stop(1);
    }

    public boolean getAccessToken() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        String redirectURI = "http://localhost:8080";
        String grantType = "authorization_code";

        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);

        String parameter = String.format("" +
                "code=%s&" +
                "redirect_uri=%s&" +
                "grant_type=%s", code, redirectURI, grantType);

        // application/json
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", authHeader)
                .uri(URI.create(spotifyAccessServer + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(parameter))
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200) {
            System.out.println(response.body());
            musicAdvisor.setAccessGranted(true);
            System.out.println("---SUCCESS---");
            return true;
        } else {
            System.out.println("error");
            return false;
        }



    }

    /**
     * Getting access_code
     */
    public void getAccessCode() {
        //Creating a line to go to in the browser
        String uri = SERVER_PATH + "/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code";
        System.out.println("use this link to request the access code:");
        System.out.println(uri);

        //Creating a server and listening to the request.
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.start();
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            String query = exchange.getRequestURI().getQuery();
                            String request;
                            if (query != null && query.contains("code")) {
                                ACCESS_CODE = query.substring(5);
                                System.out.println("code received");
                                System.out.println(ACCESS_CODE);
                                request = "Got the code. Return back to your program.";
                            } else {
                                request = "Authorization code not found. Try again.";
                            }
                            exchange.sendResponseHeaders(200, request.length());
                            exchange.getResponseBody().write(request.getBytes());
                            exchange.getResponseBody().close();
                        }
                    });

            System.out.println("waiting for code...");
            while (ACCESS_CODE.length() == 0) {
                Thread.sleep(100);
            }
            server.stop(5);

        } catch (IOException | InterruptedException e) {
            System.out.println("Server error");
        }
    }

    /**
     * Getting access_token based on access_code
     */
    public void getAccessToken2() {

        System.out.println("making http request for access_token...");
        System.out.println("response:");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(SERVER_PATH + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code"
                                + "&code=" + ACCESS_CODE
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + REDIRECT_URI))
                .build();

        try {

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assert response != null;
            if (response.statusCode()==200) {
                musicAdvisor.setAccessGranted(true);
                System.out.println(response.body());
                System.out.println("---SUCCESS---");
            } else {
                System.out.println("error");
            }


        } catch (InterruptedException | IOException e) {
            System.out.println("Error response");
        }
    }
}
