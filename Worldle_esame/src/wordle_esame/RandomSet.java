/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordle_esame;

import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author giacomotorbidoni
 */


/**

Come scelta di implementazione, è stata creata una nuova classe per gestire il vocabolario di parole da
indovinare utilizzando un HashSet come struttura dati, modificata per aggiungere un metodo per il sorteggio
casuale di una parola dall'insieme.
Uno dei metodi basilari della classe è getRandom(), che ha la funzione di convertire l'HashSet in un array,
generare un indice casuale tramite la funzione random, e restituire una parola casuale dall'array, opportunamente
convertito.
Questa scelta di implementazione consente una gestione efficiente e casuale del vocabolario di parole da indovinare.

*/





//Struttura dati per il mantenimemto e il pescaggio in maniera random delle parole
//la struttura dati è un HashSet con implementato la possibilità di estrarre in maniera randomica una parola dal suo interno
public class RandomSet<E> {
    private HashSet<String> set;
    private Random random;

    public RandomSet() {
        this.set = new HashSet<>();
        this.random = new Random();
    }

    
    //aggiunge un elemento di tipo String alla struttura dati
    public void add(String s) {
        set.add(s);
    }

    
    //metodo per ottenere in maniera random un elemento dalla struttura dati
    public String getRandom() {
        Object[] array = set.toArray();
        int randomIndex = random.nextInt(array.length);
        return (String) array[randomIndex];
    }
    
    
    //medoto per controllare se un dato elemento è presente nella struttura dati
    public boolean contains(E element){
        return set.contains(element);
    }
    
    //metodo per la stampa dell'HashSet
    public void printAll(){
        for(String s : set){
        System.out.println(s);
        }
    }
    
}