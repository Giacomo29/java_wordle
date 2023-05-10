/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordle_esame;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;  
import java.security.MessageDigest;  
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;



/**
 *
 * @author giacomotorbidoni
 *
 */





/**

Questa classe ha il compito di semplificare il codice del task del server, portando tutte le letture e scritture
in una classe separata. La scelta di separare questa logica da quella del task è stata fatta per rendere il codice
più organizzato e per renderlo più scalabile.
Infatti, questa classe genera le strutture dati necessarie per il ServerMain, ma rende completamente trasparente
il supporto di memorizzazione persistente dei dati, consentendo di sostituire, ad esempio, il file utenti.json,
con un database più efficiente, semplicemente cambiando questa classe.
In questo modo, la classe diventa un'astrazione della gestione dei dati per il server, consentendo di utilizzare
una varietà di meccanismi di archiviazione dati senza dover modificare il codice del task.
*/



public class GestoreFile {
    

    
    private static String DIR_CONF = "config/";
    private static String USERFILE = "utenti.json";
    private static String SERVERCONFIG = "serverdetail.txt";
    private static String WORDLIST = "words.txt";
    private static String CLIENTCONFIG = "clientdetail.txt";
    private static String parola;
    
    GestoreFile(){  
    }
    
    
    
    //metodo che mi restituisce una ConcurrentHashMap di utenti, leggendoli dal file 
    public static ConcurrentHashMap<String, Utente> getUtenti(){
        Gson gson = new Gson();
        ConcurrentHashMap<String, Utente> mappaUtenti = new ConcurrentHashMap<String, Utente>();
        try {
            Utente[] utenti = gson.fromJson(new FileReader(DIR_CONF+USERFILE), Utente[].class);
            
            for (Utente u : utenti) {
                mappaUtenti.put(u.username,u);
            }

            
        }catch (JsonSyntaxException | JsonIOException | IOException e) {
            e.printStackTrace();
        }
        
        return mappaUtenti;
    }
    
    
    
    //metodo per salvare gli utenti e i nuovi dati su file passandogli la struttura dati
    public static void saveUtenti(ConcurrentHashMap<String, Utente> mappaUtenti) {
        
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(DIR_CONF+USERFILE)) {
            
            gson.toJson(mappaUtenti.values(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    //metodo che restituisce un oggetto RandomSet contentente tutte le paroel del file words.txt
    public static RandomSet<String> getParole(){
        RandomSet<String> parole = new RandomSet<String>();
        String line;
        try(BufferedReader rd = new BufferedReader(new FileReader(DIR_CONF+WORDLIST))){
            while((line = rd.readLine()) != null){
                parole.add(line);
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("Errore apertura file " + WORDLIST);
        }
        
        catch(IOException ex){
            System.out.println("Errore in lettura del file "+ WORDLIST);
        }
        
        return parole;
        
    }
    
    
    //metodo che restituisce una struttura dati contentente tutte le info per il server
    public static ConcurrentHashMap<String, String> serverDetail(){
        ConcurrentHashMap<String,String> configMap = new ConcurrentHashMap<String,String>();
        try(BufferedReader br = new BufferedReader(new FileReader(DIR_CONF+SERVERCONFIG))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("=");
                if(parts.length == 2){
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    configMap.put(key,value);
                }
            }
               
        }
        catch(FileNotFoundException ex){
            System.out.println("Errore apertura file " + SERVERCONFIG);
        }
        catch(InputMismatchException ex){
            System.out.println("Errore formattazione file " + SERVERCONFIG);
        }
        catch(IOException ex){
            System.out.println("Errore file " + SERVERCONFIG);
        }
        return configMap;
    }
    
    
     //metodo che restituisce una struttura dati contentente tutte le info per il client
    public static ConcurrentHashMap<String, String> clientDetail(){
        ConcurrentHashMap<String,String> configMap = new ConcurrentHashMap<String,String>();
        try(BufferedReader br = new BufferedReader(new FileReader(DIR_CONF+CLIENTCONFIG))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("=");
                if(parts.length == 2){
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    configMap.put(key,value);
                }
            }
               
        }
        catch(FileNotFoundException ex){
            System.out.println("Errore apertura file " + SERVERCONFIG);
        }
        catch(InputMismatchException ex){
            System.out.println("Errore formattazione file " + SERVERCONFIG);
        }
        catch(IOException ex){
            System.out.println("Errore file " + SERVERCONFIG);
        }
        return configMap;
    }
    
    
    
    
    
    
    
    public static void main(String[] args) {
        
        
        
        GestoreFile prova = new GestoreFile();
        
        RandomSet<String> parole = new RandomSet<>();
        
        parole = prova.getParole();
        
        //parole.printAll();
        parola = parole.getRandom();
        System.out.println(parola);

        
        /*
        prova.getUtenti();
        ConcurrentHashMap<String, Utente> mappaUtenti = new ConcurrentHashMap<String, Utente>();
        mappaUtenti = prova.getUtenti();
        
        mappaUtenti.entrySet().forEach(entry -> {
        System.out.println(entry.getKey() + " " + entry.getValue());
        });
        
        */
        //prova.saveUtenti(mappaUtenti);
        /*
        
        ConcurrentHashMap<String,String> serverInfo = new ConcurrentHashMap<String,String>();
        
        serverInfo = prova.serverDetail();
        PORT = Integer.parseInt(serverInfo.get("PORT"));
        
        TIMEOUT = Integer.parseInt(serverInfo.get("TIMEOUT"));
        DELAY = Integer.parseInt(serverInfo.get("DELAY"));
        PORT_SHARE = Integer.parseInt(serverInfo.get("PORT_SHARE"));
        PORT_MULTICAST = Integer.parseInt(serverInfo.get("PORT_MULTICAST"));
        ADDRESS_MULTICAST = serverInfo.get("ADDRESS_MULTICAST");
        
        
        System.out.println(PORT);
        System.out.println(TIMEOUT);
        System.out.println(DELAY);
        System.out.println(PORT_SHARE);
        System.out.println(PORT_MULTICAST);
        System.out.println(ADDRESS_MULTICAST);
        */
        
        
        
    }
    
    
}



