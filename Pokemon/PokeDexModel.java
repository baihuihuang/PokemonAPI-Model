/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pokemon;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Baihui
 */
public class PokeDexModel {

    public static JSONObject connectPokeAPI(String searchWord) {

        int status = 0;
        String response = "";
        HttpURLConnection conn;
        JSONObject pokemon = new JSONObject();
        try {
            // Call the API to get Pokemon information using the index in URL
            URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + searchWord);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Setting request property for API
            conn.setRequestProperty("User-Agent", "cheese");
            status = conn.getResponseCode();

            if (status != 200) {
                String msg = conn.getResponseMessage();
                System.out.println(msg);
            }

            // Reading output from API
            String output = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((output = br.readLine()) != null) {
                response += output;
            }

            // Cast the output to a JSON Object
            JSONObject temp = new JSONObject(response);

            // Perform business logic in parsingPokemonInfo method
            pokemon = parsingPokemoninfo(temp);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // return parsed JSONObject to servlet
        return pokemon;
    }

    // Extracting the pokemon infomation my client needs
    // and calling Qwant API to get the funny Pokemon picture
    public static JSONObject parsingPokemoninfo(JSONObject json) throws IOException {
        JSONObject pokemon = new JSONObject();
        try {
            // extracting the information client wants 
            String pokemonName = json.getString("name");
            String imageURL = connectImageAPI(pokemonName);
            String weight = Integer.toString(json.getInt("weight"));
            String height = Integer.toString(json.getInt("height"));
            JSONArray types = json.getJSONArray("types");
            String[] typeNames = new String[types.length()];
            for (int i = 0; i < types.length(); i++) {
                typeNames[i] = types.getJSONObject(i).getJSONObject("type").getString("name");
            }

            // Extract speciesURL to call the API again 
            String speciesURL = json.getJSONObject("species").getString("url");

            // call the API again
            URL url = new URL(speciesURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "cheese");

            int status = conn.getResponseCode();

            if (status != 200) {
                String msg = conn.getResponseMessage();
                System.out.println(msg);
            }

            String output = "";
            String response = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((output = br.readLine()) != null) {
                response += output;
            }

            String flavorText = "";
            JSONObject species = new JSONObject(response);
            JSONArray flavor_text_entries = species.getJSONArray("flavor_text_entries");
            for (int i = 0; i < flavor_text_entries.length(); i++) {
                JSONObject language = flavor_text_entries.getJSONObject(i).getJSONObject("language");
                String flavor_text = flavor_text_entries.getJSONObject(i).getString("flavor_text");
                if (language.getString("name").equals("en")) {
                    flavorText = flavor_text;
                    break;
                }
            }

            JSONArray genera = species.getJSONArray("genera");
            String genus = "";
            for (int i = 0; i < genera.length(); i++) {
                JSONObject language = genera.getJSONObject(i).getJSONObject("language");
                if (language.getString("name").equals("en")) {
                    genus = genera.getJSONObject(i).getString("genus");
                }
            }

            // get the evolution chain by calling the API again
            String evolutionURL = species.getJSONObject("evolution_chain").getString("url");

            url = new URL(evolutionURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "cheese");

            status = conn.getResponseCode();

            //System.out.println(status);
            if (status != 200) {
                String msg = conn.getResponseMessage();
                System.out.println(msg);
                //return String.valueOf(conn.getResponseCode());
            }

            output = "";
            response = "";
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((output = br.readLine()) != null) {
                response += output;
            }

            JSONObject evolution_chain = new JSONObject(response);

            JSONObject chain = evolution_chain.getJSONObject("chain");
            species = chain.getJSONObject("species");
            String[] species_chain = new String[3];
            species_chain[0] = species.getString("name");
            JSONArray evolves_to = chain.getJSONArray("evolves_to");

            for (int i = 1; i < 3; i++) {
                if (evolves_to.length() != 0) {
                    species = evolves_to.getJSONObject(0).getJSONObject("species");
                    //JSONObject species = evolves_to
                    species_chain[i] = species.getString("name");
                    evolves_to = evolves_to.getJSONObject(0).getJSONArray("evolves_to");
                } else {
                    break;
                }
            }

            // put everything together to pass this back to client
            pokemon.put("name", pokemonName);
            pokemon.put("imageURL", imageURL);
            pokemon.put("weight", weight);
            pokemon.put("height", height);
            pokemon.put("typeNames", String.join(",", typeNames));
            pokemon.put("flavorText", flavorText);
            pokemon.put("genus", genus);
            pokemon.put("species_chain", String.join(",", species_chain));

        } catch (JSONException ex) {
            Logger.getLogger(PokeDexModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(PokeDexModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return pokemon;
    }

    // this is the method for search funny pictures of Pokemon
    // Because pokemon APi does not contains ant pictures
    public static String connectImageAPI(String searchWord) {

        int status = 0;
        String response = "";
        HttpURLConnection conn;
        JSONObject pokemon = new JSONObject();
        String imageURL = "";
        try {
            URL url = new URL("https://api.qwant.com/api/search/images?count=1&offset=1&q=" + searchWord);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("User-Agent", "cheese");
            status = conn.getResponseCode();

            //System.out.println(status);
            if (status != 200) {
                String msg = conn.getResponseMessage();
                System.out.println(msg);
                return "";//String.valueOf(conn.getResponseCode());
            }

            String output = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((output = br.readLine()) != null) {
                response += output;
            }

            pokemon = new JSONObject(response);

            JSONObject data = pokemon.getJSONObject("data");
            JSONObject result = data.getJSONObject("result");
            JSONObject items = result.getJSONArray("items").getJSONObject(0);

            imageURL = items.getString("media");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return imageURL;

    }

    /**
     * This method log information from API to MongoDB
     * @param pokemon JSONObject with pokemon information
     * @param mobile USer Agent
     * @throws JSONException 
     */
    public void mongoDBlog(JSONObject pokemon, String mobile) throws JSONException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        MongoClientURI uri = new MongoClientURI("mongodb://baihuih:sesame@ds237409.mlab.com:37409/project4task2");
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());

        MongoCollection<Document> pokemons = db.getCollection("pokemons");
        MongoCollection<Document> pokemon_types = db.getCollection("types");

        String pokemonName = pokemon.getString("name");
        String genus = pokemon.getString("genus");
        String[] types = pokemon.getString("typeNames").split(",");
        String[] evolution_chain = pokemon.getString("species_chain").split(",");

        List<Document> seedData = new ArrayList<Document>();
        seedData.add(new Document("name", pokemonName)
                .append("genus", genus)
                .append("number_of_evolves", evolution_chain.length)
                .append("mobile", mobile)
                .append("types",types.length));

        pokemons.insertMany(seedData);

        for (String type : types) {
            Document typeData = new Document();
            typeData.append("type", type);
            pokemon_types.insertOne(typeData);
        }
        client.close();
    }
}
