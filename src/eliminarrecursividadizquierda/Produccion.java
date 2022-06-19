/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eliminarrecursividadizquierda;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author edy
 */
public class Produccion {

    private String simbolo;
    private String valor;

    public Produccion(String s, String v) {
        setSimbolo(s);
        setValor(v);
    }

    public Produccion(String prod) {
        String aux = "";
        for (int i = 0; i < prod.length(); i++) {
            if (prod.charAt(i) != '=') {
                aux = aux + prod.charAt(i);
            } else {
                setSimbolo(aux); //simbolo a la izquierda del signo igual
                aux = "";
            }
        }
        setValor(aux); //volcar valor (despues del igual)
    }

    public boolean esRecursiva() {
        boolean recursiva = false;
        if ((getValor().indexOf(getSimbolo())) >= 0) {
            recursiva = true;
        }
        return recursiva;
    }

    public List<String> dividir() {
        List<String> divisiones = new ArrayList<String>();
        String aux = "";
        for (int i = 0; i < getValor().length(); i++) {
            if (getValor().charAt(i) != '|') {
                aux = aux + getValor().charAt(i);
            } else {
                divisiones.add(aux);
                aux = "";
            }
        }
        divisiones.add(aux); //volcar utlimo valor
        return divisiones;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getValor() {
        return valor;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTerminals() {
        String terminales = "";

        String REGEX = "\'[^\']*\'";
        String INPUT = getValor();

        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(INPUT);
        int count = 0;

        while (m.find()) {
            //count++;
            //System.out.println("Match# " + count);
            //System.out.println("valor(): " + m.group());
            //System.out.println("start(): " + m.start());
            //System.out.println("end(): " + m.end());
            terminales = terminales+m.group()+", ";

        } 
        return terminales.replace("\'","");
    }
    

    public String getNonTerminals() {
        String REGEX = "(\'[^\']*\')|\\|";
        String INPUT = getValor();
        INPUT = INPUT.replaceAll(REGEX, "");
        return INPUT;
    }

    @Override
    public String toString() {
        return getSimbolo() + " -> \t" + getValor();
    }
}
