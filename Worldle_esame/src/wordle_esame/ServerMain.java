/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordle_esame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author giacomotorbidoni
 */

/**
 * 
 * La classe ServerMain.java è fondamentale per la gestione del server e della comunicazione con i giocatori.
 * Grazie all'utilizzo di threadPool e Socket TCP, è possibile gestire in modo efficiente
 * le connessioni dei giocatori. Inoltre, la gestione del server multicast permette la condivisione dei 
 * risultati delle partite in modo semplice ed efficace.
 * La presenza dei metodi principali nella classe rende il codice più organizzato e leggibile.
 * 
 */






public class ServerMain {
    
    //info server -> ottenute da serverconfig.txt
    
    private static int PORT;
    private static int TIMEOUT;
    private static int DELAY;
    private static int PORT_SHARE;
    private static int PORT_MULTICAST;
    private static String ADDRESS_MULTICAST;
    
    
    //strutture dati di gioco
    
    private static boolean stopServer = false;
    private static ConcurrentHashMap<String,Utente> utenti;
    private static ConcurrentHashMap<String,Boolean> giocate_utenti;
    private static String parola;
    private static RandomSet<String> parole;
   
        
    
    public void setUtente(String username){
        giocate_utenti.put(username,true);
    }
    
    
    
    //metodo per settare il server all'avvio, importando i vari parametri dal file di configurazione, e inizializzare le strutture dati
    private static void startServer(){
        
        //ottengo dal file di configurazione una hashMap con tutti i dettagli e 
        //li vado a salvare nelle costanti opportune
        ConcurrentHashMap<String,String> serverInfo = new ConcurrentHashMap<String,String>();
        
        serverInfo = GestoreFile.serverDetail();
        PORT = Integer.parseInt(serverInfo.get("PORT"));
        TIMEOUT = Integer.parseInt(serverInfo.get("TIMEOUT"));
        DELAY = Integer.parseInt(serverInfo.get("DELAY"));
        PORT_SHARE = Integer.parseInt(serverInfo.get("PORT_SHARE")); 
        PORT_MULTICAST = Integer.parseInt(serverInfo.get("PORT_MULTICAST"));
        ADDRESS_MULTICAST = serverInfo.get("ADDRESS_MULTICAST");
        
        //dal file utenti.json ricavo una lista contenenti tutti gli utenti
        
        
        utenti = GestoreFile.getUtenti();
        
        
        
        //la hashMap "giocate_utenti" serve per tener conto delle parole che un utente ha gia giocato
        giocate_utenti = new ConcurrentHashMap<>();
        
        
       
        
        //ottengo tutte le parole dal vocabolario
        
        parole = GestoreFile.getParole();
 
             
    }
    
    
    //metodo che passato una Stringa (un username) restituisce l'oggetto utente corrispondente 
    public static Utente getUser(String username){
        return utenti.get(username);
        }
    
    
    //metodo per la registrazione di un nuovo utente all'interno della struttura dati
    //vengono passati al metodo come stringhe username e password
    public static String registerUser(String username,String password){
        if(utenti.containsKey(username)){
            return "--AE"; //se lo username è presente vuol dire che l'account esiste gia ("--AccountEsistente")
        }
        if(password.equals("")){
            return "--EP"; //se la passoword è vuota restituisco un errore --ErrorePassword
        }
        //altrimenti posso creare l'oggetto, lo inserisco nella struttura dati e invio la conferma --AccountCreato
        Utente usr = new Utente(username,password);
        utenti.put(username, usr);
        return "--AC";
        
        
    }
    
    //metodo per la generazione della parola da indovinare
    private static void wordGenerator(){

        //genero una nuova parola per tutti pescandola in maniera random
        parola = parole.getRandom();
        //ogni nuova parola azzero la struttura dati giocate_utenti in modo che possano giocare di nuovo tutti
        
        giocate_utenti.clear();
        
        
        Instant now = Instant.now();
        System.out.println("PAROLA GENERATA at "+ now.toEpochMilli() + ": " + parola);
        
    }
    
    
    //TODO rimuovere e correggere
    private static void endServer(){
        try(Scanner cmd = new Scanner(System.in);){
            if(cmd.hasNext()){
                if(cmd.nextLine().equals("STOP"));
                stopServer = true;
                System.out.println("Shutting down");
                GestoreFile.saveUtenti(utenti);
            }
        }
            
    }
    
    
    //metodo per il controllo della presenza di una parola all'interno del vocabolario delle parole
    public static Boolean containsWord(String parola){
        return parole.contains(parola);
    }
   
    
    //metodo per il controllo della giocata del giocatore
    public static String matches_played(String username){

        Boolean tmp = ServerMain.giocate_utenti.get(username); //guardo se il giocatore ha gia giocato la parola
        if(tmp != null){
           return "--PG"; // il giocatore ha gia giocato per questa parola
        }
        
        giocate_utenti.put(username,true); //se non ha giocato segno che lo ha appena fatto
        return parola;
    }
    
    //metodo per il controllo della giocata del giocatore senza aggiornare la struttura dati
    public static String check_matches_played(String username){

        Boolean tmp = ServerMain.giocate_utenti.get(username); //guardo se il giocatore ha gia giocato la parola
        if(tmp != null){
           return "--PG"; // il giocatore ha gia giocato per questa parola
        }
        
        return parola;
    }
    
    
    
    //metodo per il salvataggio dell'utente presente nella struttura dati
    public static void saveAndUpdate(String user){
        Utente tmp = utenti.get(user);
        utenti.replace(user, tmp);
    }
    
   
        //metodo per la gestione del server multicast per le classifiche
    public static void multicast(){
        try(MulticastSocket ms= new MulticastSocket(PORT_MULTICAST); DatagramSocket s = new DatagramSocket(PORT_SHARE);){
            InetAddress group = InetAddress.getByName(ADDRESS_MULTICAST);
            ms.joinGroup(group);
            ms.setSoTimeout(TIMEOUT);
            s.setSoTimeout(TIMEOUT);
            while(!stopServer){
                try{
                    DatagramPacket request = new DatagramPacket(new byte[1024],1024);
                    s.receive(request);
                    String msg = new String(request.getData());
                    msg = request.getPort() + "/" + msg.trim(); //utilizzo questa struttura del mesaggio per poter poi identificare il proprietario del messaggio tramite split sul carattere $
                    System.out.println("CONDIVISIONE: " + msg);
                    DatagramPacket send = new DatagramPacket(msg.getBytes(),msg.getBytes().length,group,PORT_MULTICAST);
                    ms.send(send);
                }
                catch(SocketTimeoutException ex){}
                catch(IOException ex){
                    System.out.println("ERRORE: socket in multicast");
                }
            }
        }catch(UnknownHostException ex){
            System.out.println("ERRORE: impossibile collegarsi in multicast");
        }
        catch(IOException ex){
            System.out.println("ERRORE: socket");
        }
    }


       
    
    
    
    public static void main(String[] args) throws Exception {
        
        //TODO cambiare commento sotto
        //ogni client che si connette viene gestito da un thread del threadPool, se si raggiunge il limite massimo di 10 utenti connessi contemporaneamente
        //i nuovi client verranno messi in attesa
        startServer();
        wordGenerator();
        System.out.println("PORT="+PORT);
        System.out.println("TIMEOUT="+TIMEOUT);
        System.out.println("DELAY="+DELAY);
        System.out.println("PORT_SHARE="+PORT_SHARE);
        System.out.println("PORT_MULTICAST="+PORT_MULTICAST);
        System.out.println("ADDRESS_MULTICAST="+ADDRESS_MULTICAST);
        
       

        //thread per la gestione del cabmio parola ogni DELAY secondi
        ExecutorService serverThreads = Executors.newFixedThreadPool(3);
        
        
        //Thread per la gestione del gruppo multicast
        serverThreads.submit(ServerMain::multicast);
       
        
        serverThreads.execute(new Thread()
        {
           @Override
           public void run(){
               while(true){
                try {
                   Thread.sleep(DELAY*1000);
                   wordGenerator(); //genero una nuova parola
                   System.out.println("word cambiata");
               } catch (InterruptedException ex) {
                   Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
               }
              }
           }
        });
        //tramite questo metodo il thread si avvia tramite Signal (quando viene stoppato il server) e vengono salvati i dati
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run(){
                GestoreFile gestore = new GestoreFile();
                for (Utente utente : utenti.values()){
                    utente.log_in = false;
                }
                gestore.saveUtenti(utenti);
            }
        }
);
        
    
        
        
        //viene avviato il socket listener responsabile di accettare le connessioni in entrata
        
        try (ServerSocket listener = new ServerSocket(PORT)) 
            {
            System.out.println("The server is running...");
            listener.setSoTimeout(TIMEOUT);
            ExecutorService tasks = Executors.newCachedThreadPool();
                while (!stopServer) {
                    try{
                        tasks.submit(new Task(listener.accept())); 
                    }catch(IOException e){}
                }
                
                //termino tutti i task prima di chiudere il server e termino anche il thread per il multicast
                tasks.shutdown();
                tasks.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
                serverThreads.shutdown();
                serverThreads.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
            }
    }
    
    

        
    }
    

