package advisor;

import advisor.presentation.MusicAdvisor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String search1 = "-access";
        String search2 = "-resource";
        String spotifyAccessServer = "";

        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (arguments.contains(search1)) {
            spotifyAccessServer = args[arguments.indexOf(search1) + 1];
        } else {
            spotifyAccessServer = "https://accounts.spotify.com";
        }
        String spotifyResourceServer = "";
        if (arguments.contains(search2)) {
            spotifyResourceServer = args[arguments.indexOf(search2) + 1];
        } else {
            spotifyResourceServer = "https://api.spotify.com";
        }
System.out.println(spotifyAccessServer);
        System.out.println(spotifyResourceServer);
        MusicAdvisor musicAdvisor = new MusicAdvisor(spotifyAccessServer, spotifyResourceServer);
        musicAdvisor.run();


    }
}


