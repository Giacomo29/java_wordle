/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package wordle_esame;
import java.io.Serializable;


/**
 *
 * @author giacomotorbidoni
 */

/**

Questa classe rappresenta l'utente del gioco e contiene informazioni come username, password,
numero di vittorie totali, numero di partite giocate, serie di vittorie consecutive attuale,
serie migliore di vittorie di seguito, flag log_in per verificare se l'utente è loggato o meno
e flag in_playing per verificare se l'utente sta attualmente giocando. La classe include anche
il metodo saveMatch() che consente di aggiornare i dati della partita attuale dell'utente,
come l'esito della partita e il numero di tentativi impiegati. 
* 
*/



public class Utente implements Serializable{
    public String username;
    private String password;
    public int partite_giocate;
    public int vittorie;
    public int streak;
    public int max_streak;
    public int[] distribution;
    public boolean in_playing;
    public boolean log_in;
    
    
    public Utente(String username, String password) {
       this.username = username;
       this.password = password;
       this.distribution = new int[12];
       this.in_playing = false;
       this.log_in = false;
    }
    
    public Utente(){
        this.in_playing = false;
        this.log_in = false;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    @Override
        public String toString() {
            return "username:" + username + " password:" + password + " partite giocate: " + partite_giocate 
                    + " vittorie:" + vittorie + " streak: " + streak + " max_streak: " 
                    + max_streak + " in_playing: " + in_playing + " log_in: "+ log_in + "\n";
        }
        
        
    public void saveMatch(boolean esito, int attempt){
        
        if(esito == true){
            vittorie++;
            streak++;
            max_streak = Math.max(max_streak, streak);
            partite_giocate++;
            distribution[attempt-1] = distribution[attempt-1] + 1;
        }
        else{
            streak = 0;
            partite_giocate++;
        }

    }
    
    
}


 
    
    


