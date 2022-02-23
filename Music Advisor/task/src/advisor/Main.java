package advisor;

import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, String> inputs = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) inputs.put(args[i], args[i+1]);
        if (inputs.containsKey("-access")) {
            Authorization.SERVER_PATH = inputs.get("-access");
        }
        if (inputs.containsKey("-resource")) {
            Authorization.API_SERVER_PATH = inputs.get("-resource");
        }
        if (inputs.containsKey("-page")) {
            Playlists.PAGES = Integer.parseInt(inputs.get("-page"));
        }
        Playlists playlists = new Playlists();
        playlists.run();
    }
}

class Playlists {
    static boolean isAuthorized = false;
    public static int PAGES = 5;
    Viewers current;
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String command = scanner.nextLine().trim();
            if (command.equals("exit")) {
                System.out.println("---GOODBYE!---");
                //System.exit(0);
            } else if (command.equals("auth")) {
                Authorization authorisation = new Authorization();
                authorisation.getAccessCode();
                authorisation.getAccessToken();
            } else if (!isAuthorized) {
                System.out.println("Please, provide access for application.");
            } else if (command.equals("categories")) {
                current = new Categories();
                current.printPlaylist();
            } else if (command.equals("featured")) {
                current = new Featured();
                current.printPlaylist();
            } else  if (command.equals("new")) {
                current = new NewClass();
                current.printPlaylist();
            } else if (command.contains("playlists ")) {
                String type = command.trim().substring(10);
                current = new ParticularPlaylist(type);
                current.printPlaylist();
            } else if (command.equals("prev")) {
                current.prev();
            } else if (command.equals("next")) {
                current.next();
            } else {
                System.out.println("Enter Valid Command.");
            }
        }
    }
}

class Authorization {
    public static String SERVER_PATH = "https://accounts.spotify.com";
    public static String REDIRECT_URI = "http://localhost:8080";
    public static String CLIENT_ID = "bc28fe5be37d4ea49470556b006d4e55";
    public static String CLIENT_SECRET = "430e02cd09ef4323be0d49a0eddc4721";
    public static String ACCESS_TOKEN = "BQBfp1KxkrPxIlkwidhU2rhhK1J9YKNqNn9nPTl1G2quWdZI8CGgL0AHHZ38qSwsvBk2cGppSnHHWRSS82YqKtNbPZF3ixc9a_NOjU1GePKC37zlNWyAZ9vLH0btI96R4HZPM0oX_QufMu8MKMFkn8VHJL8cOKpSGg\n";
    public static String API_SERVER_PATH = "https://api.spotify.com";
    public static String ACCESS_CODE = "";

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
    public void getAccessToken() {

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
            System.out.println(response.body());
            Playlists.isAuthorized = true;
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
            ACCESS_TOKEN = jo.get("access_token").getAsString();
            System.out.println("Success!");

        } catch (InterruptedException | IOException e) {
            System.out.println("Error response");
        }
    }
}

class Categories implements Viewers {
    static HashMap<Object, Object> categoryId;
    static List<String> categoriesList;
    static int pages;
    static int currentStartPage;
    static int prevStartPage;
    static int nextStartPage;

    public Categories() {
        categoryId = new HashMap<>();
        categoriesList = new ArrayList<>();
        pages = Playlists.PAGES;
        currentStartPage = 0;
        prevStartPage = -pages;
        nextStartPage = pages;
    }

    public void next() {
        if (nextStartPage >= categoriesList.size()) {
            System.out.println("No more pages.");
        } else {
            prevStartPage = currentStartPage;
            currentStartPage = nextStartPage;
            nextStartPage = Math.min(nextStartPage + pages, categoriesList.size());
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= categoriesList.size()) break;
                System.out.println(categoriesList.get(i));
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + categoriesList.size() / pages + "---");
        }
    }

    public void prev() {
        if (prevStartPage < 0) {
            System.out.println("No more pages.");
        } else {
            nextStartPage = currentStartPage;
            currentStartPage = prevStartPage;
            prevStartPage = prevStartPage - pages;
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= categoriesList.size()) break;
                System.out.println(categoriesList.get(i));
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + categoriesList.size() / pages + "---");
        }
    }

    public void printPlaylist() {
        getAllCategories();
        for (int i = currentStartPage; i < nextStartPage; i++) {
            if (i >= categoriesList.size()) break;
            System.out.println(categoriesList.get(i));
        }
        System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + categoriesList.size() / pages + "---");
    }


    static void getAllCategories() {
        String path = Authorization.API_SERVER_PATH + "/v1/browse/categories";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + Authorization.ACCESS_TOKEN)
                .uri(URI.create(path))
                .GET()
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();

            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject categories = json.getAsJsonObject("categories");
            JsonElement items = categories.getAsJsonArray("items");

            for (JsonElement element : items.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    String playlistName = element.getAsJsonObject().get("name").getAsString();
                    String playlistID = element.getAsJsonObject().get("id").getAsString();
                    categoryId.put(playlistName, playlistID);
                    categoriesList.add(playlistName);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Featured implements Viewers{
    static List<String> songName;
    static List<String> songLink;
    static int pages;
    static int currentStartPage;
    static int prevStartPage;
    static int nextStartPage;

    public Featured() {
        songName = new ArrayList<>();
        songLink = new ArrayList<>();
        pages = Playlists.PAGES;
        currentStartPage = 0;
        prevStartPage = -pages;
        nextStartPage = pages;
    }

    public void next() {
        if (nextStartPage >= songName.size()) {
            System.out.println("No more pages.");
        } else {
            prevStartPage = currentStartPage;
            currentStartPage = nextStartPage;
            nextStartPage = Math.min(nextStartPage + pages, songLink.size());
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }

    public void prev() {
        if (prevStartPage < 0) {
            System.out.println("No more pages.");
        } else {
            nextStartPage = currentStartPage;
            currentStartPage = prevStartPage;
            prevStartPage = prevStartPage - pages;
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }

    public void printPlaylist() {
        prepareFeaturedList();
        for (int i = currentStartPage; i < nextStartPage; i++) {
            if (i >= songName.size()) break;
            System.out.println(songName.get(i));
            System.out.println(songLink.get(i) + "\n");
        }
        System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
    }

    private void prepareFeaturedList() {
        String path = Authorization.API_SERVER_PATH + "/v1/browse/featured-playlists";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + Authorization.ACCESS_TOKEN)
                .uri(URI.create(path))
                .GET()
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();

            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject playlists = json.getAsJsonObject("playlists");

            JsonElement items = playlists.getAsJsonArray("items");

            for (JsonElement element : items.getAsJsonArray()) {

                if (element.isJsonObject()) {

                    songName.add(element.getAsJsonObject().get("name").getAsString());
                    songLink.add(element.getAsJsonObject().get("external_urls")
                            .getAsJsonObject().get("spotify").getAsString());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class NewClass implements Viewers{
    static List<String> songName;
    static List<String> songLink;
    static List<ArrayList<String>> artistsName;
    static int pages;
    static int currentStartPage;
    static int prevStartPage;
    static int nextStartPage;

    public NewClass() {
        songName = new ArrayList<>();
        songLink = new ArrayList<>();
        artistsName = new ArrayList<>();
        pages = Playlists.PAGES;
        currentStartPage = 0;
        prevStartPage = -pages;
        nextStartPage = pages;
    }

    public void next() {
        if (nextStartPage >= songName.size()) {
            System.out.println("No more pages.");
        } else {
            prevStartPage = currentStartPage;
            currentStartPage = nextStartPage;
            nextStartPage = Math.min(nextStartPage + pages, songLink.size());
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(artistsName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }
    @Override
    public void prev() {
        if (prevStartPage < 0) {
            System.out.println("No more pages.");
        } else {
            nextStartPage = currentStartPage;
            currentStartPage = prevStartPage;
            prevStartPage = prevStartPage - pages;
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(artistsName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }

    public void printPlaylist() {
        getNewReleases();
        for (int i = currentStartPage; i < nextStartPage; i++) {
            if (i >= songName.size()) break;
            System.out.println(songName.get(i));
            System.out.println(artistsName.get(i));
            System.out.println(songLink.get(i) + "\n");
        }
        System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
    }

    static void getNewReleases() {
        String path = Authorization.API_SERVER_PATH + "/v1/browse/new-releases";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + Authorization.ACCESS_TOKEN)
                .uri(URI.create(path))
                .GET()
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject albums = json.getAsJsonObject("albums");
            JsonElement items = albums.getAsJsonArray("items");

            for (JsonElement element : items.getAsJsonArray()) {

                if (element.isJsonObject()) {
                    songName.add(element.getAsJsonObject().get("name").getAsString());

                    JsonArray elementArray = element.getAsJsonObject().getAsJsonArray("artists");

                    ArrayList<String> artistsList = new ArrayList<>();

                    for (JsonElement el : elementArray) {
                        artistsList.add(el.getAsJsonObject().get("name").getAsString());
                    }
                    artistsName.add(artistsList);
                    songLink.add(element.getAsJsonObject().get("external_urls")
                            .getAsJsonObject().get("spotify").getAsString());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ParticularPlaylist implements Viewers {
    static Map<String, String> categoryId;
    static String str;
    static List<String> songName;
    static List<String> songLink;
    static int pages;
    static int currentStartPage;
    static int prevStartPage;
    static int nextStartPage;
    public ParticularPlaylist(String type) {
        str = type;
        categoryId = new HashMap<>();
        songName = new ArrayList<>();
        songLink = new ArrayList<>();
        pages = Playlists.PAGES;
        currentStartPage = 0;
        prevStartPage = -pages;
        nextStartPage = pages;
    }

    public void next() {
        if (nextStartPage >= songName.size()) {
            System.out.println("No more pages.");
        } else {
            prevStartPage = currentStartPage;
            currentStartPage = nextStartPage;
            nextStartPage = Math.min(nextStartPage + pages, songLink.size());
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }
    public void prev() {
        if (prevStartPage < 0) {
            System.out.println("No more pages.");
        } else {
            nextStartPage = currentStartPage;
            currentStartPage = prevStartPage;
            prevStartPage = prevStartPage - pages;
            for (int i = currentStartPage; i < nextStartPage; i++) {
                if (i >= songName.size()) break;
                System.out.println(songName.get(i));
                System.out.println(songLink.get(i) + "\n");
            }
            System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
        }
    }

    public void printPlaylist() {
        getCategories();
        getPlaylist();
        for (int i = currentStartPage; i < nextStartPage; i++) {
            if (i >= songName.size()) break;
            System.out.println(songName.get(i));
            System.out.println(songLink.get(i) + "\n");
        }
        System.out.println("---PAGE " + (currentStartPage/pages + 1) + " OF " + songName.size() / pages + "---");
    }

    private void getPlaylist() {
        if (categoryId.containsKey(str)) {
            String path = Authorization.API_SERVER_PATH + "/v1/browse/categories/" + categoryId.get(str) + "/playlists";

            HttpRequest request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + Authorization.ACCESS_TOKEN)
                    .uri(URI.create(path))
                    .GET()
                    .build();

            try {
                HttpClient client = HttpClient.newBuilder().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String jsonString = response.body();
                JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

                JsonObject playlists = json.getAsJsonObject("playlists");
                JsonElement items = playlists.getAsJsonArray("items");

                for (JsonElement element : items.getAsJsonArray()) {
                    songName.add(element.getAsJsonObject().get("name").getAsString());
                    songLink.add(element.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString());
                }
            } catch (IOException | InterruptedException | NullPointerException e) {
                System.out.println("Test unpredictable error message");
            }
        } else {
            System.out.println("Specified id doesn't exist");
        }
    }

    private void getCategories() {
        String path = Authorization.API_SERVER_PATH + "/v1/browse/categories";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + Authorization.ACCESS_TOKEN)
                .uri(URI.create(path))
                .GET()
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();

            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject categories = json.getAsJsonObject("categories");
            JsonElement items = categories.getAsJsonArray("items");

            for (JsonElement element : items.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    String playlistName = element.getAsJsonObject().get("name").getAsString();
                    String playlistID = element.getAsJsonObject().get("id").getAsString();
                    categoryId.put(playlistName, playlistID);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

interface Viewers {
    abstract void prev();
    abstract void next();
    abstract void printPlaylist();
}