/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package eliminarrecursividadizquierda;

import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author edy
 */
public class Principal extends javax.swing.JFrame {

    private List<Produccion> producciones = new ArrayList<>();
    private List<Produccion> producciones_sin_recur = new ArrayList<>();
    private List<Produccion> producciones_no_elim = new ArrayList<>();
    private List<Produccion> prod_factorizadas = new ArrayList<>();
    private List<Primera> primeras = new ArrayList<>();
    private HashMap<String, String> hashPrimeras = new HashMap<>();
    private HashMap<String, String> producciones_no_elim_factorizadas = new HashMap<>();
    private HashMap<String, Integer> terminales = new HashMap<>();
    private int no_terminal_aux = 1;
    private int _terminal_aux = 1;

    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
        setTitle("Eliminación de Recursividad por la Izquierda");

        txt_entrada.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        System.out.println("Archivo recibido desde Drag and Drop");
                        System.out.println(file.getAbsolutePath());
                        lbl_ruta_archivo.setText(file.getAbsolutePath());

                        try {
                            String ST = new String(Files.readAllBytes(file.toPath()));
                            txt_entrada.setText(ST);
                            limpiarTextAreas();

                            /*Regex simbolos terminales*/
                            String REGEX = "\'[^\']*\'";
                            String INPUT = ST;

                            Pattern p = Pattern.compile(REGEX);
                            Matcher m = p.matcher(INPUT);
                            int count = 0;

                            while (m.find()) {
                                terminales.put(m.group(), 1);
                            }

                            /*mostrar simbolos terminales*/
                            Iterator it_terminales = terminales.entrySet().iterator();
                            while (it_terminales.hasNext()) {
                                Map.Entry<String, Long> ele = (Map.Entry) it_terminales.next();
                                txt_terminales.append(ele.getKey() + "\n");
                                txt_terminales1.append(ele.getKey() + "\n");
                            }

                            //leer line a linea el archivo y crear las producciones.
                            //añadir los no terminales en GUI
                            for (String s : Files.readAllLines(file.toPath())) {
                                Produccion aux_prod = new Produccion(s);

                                //Agregar los no terminales.
                                if (txt_no_terminales.getText().indexOf(aux_prod.getSimbolo()) < 0) {
                                    txt_no_terminales.append(aux_prod.getSimbolo() + "\n");
                                    txt_no_terminales1.append(aux_prod.getSimbolo() + "\n");
                                }
                                for (String pe : (aux_prod.dividir())) {
                                    producciones.add(new Produccion(aux_prod.getSimbolo(), (pe)));
                                }
                            }

                            producciones_no_elim = new ArrayList<Produccion>(producciones);

                            //agregar producciones orginales
                            for (Produccion s : producciones) {
                                txt_producciones_ext.append(s.toString() + "\n");
                                txt_esRecursiva.append(String.valueOf(s.esRecursiva()) + "\n");
                            }

                            //agregar producciones no recursivas a txt_producciones1
                            for (Produccion s : producciones) {
                                if (s.esRecursiva()) {
                                    //si es recursiva, se debe eliminar la recursividad en la produccion
                                    Produccion noRecursiva = encuentraNoRecursivaBySimbolo(producciones, s);
                                    if (noRecursiva != null) {
                                        //agregar nuevo Simbolo no terminal S_1
                                        txt_no_terminales1.append(s.getSimbolo() + "_1\n");

                                        Produccion nuevaProd1 = new Produccion(s.getSimbolo(), noRecursiva.getValor() + noRecursiva.getSimbolo() + "_1");
                                        txt_producciones_ext1.append(nuevaProd1 + "\n");
                                        producciones_sin_recur.add(nuevaProd1);

                                        Produccion nuevaProd2 = new Produccion(noRecursiva.getSimbolo() + "_1", s.getValor().replace(s.getSimbolo(), "") + noRecursiva.getSimbolo() + "_1");
                                        txt_producciones_ext1.append(nuevaProd2 + "\n");
                                        //producciones_sin_recur.add(nuevaProd2);

                                        Produccion nuevaProd3 = new Produccion(noRecursiva.getSimbolo() + "_1", "\'ε\'");
                                        txt_producciones_ext1.append(nuevaProd3 + "\n");
                                        //producciones_sin_recur.add(nuevaProd3);

                                        producciones_sin_recur.add(new Produccion(noRecursiva.getSimbolo() + "_1", nuevaProd2.getValor() + "|" + nuevaProd3.getValor()));

                                        producciones_no_elim.remove(noRecursiva);
                                        producciones_no_elim.remove(s);
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Error: \n No se encontró reglas con las \n cuales se pueda eliminar esta recursividad\n" + s.toString(), "Error", HEIGHT);
                                    }
                                    if (txt_terminales1.getText().indexOf('ε') < 0) {
                                        txt_terminales1.append("\'ε\'\n");
                                    }
                                }
                            }

                            for (Produccion s : producciones_no_elim) {
                                //añadir las producciones originales que no se usaron para eliminar recursividad
                                txt_producciones_ext1.append(s.toString() + "\n");
                                producciones_sin_recur.add(s);
                            }

                            //lista de producciones sin recursividad factorizadas *un no terminal por cada produccion
                            //List<Produccion> producciones_no_elim_factorizadas = new ArrayList<Produccion>();
                            //factorizar no eliminadas en HashMap
                            for (Produccion prod : producciones_sin_recur) {
                                System.out.println(prod.toString());
                                String aux_valor_actual_enHash = producciones_no_elim_factorizadas.get(prod.getSimbolo());
                                if (aux_valor_actual_enHash != null) {
                                    producciones_no_elim_factorizadas.put(prod.getSimbolo(), aux_valor_actual_enHash + "|" + prod.getValor());
                                } else {
                                    producciones_no_elim_factorizadas.put(prod.getSimbolo(), prod.getValor());
                                }
                            }

                            Iterator it_factorizados2 = producciones_no_elim_factorizadas.entrySet().iterator();
                            while (it_factorizados2.hasNext()) {
                                Map.Entry<String, String> ele = (Map.Entry) it_factorizados2.next();
                                prod_factorizadas.add(new Produccion(ele.getKey(), ele.getValue()));
                            }
                            System.out.println("prod_factorizadas:" + prod_factorizadas);

                            //CONSTRUIR FUNCION PRIMERA
                            funcion_primera();

                            //hash map prod no elim factorizadas.
                            System.out.println("****Factorizadas*****************");
                            System.out.println(producciones_no_elim_factorizadas);
                            Iterator it_factorizados = producciones_no_elim_factorizadas.entrySet().iterator();
                            while (it_factorizados.hasNext()) {
                                Map.Entry<String, String> ele = (Map.Entry) it_factorizados.next();
                                System.out.println(ele.getKey() + " -> " + ele.getValue());
                            }
                            System.out.println("*********************************");

                            //consola:  Ver producciones (listas)
                            System.out.println("****producciones***************");
                            for (Produccion prod : producciones) {
                                System.out.println(prod.toString());
                            }
                            System.out.println("*********************************");

                            System.out.println("****producciones_no_elim*********");
                            for (Produccion prod : producciones_no_elim) {
                                System.out.println(prod.toString());
                            }
                            System.out.println("*********************************");

                            System.out.println("****producciones_no_recur*******");
                            for (Produccion prod : producciones_sin_recur) {
                                System.out.println(prod.toString());
                            }
                            System.out.println("*********************************");

                            System.out.println("****Primeras *******************");
                            for (Primera prim : primeras) {
                                System.out.println(prim.toString());
                            }
                            System.out.println("*********************************");

                            /*TABLA DE SIMBOLOS----------------------------------------------*/
                            String[][] dataTabla = new String[producciones_no_elim_factorizadas.size() + 1][terminales.size() + 2];

                            //Non Terminals ->TABLA
                            Iterator it_factorizados3 = producciones_no_elim_factorizadas.entrySet().iterator();
                            while (it_factorizados3.hasNext()) {
                                Map.Entry<String, String> element = (Map.Entry) it_factorizados3.next();
                                dataTabla[no_terminal_aux][0] = element.getKey();
                                no_terminal_aux++;
                            }

                            //terminals -> TABLA
                            Iterator it_terminales2 = terminales.entrySet().iterator();
                            while (it_terminales2.hasNext()) {
                                Map.Entry<String, Long> element = (Map.Entry) it_terminales2.next();
                                dataTabla[0][_terminal_aux] = element.getKey();
                                _terminal_aux++;
                            }
                            dataTabla[0][_terminal_aux] = "ε";
//                              dataTabla[0][_terminal_aux] = "$";

                            //LLenar tabla de simbolos
                            //Primeas ->TABLA
                            hashPrimeras = ListPrimerastoHashMap(primeras);
                            for (int i = 1; i < dataTabla.length; i++) {
                                for (int j = 1; j < dataTabla[0].length; j++) {
                                    String c = dataTabla[0][j].replace("'", ""); //columna 
                                    String f = dataTabla[i][0]; //fila
                                    System.out.println(hashPrimeras);
                                    System.out.println("C:" + c + "F:" + f);
                                    System.out.println("hasPrimeras.get(f)" + hashPrimeras.get(f));
                                    if (hashPrimeras.get(f).indexOf(c) >= 0) {
                                        System.out.println("coincidencia:");
                                        System.out.println("");
                                        dataTabla[i][j] = f + "->"
                                                + producciones_no_elim_factorizadas.get(f);

                                    }
                                }
                            }

                            printFtoTxtArea(txt_tabla_simbolos, dataTabla);

                        } catch (FileNotFoundException ex) {
                            System.err.println(ex.getMessage());
                        } catch (IOException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    public void printFtoTxtArea(javax.swing.JTextArea txtA, String[][] data) {
        String leftAlignFormat = " %-20s |";

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                String celda;
                if (j == 0) {
                    celda = String.format(" %-5s |", (data[i][j] != null) ? data[i][j] : "");
                } else {
                    celda = String.format(leftAlignFormat, (data[i][j] != null) ? data[i][j] : "");
                }
                txtA.append(celda);
            }
            txtA.append("\n");
            for (int k = 0; k < data[0].length; k++) {
                String celda;
                if (k == 0) {
                    celda = String.format(" %-5s +", "-----");
                } else {
                    celda = String.format(" %-20s +", "--------------------");
                }
                txtA.append(celda);
            }
            txtA.append("\n");
        }
    }

    public HashMap<String, String> ListPrimerastoHashMap(List<Primera> arr) {
        HashMap<String, String> mapa = new HashMap<>();
        for (Primera prim : arr) {
            mapa.put(prim.getSimbolo(), prim.getValor());
        }
        return mapa;
    }

    public void recu_funcion_primera(String key, String simb_original) {
        Produccion aux_produccion = new Produccion(key, producciones_no_elim_factorizadas.get(key));
        System.out.println("recu_funcione_primera: aux_prod: " + aux_produccion);
        System.out.println("recu_funcione_primera: aux_prod.getTerminasl: " + aux_produccion.getTerminals());
        System.out.println("recu_funcione_primera: aux_prod.getNonTerminasl: " + aux_produccion.getNonTerminals());
        String aux_terminales = aux_produccion.getTerminals();
        if (!(aux_terminales.equals(""))) {
            //la produccion tiene terminales, 
            //txt_funcion_primera.append(producciones_no_elim_factorizadas.get(key) + "}\n");
            txt_funcion_primera.append(aux_produccion.getTerminals() + "}\n");
            primeras.add(new Primera(simb_original, aux_produccion.getTerminals())); //registrar "primeras"

        } else {
            //la produccion no tiene terminales,
            System.out.println("aux_produccion.getNonTerminals() :: " + aux_produccion.getNonTerminals());
            recu_funcion_primera(String.valueOf(aux_produccion.getNonTerminals().charAt(0)), simb_original);
        }
    }

    public void funcion_primera() {
        for (Produccion prod : prod_factorizadas) {
            txt_funcion_primera.append("P(" + prod.getSimbolo() + ") -> \t{");
            if (!(prod.getTerminals().equals(""))) {
                //la produccion tiene terminales, 
                txt_funcion_primera.append(prod.getTerminals() + "}\n");
                primeras.add(new Primera(prod.getSimbolo(), prod.getTerminals())); //registrar "primeras"
            } else {
                //la produccion no tiene terminales, se debe buscar recursivamente hasta llegar a una con terminales derivada
                recu_funcion_primera(String.valueOf(prod.getNonTerminals().charAt(0)), prod.getSimbolo());
            }
        }

    }

    public Produccion encuentraNoRecursivaBySimbolo(List<Produccion> lista_producciones, Produccion p) {
        Produccion noRecursiva = null;
        for (Produccion prod_actual : lista_producciones) {
            if ((prod_actual.getSimbolo().equals(p.getSimbolo())) && !(prod_actual.esRecursiva())) {
                noRecursiva = prod_actual;
            }
        }
        return noRecursiva;
    }

    public void limpiarTextAreas() {

        txt_esRecursiva.setText("");
        txt_no_terminales.setText("");
        txt_producciones_ext.setText("");
        txt_terminales.setText("");
        txt_no_terminales1.setText("");
        txt_producciones_ext1.setText("");
        txt_terminales1.setText("");
        txt_funcion_primera.setText("");
        txt_funcion_segunda.setText("");
        txt_tabla_simbolos.setText("");

        producciones.clear();
        prod_factorizadas.clear();
        producciones_no_elim.clear();
        producciones_sin_recur.clear();
        producciones_no_elim_factorizadas.clear();
        hashPrimeras.clear();
        primeras.clear();

        no_terminal_aux = 1;
        _terminal_aux = 1;
        terminales.clear();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txt_entrada = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        txt_esRecursiva = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txt_terminales = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txt_producciones_ext = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        txt_no_terminales = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        txt_terminales1 = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        txt_producciones_ext1 = new javax.swing.JTextArea();
        jScrollPane9 = new javax.swing.JScrollPane();
        txt_no_terminales1 = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lbl_ruta_archivo = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        txt_funcion_primera = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        txt_funcion_segunda = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txt_tabla_simbolos = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txt_entrada.setColumns(20);
        txt_entrada.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        txt_entrada.setRows(5);
        jScrollPane1.setViewportView(txt_entrada);

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel5.setText("Eliminación de Recursividad por la Izquierda");

        jLabel6.setText("Arrastra acá el archivo...");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Con recursividad", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 3, 15))); // NOI18N

        txt_esRecursiva.setColumns(20);
        txt_esRecursiva.setRows(5);
        jScrollPane5.setViewportView(txt_esRecursiva);

        txt_terminales.setColumns(20);
        txt_terminales.setRows(5);
        jScrollPane2.setViewportView(txt_terminales);

        jLabel3.setText("Recursiva?");

        txt_producciones_ext.setColumns(20);
        txt_producciones_ext.setRows(5);
        jScrollPane3.setViewportView(txt_producciones_ext);

        jScrollPane4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        txt_no_terminales.setColumns(20);
        txt_no_terminales.setRows(5);
        txt_no_terminales.setToolTipText("");
        jScrollPane4.setViewportView(txt_no_terminales);

        jLabel1.setText("V");

        jLabel2.setText("T");

        jLabel4.setText("P");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jLabel2)
                        .addGap(104, 104, 104)
                        .addComponent(jLabel4))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(56, 56, 56))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sin recursividad", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 3, 15))); // NOI18N

        txt_terminales1.setColumns(20);
        txt_terminales1.setRows(5);
        jScrollPane7.setViewportView(txt_terminales1);

        txt_producciones_ext1.setColumns(20);
        txt_producciones_ext1.setRows(5);
        jScrollPane8.setViewportView(txt_producciones_ext1);

        txt_no_terminales1.setColumns(20);
        txt_no_terminales1.setRows(5);
        jScrollPane9.setViewportView(txt_no_terminales1);

        jLabel8.setText("V");

        jLabel9.setText("T");

        jLabel10.setText("P");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jLabel9)
                        .addGap(100, 100, 100)
                        .addComponent(jLabel10))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(129, 129, 129))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbl_ruta_archivo.setBackground(new java.awt.Color(204, 204, 204));
        lbl_ruta_archivo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ruta Archivo", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 0, 12))); // NOI18N

        jButton1.setText("Limpiar todo");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Función Primera", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 3, 15))); // NOI18N

        txt_funcion_primera.setColumns(20);
        txt_funcion_primera.setRows(5);
        jScrollPane6.setViewportView(txt_funcion_primera);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane6)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Función Siguiente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 3, 15))); // NOI18N

        txt_funcion_segunda.setColumns(20);
        txt_funcion_segunda.setRows(5);
        jScrollPane10.setViewportView(txt_funcion_segunda);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane10)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tabla de Símbolos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 3, 15))); // NOI18N

        txt_tabla_simbolos.setColumns(20);
        txt_tabla_simbolos.setFont(new java.awt.Font("Monospaced", 1, 10)); // NOI18N
        txt_tabla_simbolos.setRows(5);
        jScrollPane11.setViewportView(txt_tabla_simbolos);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane11)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbl_ruta_archivo, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbl_ruta_archivo)
                                .addGap(33, 33, 33)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        limpiarTextAreas();
        txt_entrada.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
 /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
         */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JLabel lbl_ruta_archivo;
    private javax.swing.JTextArea txt_entrada;
    private javax.swing.JTextArea txt_esRecursiva;
    private javax.swing.JTextArea txt_funcion_primera;
    private javax.swing.JTextArea txt_funcion_segunda;
    private javax.swing.JTextArea txt_no_terminales;
    private javax.swing.JTextArea txt_no_terminales1;
    private javax.swing.JTextArea txt_producciones_ext;
    private javax.swing.JTextArea txt_producciones_ext1;
    private javax.swing.JTextArea txt_tabla_simbolos;
    private javax.swing.JTextArea txt_terminales;
    private javax.swing.JTextArea txt_terminales1;
    // End of variables declaration//GEN-END:variables
}
