/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package eliminarrecursividadizquierda;

/**
 *
 * @author edy dubon
 */

        /*Esta clase es para pruebas (Test)*/
public class EliminarRecursividadIzquierda {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Produccion p1 = new Produccion("S", "'r'|S'q'");
        System.out.println(p1.esRecursiva());
        
        /*for (String s: p1.dividir()){
            System.out.println(s);
        }*/
        
        System.out.println("Terminales: "+p1.getTerminals());
        System.out.println("NonTerminal: "+p1.getNonTerminals());
    }
    
}
