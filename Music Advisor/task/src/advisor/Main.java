package advisor;

import advisor.presentation.MusicAdvisor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String search = "-access";
        String spotifyAccessServer = "";
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (arguments.contains(search)) {
            spotifyAccessServer = args[arguments.indexOf(search) + 1];
        } else {
            spotifyAccessServer = "https://accounts.spotify.com";
        }

        MusicAdvisor musicAdvisor = new MusicAdvisor(spotifyAccessServer);
        musicAdvisor.run();


    }
}
