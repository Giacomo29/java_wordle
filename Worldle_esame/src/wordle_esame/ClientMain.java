/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordle_esame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author giacomotorbidoni
 */
public class ClientMain {
    
    
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
    
    
    
    private static int SERVER_PORT;
    private static String SERVER_ADDRESS;
    private static int TIMEOUT;
    private static int PORT_SHARE;
    private static int PORT_MULTICAST;
    private static String ADDRESS_MULTICAST;
    
    private static Utente user;
    private static boolean game = true;
    private static String parola;
    private static ArrayList<String> notifiche;
    private static int my_port;
    private static boolean exit = false; //variabile per gestire il logout automatico tramite hook
    
    
    
    //metodo per la stampa delle regole di gioco
    private static void regolamento(){
        System.out.println(ANSI_CYAN + "Benvenuto su Wordle, il gioco dell'indovinello!\n\n" +
                   "Il gioco consiste nell'indovinare una parola casuale ogni giorno. Se indovini la parola al primo colpo, complimenti! Altrimenti, ti verranno dati dei suggerimenti:\n" +
                   "- Un segno \"+\" per ogni lettera che è nella parola da indovinare e nella giusta posizione\n" +
                   "- Un segno \"?\" per ogni lettera che è presente nella parola ma in una posizione errata\n" +
                   "- Una \"x\" per ogni lettera sbagliata\n\n" +
                   "Cosa aspetti? Registrati e sfida i tuoi amici condividendo i tuoi risultati!\n\n" +
                   "Ecco alcune opzioni del gioco che potrebbero esserti utili:\n" +
                   "- REGISTRATI[r]: crea un account per giocare e tenere traccia dei tuoi risultati\n" +
                   "- ACCEDI[l]: accedi al tuo account per giocare e vedere le tue statistiche\n" +
                   "- STATISTICHE[s]: accedi al tuo account per giocare e vedere le tue statistiche\n" +
                   "- ESCI DAL GIOCO[q]: esci dal gioco\n\n" +
                   "Buona fortuna e buon divertimento!");
        
    }
    
    
    //metodo per la configurazione iniziale del client
    private static void startClient(){
        ConcurrentHashMap<String,String> serverInfo = new ConcurrentHashMap<>(); 
        serverInfo = GestoreFile.clientDetail();
        notifiche = new ArrayList<>();
        
        SERVER_PORT = Integer.parseInt(serverInfo.get("SERVER_PORT"));
        SERVER_ADDRESS = serverInfo.get("SERVER_ADDRESS");
        TIMEOUT = Integer.parseInt(serverInfo.get("TIMEOUT"));
        PORT_SHARE = Integer.parseInt(serverInfo.get("PORT_SHARE")); 
        PORT_MULTICAST = Integer.parseInt(serverInfo.get("PORT_MULTICAST"));
        ADDRESS_MULTICAST = serverInfo.get("ADDRESS_MULTICAST");
        user = new Utente();
        
        System.out.println(ANSI_RED+"  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------. ");
        System.out.println(ANSI_RED+" | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |");
        System.out.println(ANSI_RED+" | | _____  _____ | || |     ____     | || |  _______     | || |  ________    | || |   _____      | || |  _________   | |");
        System.out.println(ANSI_RED+" | ||_   _||_   _|| || |   .'    `.   | || | |_   __ \\    | || | |_   ___ `.  | || |  |_   _|     | || | |_   ___  |  | |");
        System.out.println(ANSI_RED+" | |  | | /\\ | |  | || |  /  .--.  \\  | || |   | |__) |   | || |   | |   `. \\ | || |    | |       | || |   | |_  \\_|  | |");
        System.out.println(ANSI_RED+" | |  | |/  \\| |  | || |  | |    | |  | || |   |  __ /    | || |   | |    | | | || |    | |   _   | || |   |  _|  _   | |");
        System.out.println(ANSI_RED+" | |  |   /\\   |  | || |  \\  `--'  /  | || |  _| |  \\ \\_  | || |  _| |___.' / | || |   _| |__/ |  | || |  _| |___/ |  | |");
        System.out.println(ANSI_RED+" | |  |__/  \\__|  | || |   `.____.'   | || | |____| |___| | || | |________.'  | || |  |________|  | || | |_________|  | |");
        System.out.println(ANSI_RED+" | |              | || |              | || |              | || |              | || |              | || |              | |");
        System.out.println(ANSI_RED+" '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |");
        System.out.println(ANSI_RED+"  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------' ");
        System.out.println("\n");
        System.out.println(ANSI_YELLOW +"Benvenuto a Worlde!\n");
        System.out.println(ANSI_YELLOW +"[r] - Registrati!\n");
        System.out.println(ANSI_YELLOW +"[l] - Login!\n");
        System.out.println(ANSI_YELLOW +"[?] - Regole di gioco!\n");
        System.out.println(ANSI_YELLOW +"[q] - Esci dal gioco!\n");
        
    }
    
    //metodo per la stampa di un piccolo menu di gioco
    private static void menu(){
            System.out.println(ANSI_YELLOW +"[p] - Nuova partita!\n");
            System.out.println(ANSI_YELLOW +"[s] - Statistiche!\n");
            System.out.println(ANSI_YELLOW +"[c] - Classifiche!\n");
            System.out.println("\n");
            System.out.println(ANSI_YELLOW +"[q] - Log-out!\n");
    }
    
    
    //metodo per il login
    //prende come parametri un oggetto PrintWriter "out", uno Scanner "in" che conterrà le risposte
    //del server un uno Scanner per ottenere i comandi da tastiera e il riferimento al thread per la gestione dei messaggi
    private static void login(PrintWriter out, Scanner in,Scanner cmd,ExecutorService threadMessaggi){
        
        if(user.log_in){ //se l'utente ha gia effettuato il login lo segnalo e scrivo chi è attualmente connesso
            System.out.println(ANSI_RED + "Hai gia effettuato il login come "+ user.username +" !\n" + ANSI_RESET);
            return;
        }
        //invio il carattere speciale per segnalare al task nel server che voglio effettuare il login
        out.println("--LI");

        System.out.println(ANSI_YELLOW + " -- LOGIN -- \n");
        System.out.println(ANSI_YELLOW + "Inserisci l'username!\n");
        String username = cmd.nextLine();

        out.println(username); //inoltro al server lo username

        System.out.println(ANSI_YELLOW + "Inserisci la password!\n");
        String password = cmd.nextLine();
        out.println(password); //inoltro al server la password
 
        String res = in.nextLine(); //prendo la risposta del server
        

        
        if(res.equals("--LE")){ //in caso avessi gia effettuato il login lato server mi viene segnalato e lo riporto al client
            System.out.println(ANSI_RED + "Hai gia effettuato il login!\n" + ANSI_RESET);
            return;
        }
        
        else if(res.equals("--PE")){ //caso password errata
            System.out.println(ANSI_RED + "Password Errata, riprova!\n" + ANSI_RESET);
            return; 
        }
        
        else if(res.equals("--UE")){ //caso in cui non esista nessuno account con quell'username
            System.out.println(ANSI_RED + "Nome utente errato o inesistente!\n" + ANSI_RESET);
            return;
        }
        
        else if(res.equals("--OK")){ //il login ha avuto successo, creo un oggetto user con quelle credenziali
            System.out.println(ANSI_BLUE + "Login efettuato, Benvenuto "+ username +"!\n" + ANSI_RESET);
            user = new Utente(username,password);
            threadMessaggi.submit(ClientMain::addNotification); //faccio partire il thread responsabile della ricezione dei messaggi in multicast
            user.log_in = true; //setto a true il login dell'utente
            
            menu(); //chiamo le stringhe per il menu
            return;
        }
        
    }
    
    
    //metodo per richiedere al server la registrazione di un nuovo utente
    //prende come parametri un oggetto PrintWriter "out", uno Scanner "in" che conterrà le risposte del server un uno Scanner per ottenere i comandi da tastiera
    private static void register(PrintWriter out, Scanner in,Scanner cmd){
        
        if(user.log_in == true){ //se sono loggato restituisco un errore
            System.out.println(ANSI_RED + "Hai gia effettuato il login come "+ user.username +"!\n" + ANSI_RESET);
        }
        out.println("--RU"); //indico al server che intendo eseguire una nuova registrazione
        System.out.println(ANSI_YELLOW + " -- REGISTRATI ORA! -- \n");
        System.out.println(ANSI_YELLOW + "Inserisci l'username!\n");
        String username = cmd.nextLine(); //ottengo lo username da tastiera
        out.println(username);//inoltro al server
        System.out.println(ANSI_YELLOW + "Inserisci la password!\n");
        String password = cmd.nextLine(); //ottengo la password da tastiera
        
        out.println(password);//inoltro al server
        String res = in.nextLine();//salvo la risposta del server
         
        
        if(res.equals("--AE")){ //caso account gia esistente
            System.out.println(ANSI_RED + "Nome utente gia in uso!\n" + ANSI_RESET);
            return;
        }
        
        else if(res.equals("--EP")){ //caso di errore della password
            System.out.println(ANSI_RED + "La passowrd non può essere vuota!\n" + ANSI_RESET);
            return; 
        }
        
        else if(res.equals("--AC")){ // altrimenti viene creato con successo l'account
            System.out.println(ANSI_RED + "Account Creato con successo!\n" + ANSI_RESET);
            return;
        }
        
    }
    
     //funzione per eseguire il logout di un utente
    //ottiene come parametro un oggetto PrintWriter "out" e uno Scanner "in"
    private static void logout(PrintWriter out, Scanner in){
        
        
        
        out.println("--LO"); //indico al server che intendo esegurire un logout
        System.out.println(ANSI_YELLOW + " -- LOGOUT -- \n");
        String tmp = in.nextLine(); //ottengo la risposta dal server
        
        if(tmp.equals("--DE")){ //caos in cui il login è andato a buon fine, avverto l'utente
            System.out.println(ANSI_BLUE + "Disconnessione eseguita!\n" + ANSI_RESET);
            game = false;
        }
        
    }
    
    
    //metodo per aggiungere nella lista dei messaggi i nuovi messaggi. E' il metodo che sarà in running su un tgread separato
    private static void addNotification(){
        try(MulticastSocket ms = new MulticastSocket(PORT_MULTICAST);){
            ms.setSoTimeout(TIMEOUT);
            InetAddress group = InetAddress.getByName(ADDRESS_MULTICAST);
            ms.joinGroup(group); //mi unisco al gruppo multicast
            byte[] buf = new byte[1024]; //creo un buffer di 1024 byte
            DatagramPacket p = new DatagramPacket(buf, buf.length); //creo il pacchetto 
            while(game){ //sarà in funzione finche il client non uscendo dalla partita setterà a false "game"
                try{
                    ms.receive(p); //attende un nuovo pacchetto
                }catch(SocketTimeoutException ex) {continue;}
                
                String msg = new String(p.getData());
                msg = msg.trim(); //rimuovo gli spazi bianchi dal messaggio
                String[] data = msg.split("/"); //carattere per dividere in due la stringa tra le info del mittente e le statistiche vere e proprie
                if((Integer.parseInt(data[0]) != my_port)) notifiche.add(data[1]); //aggiungo alla lista il messaggio
            }
            }
        catch(IOException ex){
            System.out.println("ERRORE: notifiche");
        }
    }
    
    
    
    //metodo per la condivisione di notifiche. Mi permette, passata una stringa di inotrarla al gruppo multicast
    private static void share(String stat){ 
        System.out.println("Condivisione in corso...");
        try(DatagramSocket s = new DatagramSocket();){
            my_port = s.getLocalPort(); //ottiene la porta del client
            InetAddress server = InetAddress.getByName(SERVER_ADDRESS);//indirizzo del server multicast
            DatagramPacket request = new DatagramPacket(stat.getBytes(),stat.getBytes().length,server,PORT_SHARE);
            s.send(request);
            System.out.println("Hai condiviso le tue statistiche!");
        }
        catch(IOException ex){
            System.out.println("ERRORE: condivisione risultati");
        }
        
    }
    
    
    //metodo per la stampa delle notifiche all'interno dell'array notifiche
    private static void showMeSharing(){
        
        System.out.println("------ Hai "+ notifiche.size() +" messaggi non letti! ------");
        for(String message:notifiche){
        System.out.println("\n"); //per ogni elemento dell'array stampo la stringa in maniera formattata per renderlo facilmente comprensibile
        System.out.println(BeautifyStat(message)); 
        }
        //una volta letti i messaggi svuoto la lista
        notifiche.clear();
    }
    
    
    
    //Metodo per indicare al server che si intende inziare una nuova partita
    private static void playWORDLE(PrintWriter out, Scanner in,Scanner cmd){
        out.println("--PW"); //indico al server che voglio inizare una partita
        String res = in.nextLine(); //ottengo il risultato
        if(res.equals("--EL")){ //caso in cui non ci sono giocatori loggati (controllo in piu rispetto a quello presente nel main)
            System.out.println(ANSI_RED+ "Per giocare devi effettuare il login! -> [l]\n");
        }
        
        else if(res.equals("--PG")){ //caso in cui l'utente ha gia giocato la partita
            System.out.println(ANSI_RED + "Hai gia giocato la parola di oggi! Riprova tra qualche ora!");
        }
        else{ //altrimenti inizio la partita
            System.out.println(ANSI_PURPLE + "Partita in corso!\n");
            System.out.println(ANSI_BLACK+"-------------------------------------\n");
            System.out.println(ANSI_YELLOW+"[s] e poi invia la parola e prova ad indovinare\n");
            System.out.println(ANSI_YELLOW + "[o] per disconnetterti! (verrà considerata come una sconfitta)");
            System.out.println(ANSI_BLACK+"-------------------------------------\n");
            System.out.println(ANSI_PURPLE+"Ricorda che hai solo 12 tentativi!\n");
            parola = res; //ottengo dal server anche la parola -> non necessaria, utilizzata per debug principalmente
            //System.out.println("PAROLA: " + parola);
            res = cmd.nextLine(); //aspetto l'input dell'utente per la scelta che vuole fare
            while(!res.equals("o")){
                switch(res){
                    case "s": //caso in cui si prema s -> si intende inviare una parola al server
                        System.out.println(ANSI_PURPLE+"->");
                        res = cmd.nextLine();
                        System.out.println("parola->" + res);
                        sendWord(out,in,res); //chiamo la funzione sendWord() e ritorno il risultato, poi torno a porre la scelta all'utente
                        System.out.println(ANSI_PURPLE+"[s] -> riprova");
                        System.out.println(ANSI_PURPLE+"[#] -> condividi risultati");
                        System.out.println(ANSI_PURPLE+"[o] -> esci dalla partita");
                        res = cmd.nextLine();
                        break;
                
                    case "#": //caso in cui si intende condividere i risultati 
                        String stat = getStat(out,in); //ottendo le statistiche dell'utente attuale
                        share(stat); //condivido le statistiche tramite la funzione share passandogli una stringa con le statistiche
                        //break;
                    
                    default: //altrimenti non ha messo un comando valido
                        System.out.println("-----------------------");
                        System.out.println(ANSI_PURPLE+"Immetti:\nscrivi parola: [s]\ncondividi: [#]\nesci: [o] ");
                        System.out.println("-----------------------");
                        res = cmd.nextLine();
            }
        }
            System.out.println("Uscita dalla partita...\n"); 
            menu();
        }
    }
    
    
    //metodo per l'invio di una parola al server
    private static void sendWord(PrintWriter out, Scanner in, String cmd){
        out.println("--SW"); //indico al server che intendo inviare una parola
        out.println(cmd); //mando la parola al server

        String risp = in.nextLine();// prendo la risposta
        if(risp.equals("--PC")){ //caso in cui si cerchi di inltrare la parola ma nel frattempo è cambiata parola, bisogna inziare una nuova partita
            System.out.println(ANSI_PURPLE + "La parola è cambiata, inzia una nuova partita!");
            return;
        }
        if(risp.equals("--PNC")){ //caso in cui la parola che abbiamo scritto non sia prensente nel dizionario delle parole
            System.out.println( ANSI_RED + "Non ci siamo! la parola non è neanche presente nel vocabolario... :( (tentativo non contato)\n");
            return;
        }
        
        if(risp.equals("--GV")){ //caso in cui l'utente ha gia vinto la partita
            System.out.println(ANSI_YELLOW + "Hai gia indovinato questa parola, aspetta la prossima!");
            return;
        }
        
        if(risp.equals("--TF")){ //caso in cui 'utente ha finito i tentativi
            System.out.println(ANSI_YELLOW + "Hai finito i tentativi per questa parola, aspetta la prossima!");
            return;
        }
        
        if(risp.equals("--LOSE")){ //caso in cui l'utente ha perso
            System.out.println(ANSI_RED + "Accidenti hai finito i tentativi, riprova con la prossima parola!");
            return;
        }
        
        
        if(risp.equals("--WIN")){ //caso in cui l'utente ha indovinato la parola
            System.out.println(ANSI_BLUE + "Complimenti! Hai indovinato la parola di oggi!");
            return;
        }
        
        else{ //caso in cui l'utente non ha indovinato la parola ma questa è ne vocabolario. Stampo i suggerimenti
         System.out.println( ANSI_YELLOW + "Ci sei andato vicino, eccoti qualche indizio:\n");
         System.out.println(risp); //stampo i suggerimenti
        }
        
    }
    
    //metodo per ottenere le statistiche dell'utente loggato
    //prende in input un oggetto PrintWriter "out" e Scanner "in"
    private static String getStat(PrintWriter out, Scanner in){
        out.println("--S"); //invio al server la richiesta
        return in.nextLine(); //ritorno la risposta del server
    }
    
    
    
    //metodo per formattare le statistiche di un utente che il server ha inviato
    private static String BeautifyStat(String stat){
            
        String[] titles = {"Utente","Partite giocate", "Vittorie", "Streak", "Max streak", "Distribuzione"}; // Array di titoli dei dati
        String[] values = stat.split("-");

        StringBuilder output = new StringBuilder(); // Stringa di output

        // Aggiungi i titoli e i valori alla stringa di output formattata
        output.append(ANSI_GREEN + "----STATISTICHE----\n");
        for(int i = 0; i < titles.length; i++) {
            output.append(titles[i]).append(ANSI_GREEN +": ").append(values[i+1]).append("\n");
        }
        output.append(ANSI_GREEN +"--------------------------");
        return output.toString(); // ritorna la stringa di output
         
        
    }
    
    
    
    //main
    
     public static void main(String[] args) throws IOException{
        String cmd;
        //avvio la configurazione del client
        startClient();
        
        //istanzio thread per la gestione dei messsaggi
        ExecutorService threadMessaggi = Executors.newSingleThreadExecutor();
        Runtime runtime = Runtime.getRuntime();

      
  
        //provo a stabilire una connessione con il server
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            Scanner command = new Scanner(System.in); //comando per ottenere l'input da tastiera dell'utente
            )
        {
            
            // Registra un hook di shutdown
            //Shutdown Hook: questo hook viene eseguito prima che l'applicazione venga terminata, ad esempio quando
            //viene chiamato il metodo System.exit() o quando il sistema operativo riceve un segnale di interruzione come
            //SIGTERM o CTRL+C. 
            runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Esegui la funzione logout
                if(user.log_in && !exit){ //se sono loggato e l'utente non si era disconnesso
                    logout(out,in);//eseguo il logout
                }
            }
            });
            
            while(game){ //finche game == true
                cmd = command.nextLine(); //aspetto un comando dall'utente
                
                switch(cmd){
                    
                    case "r": //caso r -> registrazione
                        register(out,in,command);
                        break;
                    
                    case "l": // caso l -> login
                        login(out,in,command,threadMessaggi);
                        break;
                        
                        
                    case "?": //caso ? -> regolamento
                        regolamento();
                        break;
                    
                        
                    case "p"://caso -> inizia una partita
                        if(user.log_in) { //inzio una partita solo se c'è un utente connesso
                            playWORDLE(out, in, command); 
                        } else {
                            System.out.println("Devi effettuare il login per giocare.");
                        }
                        break;

                    case "s"://caso s -> stampo le statistiche 
                        if (user.log_in) {
                            String stat = getStat(out, in);
                            System.out.println(BeautifyStat(stat)); //rendo leggibili le statistiche
                        } else {
                            System.out.println("Devi effettuare il login per vedere le statistiche.");
                        }
                        break;

                    case "c": //caso c -> l'utente ha richiesto la lista delle notifiche
                        if (user.log_in) { //deve essere loggato l'utente
                            showMeSharing(); // stampo la lista con tutte le notifiche aggiunge in parallelo dal threadMessaggi
                        } else {
                            System.out.println("Devi effettuare il login per vedere le condivisioni.");
                        }
                        break;
                        
                    case "q": //caso q -> l'utente ha richiesto la disconnessione
                        if(user.log_in){
                            logout(out,in);
                            exit = true; //setto che l'utente ha eseguito la disconnessione                         
                        }
                        game = false; //setto a false game che terminerà il programma
                        break;
                }
                
            } //terminazione del threadMessaggi
            threadMessaggi.shutdown();
             try {
            threadMessaggi.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
            System.err.println(ex);
            }
            
            
        }
        catch(Exception ex){
            System.out.println("ERRORE AVVIAMENTO CLIENT");
        }
     }
    
}
