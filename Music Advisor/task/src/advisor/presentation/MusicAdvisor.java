package advisor.presentation;

import advisor.Server;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class MusicAdvisor {
    private final Scanner scanner;
    private final String spotifyAccessServer;
    private final Server server;
    private boolean accessGranted = false;
    private final String CLIENT_ID = "b68e796f69654238a1631d9a19c67898";
    private final String CLIENT_SECRET = "4868231c3e094fc5b83fe6738040c85a";
    private final String REDIRECT_URI = "http://localhost:8080";

    public MusicAdvisor(String spotifyAccessServer) {
        scanner = new Scanner(System.in);
        this.spotifyAccessServer = spotifyAccessServer;
        server = new Server(this, spotifyAccessServer);

    }

    public void run() {
        while (true) {
            userPrompt();
        }
    }

    public void userPrompt() {
        String input = scanner.next();
        choices(input);

    }

    public void choices(String s) {
        switch (s.toLowerCase()) {
            case "featured":
                featured();
                break;
            case "new":
                newSelected();
                break;
            case "categories":
                categories();
                break;
            case "playlists":
                String category = scanner.next();
                playlist(category);
                break;
            case "auth":
                //String auth = scanner.next();
                String auth = String.format("https://accounts.spotify.com/authorize?client_id=%s&redirect_uri=%s&response_type=code", CLIENT_ID, REDIRECT_URI);
                auth(auth);
                break;
            case "exit":
                System.out.println("---GOODBYE!---");
                System.exit(0);
                break;
        }
    }

    public void featured() {
        if (!accessGranted){
            System.out.println("Please, provide access for application.");
            return;
        }
        System.out.println("---FEATURED---");
        System.out.println("Wake Up and Smell the Coffee");
    }
    public void newSelected() {
        if (!accessGranted){
            System.out.println("Please, provide access for application.");
            return;
        }
        System.out.println("---NEW RELEASES---");
        System.out.println("Mountains [Sia, Diplo, Labrinth]");
    }
    public void categories() {
        if (!accessGranted){
            System.out.println("Please, provide access for application.");
            return;
        }
        System.out.println("---CATEGORIES---");
        System.out.println("Mood");
        System.out.println("Pop");
    }
    public void playlist(String cat) {
        if (!accessGranted){
            System.out.println("Please, provide access for application.");
            return;
        }
        System.out.printf("---%s PLAYLISTS---\n", cat.toUpperCase(Locale.ROOT));
        System.out.println("Mood");
    }

    public void auth(String authUri){
        //startServer();
        //System.out.println("use this link to request the access code:");
        //System.out.println(authUri);
        start();
    }

    public void setAccessGranted(boolean accessGranted) {
        this.accessGranted = accessGranted;
    }

    public void startServer() {
        try {
            server.createServer(spotifyAccessServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        server.getAccessCode();
        server.getAccessToken2();
    }


}
