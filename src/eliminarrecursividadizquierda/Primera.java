/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eliminarrecursividadizquierda;

/**
 *
 * @author edy
 */
public class Primera {
    private String simbolo;
    private String valor;
    
    public Primera(String s, String v){
        this.simbolo = s;
        this.valor = v;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getValor() {
        return valor;
    }
    
    @Override
    public String toString() {
        return "P(" + getSimbolo() + ") -> \t{" + getValor() + "}";
    }
}
