import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import net.minecraft.util.com.google.gson.Gson;
import net.minecraft.util.com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class getLevelSetTest
{
    public static void main(String[] args)
    {
        ArrayList<ArrayList<String>> aa = abc();
        System.out.println();
    }

    public static ArrayList<ArrayList<String>> abc() {
        String filepath = new File(System.getProperty("user.dir"), "test.json").getPath();
        try (FileReader file = new FileReader(filepath)) {
            JsonParser parser = new JsonParser();
            return new Gson().fromJson(parser.parse(file).getAsJsonObject().entrySet().stream().collect(
                            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    .get("level_sets"), ArrayList.class);

        } catch (IOException e) {
            AOSPlayerManager.LOGGER.warning("Failed to read JSON from file " + filepath);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}

