/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordle_esame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;

/**
 *
 * @author giacomotorbidoni
 * 
 */


/*
Server e logica del gioco Wordle.



La classe Task.java rappresenta un'istanza di Thread per ogni utente connesso al server. La classe
implementa l'interfaccia Runnable e viene istanziata nella classe ServerMain.java. La comunicazione
tra il client e il task avviene tramite un oggetto Socket, dopo la chiamata del metodo accept()
nella classe ServerMain.java.
Task.java contiene i metodi più importanti del programma, in quanto la gestione del gioco e dei dati
è affidata al server. 



*/


public class Task implements Runnable {
    
 
    //Variabili per la gestione dei colori
    //-----------------------
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    //-------------------------
    
    
    private final Socket socket;
    
    //variabili di gioco
    private Utente userInGame = null;
    private int attempt = 0;
    private boolean stopServer = false;
    private String secretWord;
    private boolean win,lose = false;

    //ServerMain prende come parametro l'oggetto socket, creato nel main
    Task(Socket socket) {
        this.socket = socket;
    }
    
    
    private void register(Scanner in,PrintWriter out){
        String username = in.nextLine(); //prendo lo username dal client
        String password = in.nextLine();//prendo la password dal client
        out.println(ServerMain.registerUser(username,password)); //eseguo la registrazione e stampo l'esito al client
    }
    
    
    
    private void login(Scanner in,PrintWriter out){
        //forse da togliere
        if(userInGame != null && userInGame.log_in){
            out.println("--LE"); // se userInGame è diverso da null c'è gia un utente connesso (--LoginEffettuato)
        }
        String username = in.nextLine();//prendo lo username dal client
        System.out.println("Tentato accesso:");
        System.out.println("username: " + username);
        String password = in.nextLine();//prendo la password dal client
        System.out.println("password: " + password);
        Utente user = ServerMain.getUser(username);
        
        if(user != null){
            
            if(user.log_in == true){
                out.println("--LE"); //in questo caso questo l'utente è gia stato loggato, lo comunico con il messaggio (--LoginEffettuato)
                user = null;
                return;
            }
            
            if(user.getPassword().equals(password)){
                Instant now = Instant.now();
                out.println("--OK"); //login andato a buon fine
                userInGame = user;
                userInGame.log_in = true;
                System.out.println("=>"+userInGame.username + "Logged in at " + now.toEpochMilli());//stampo il timestamp sul server del login
            }
            else{
                out.println("--PE"); //invio al client l'errore (--PasswordErrata)
                return;
            }
                  
        }
        else{
            out.println("--UE"); //invio al client l'errore dello username errato o inesistente
        }
    }
    
    
    private void logout(PrintWriter out){
        
        Instant now = Instant.now();
        //i casi in cui si puo effettuare il logout sono 2: nel mentre di una partita(sconfitta) o nel menu del gioco
        
        if(userInGame == null){
            out.println("--LNE"); //in questo caso non era stato effettuato alcun login
            return;
        }
        
        
        //da controllare bene secretWord == null --->>>>>>
        if( secretWord == null || !secretWord.equals(ServerMain.check_matches_played(userInGame.username))){
            updateStreak();
        }
        
        if(userInGame.in_playing){ //in questo caso il giocatore è in partita
            userInGame.saveMatch(false,attempt);
            userInGame.in_playing = false;
            userInGame.log_in = false;
            System.out.println("UTENTE : " + userInGame.username +  " -> " + "disconnessione eseguita at: " + now.toEpochMilli());
            out.println("--DE"); //invio il messaggio al client(--DisconnessioneEseguita)
            userInGame = null;
            stopServer = true;
            return;
        }
        
        if(!userInGame.in_playing){ //caso in cui l'utente cerchi di effetture il logout nel menu di gioco
            System.out.println("UTENTE : " + userInGame.username +  " -> " + "disconnessione eseguita at: " + now.toEpochMilli());
            userInGame.log_in = false;
            out.println("--DE");
            userInGame = null;
            stopServer = true;
            return;
        }
        
    }
    
    
    //metodo per stampare le statistiche dello user connesso userInGame
    private void printStats(PrintWriter out){
        
        //formatto in maniera da lavorarci l'array distribution
        String distribution = "";
        for(int i:userInGame.distribution){
            distribution += i + ",";
        }
        
        //concateno le varia stringhe che riguardano le statistiche e le inoltro al client per l'elaborazione
        out.println("-"+userInGame.username + "-"+ userInGame.partite_giocate +"-" + userInGame.vittorie + "-" + userInGame.streak + "-" + userInGame.max_streak
        + "-:" + distribution);    
    }
    
    
    //metodo che aggiorna le streak, se l'attuale è maggiore di max_streak la aggiorno
    private void updateStreak(){
        if(userInGame.in_playing && !win){
            if(userInGame.streak > userInGame.max_streak) userInGame.max_streak = userInGame.streak; 
            userInGame.streak = 0;
        }
    }
    
    
    
    //medoto che il client utilizza per iniziare una partita (per comunicare al server che vuole giocare)
    //prende in input un oggetto PrintWriter
    private void playWordle(PrintWriter out){
        
        try{
        if(userInGame == null || !userInGame.log_in){
            out.println("--EL"); // se si cerca di giocare senza essere loggati restituisce un errore (--EffettuareLogin)
            return;
        }
        System.out.println("PLAYWORDLE from : " + userInGame.username);
        
        //controllo se il giocatore ha gia giocato per quealla parola
        String tmp = ServerMain.matches_played(userInGame.username);
        
        if(tmp.equals("--PG")){
            out.println("--PG");
            return;//il giocatore ha gia giocato quella parola, restituisco l'errore(--ParolaGiocata)
        }
        
        
        //la parola attuale è diversa da quella da indovinare, aggiorno la streak attuale
        if(!tmp.equals(secretWord)){
            updateStreak();
        }
        secretWord = tmp;
        userInGame.in_playing = true;
        win = false;
        lose = false;
        out.println(secretWord); //invio al client la parola da indovinare 
        }catch(Exception e){
            System.out.println(e);
        }
            
        }
    
    
    
    //metodo per il controllo della parola e la creazione dei suggerimenti
    //prende in input la parola in formato String
    public String wordAnalyzer(String parola_client){
        
        
        char[] ClientWord = parola_client.toCharArray();//converto in array di caratteri la parola del client
        char[] ServerWord = secretWord.toCharArray();//converto in array di caratteri la parola da indovinare
        String indizi = ""; //inzializzo uno stringa vuota che poi conterrà la stringa con gli indizzi da inviare al client
        int i = 0;
        int lettere_indovinate = 0; //variabile che contiene il numero di lettere che sono sia nella parola inviata dal client sia nella parola da indovinare
        
        for(char lettera:ClientWord){ //per ogni lettera controllo se è presente nella parola da indovinare
            if(i > ServerWord.length)break;
            if(lettera == ServerWord[i]){
                lettere_indovinate++;
                indizi += "+"; //se è presente ed è la giusta posizione aggiungo un + alla Stringa "indizi"
            }
            else if(secretWord.contains(Character.toString(lettera))){
                indizi += "?"; //se è presente la lettera all'interno della parola ma non è nella posizione corretta aggiungo un ? alla stringa indizi
            }
            else{
                indizi += "x"; //se la parola non è presente aggiungo una x nella stringa degli indizi
        }
            i++;
        
    }
        //out.println(indizi); //restituisco al client la stringa con gli indizi
        if(lettere_indovinate == 10) return "--WIN"; //se ho indovinato 10 lettere vuol dire che ho indovinato la parola quindi restituisco la stringa "--WIN"
        else return indizi; //altrimenti restituisco la stringa con gli indizi
        
    }
    
   
    //metodo utilizzato per ottenere una parola dal client e controllare se ha sbagliato o ha trovato la parola
    //prende in input un oggetto di tipo Scanner per ottenere i dati inviati dal client, e un oggetto PrintWriter per inoltrarli in uscita al client
    private void sendWord(Scanner in,PrintWriter out){
           
        String parola_client = in.nextLine(); //ottengo la parola dal client
   
        //boolean win = false;
        String tmp = ServerMain.check_matches_played(userInGame.username); //controllo se il giocatore ha gia giocato
        
        if(!tmp.equals("--PG")){ //se ha giocato restituisco l'errore
            out.println("--PC");
            return;//il giocatore ha gia giocato quella parola, restituisco l'errore(--ParolaCambiata)
        }
        
        if(!ServerMain.containsWord(parola_client)){
            out.println("--PNC"); //se la parola non è contenuta nel dizionario restituisco l'errore(--ParolaNonContenuta)
            return;
        }
       
        attempt++;
        
        
        //result dopo la chiamata alla funzione wordAnalyzer conterra "--WIN" in caso di vittoria oppure la stringa con i suggerimenti
        String result = wordAnalyzer(parola_client);
        
        //result_bool è una variabile per distinguere con true o false la vittoria o meno
        //caso di sconfitta
        boolean result_bool = false;
        if(result.equals("--WIN")){
            result_bool = true;
        }
        
        //win è una variabile globale per capire se l'utente durante questa sessione di gioco ha gia vinto
        if(win){
            out.println("--GV"); //il giocatore ha gia vinto questa partita
            return;
        }
         if(lose){
            out.println("--TF"); //il giocatore ha gia vinto questa partita
            return;
        }
        
        //caso di vittoria
        if(result_bool){
            out.println("--WIN"); //inoltro WIN al client per indicare la vittoria
            win = true;
            userInGame.saveMatch(result_bool, attempt);
            ServerMain.saveAndUpdate(userInGame.username);
       
        }
        //altrimenti se i tentativi sono > 12 restituisco all'utente il messaggio --LOSE per indicare che ha perso 
        else if(attempt == 12){
            lose = true;
            userInGame.saveMatch(false, attempt); //l'utente ha finito i tentativi
            ServerMain.saveAndUpdate(userInGame.username);
            userInGame.in_playing = false;
            out.println("--LOSE");
        }
        
        else if(!result_bool){
            out.println(result); //in questo caso la parola non è stata indovinata quindi inoltro gli indizi
        }
        //parola non trovata, nuovo tentativo
        /*
        else if(result == false && attempt < 12){ //controllo per evitare che invi il segnale --TRY anche nel caso di vittoria
            out.println("--TRY");
        }
*/
        
    }
        
    
    
    

    
    
    //override del metodo run della classe Runnable

    @Override
    public void run() {
        try(Scanner in = new Scanner(socket.getInputStream()); PrintWriter out = new PrintWriter(socket.getOutputStream(),true);){
            String cmd;
            System.out.println("New Connection...");
            while(in.hasNextLine()){
             cmd = in.nextLine();  
             if(cmd.equals("--LI")){
                 login(in,out);
                 System.out.println("LOGIN");
             }
             
             else if(cmd.equals("--LO")){
                logout(out);   
                System.out.println("LOGOUT");
             }
             
             else if(cmd.equals("--RU")){
                 register(in,out);
                 System.out.println("REGISTRAZIONE");
                 
             }
             
             else if(cmd.equals("--PW")){
                 playWordle(out);
             }
             
             else if(cmd.equals("--SW")){
                 sendWord(in,out);
             }
             
             else if(cmd.equals("--S")){
                 printStats(out);
             }
             
      
            }
            
            
        }   catch (IOException ex) {
        System.out.println("Errore nella comunicazione col client");
        }
            
        }
                
    }
    
    
  //TODO stoppare server quando il client si chiude



