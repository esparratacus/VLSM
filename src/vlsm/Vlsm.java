/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlsm;

import java.io.*;
import java.util.*;
import jxl.*;
import jxl.write.*;


/**
 *
 * @author Niko
 */
public class Vlsm {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, WriteException {
        
        // TODO code application logic here
        
 
        System.out.println("Ingrese la IP principal");
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in ));
        String cad = null;
        try{
            cad = in.readLine();
        }catch(IOException e){
            System.out.println("Error "+e);
        }
        
        StringTokenizer tokenizer = new StringTokenizer (cad,". /");
        // ahora se hace 4 veces, una para cada parte de la cadena
        int [] dirIp = new int [5];
        int i;
        for (i= 0; i < dirIp.length && tokenizer.hasMoreElements(); i++) {
            int aux = Integer.parseInt(tokenizer.nextToken());
            if (aux < 0){
                System.out.println("No se permiten numeros negativos, se va a tomar positivo");
                aux *= -1;
            }
            if (aux>255){
                System.out.println("Fatal error: Un octeto excede el numero maximo");
                return;
            }
            dirIp [i] = aux;
        }
        if (i == 4){
            System.out.println("Error: debe ingresar la mascara /mascara u olvido un octeto");
            return;
        }else if (i != 5){
            System.out.println("Error: Error la direccion ip es invalida!!!");
            return;
        }
        if (dirIp [4] == 0 || dirIp[4] > 31){
            System.out.println("Fatal error: Mascara invalida");
            return;
        }
        //System.out.println("IP "+dirIp[0]+"."+dirIp[1]+"."+dirIp[2]+"."+dirIp[3]+"/"+dirIp[4]);
        int nR = 0;
        System.out.println("Ingrese el numero de subredes que desea:");
        try{
            nR = Integer.parseInt(in.readLine());
        }catch(IOException e){
            System.out.println("Error de ingreso de datos"+e);
            return;
        }catch(NumberFormatException e){
            System.out.println("El valor es erroneo");
            return;
        }
        int [] nhosts = new int [nR];
        for (int j = 0; j < nR; j++) {
            System.out.println("Ingrese el numero de hosts de la subred "+j+" ");
            try{
                nhosts [j] = Integer.parseInt(in.readLine());
                if (nhosts[j] <0){
                    System.out.println("Numero negativo invalido, se tomara como positivo");
                    nhosts[j] *= -1;
                }
            }catch(IOException e){
                System.out.println("Error de ingreso de datos"+e);
                return;
            }catch(NumberFormatException e){
                System.out.println("El valor es erroneo");
                return;
            }
        }
        System.out.println("\n\n\n");
        int ipPrincipal = armarInt(dirIp);
        int mascara=0;
        for (int j = 0; j < (int) dirIp[4]; j++) {
            mascara += 1 <<31-j;
        }      
        int redPrincipal = ipPrincipal & mascara;
        int [] dirRedPrincipal = desArmarInt(redPrincipal);
        System.out.println("Red principal: "+dirRedPrincipal[0]+"."+dirRedPrincipal[1]+"."+dirRedPrincipal[2]+"."+dirRedPrincipal[3]+"/"+dirIp[4]);
        
        int hostPrincipal = ipPrincipal & (~mascara);
        int nDirsPrincipal = ((int) Math.pow(2, (double) 32-dirIp[4]))-2;
        
        
        // Primero se organizan las subredes por tamaño:
        Arrays.sort(nhosts);
        nhosts = reverse(nhosts);
        
        // ahora se comeinza con la red que mas hosts requiere:
        double bitshost = 0;
        int [] hostbits = new int [nhosts.length];
        int IpVa = redPrincipal;
        subred [] redes = new subred [nR];
        
        int dirsNecesarias = 0;
        int dirsRequeridas = 0;
        //Se prepara libro de excel
        WritableWorkbook resultados= Workbook.createWorkbook(new File ("VLSM.xls"));
        WritableSheet subredes= resultados.createSheet("Subredes", 0);
        WritableFont bold = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
        WritableCellFormat wcf= new WritableCellFormat(bold);
    
        Label nSubred= new Label(0, 0, "nombre Subred",wcf);
        Label dirSubred = new Label(1,0,"direccion de subred",wcf);
        Label numHost= new Label(2,0,"# de host en la subred",wcf);
        Label numHostR = new Label(3,0,"# de hosts requeridos",wcf);
        Label mask = new Label(4,0,"Mascara (bits)",wcf);
        Label labMascara = new Label(5, 0, "Mascara de subred",wcf);
        Label inicioRango= new Label(6, 0, "Inicio de rango",wcf);
        Label finRango = new Label(7, 0, "Fin de rango",wcf);
        Label Broadcast = new Label(8, 0, "Broadcast",wcf);
        subredes.addCell(nSubred);
        subredes.addCell(dirSubred);
        subredes.addCell(numHost);
        subredes.addCell(numHostR);
        subredes.addCell(mask);
        subredes.addCell(labMascara);
        subredes.addCell(inicioRango);
        subredes.addCell(finRango);
        subredes.addCell(Broadcast);
        for (int y = 0; y < 8; y++) {
                  CellView cv = subredes.getColumnView(y);
                  cv.setAutosize(true);
                  subredes.setColumnView(y, cv);
        }   
        
        // Se calculan las subredes:
        for (int j = 0; j < nhosts.length; j++) {
           // primero se calculan cuantos bits se requieren de host:
            bitshost = Math.log10((double) (nhosts[j]+2))/Math.log10(2);  // log base 2 del numero de host mas 2
            if ((bitshost-(int) bitshost)*Math.pow(10, 3)!= 0){ // se toman los decimales para ver si el numero de bits dio entero, si no hay que sumarle un bit para aproximar y que se puedan direccionar todos los hosts necesarios
                bitshost = ((int) bitshost) +1;  // se le suma 1 para ir al siguiente numero de bits entero;
            }
            hostbits [j] = (int) bitshost;
            //System.out.println("bits "+j+" "+hostbits[j]);
            dirsRequeridas += nhosts[j];
            if (dirsNecesarias > nDirsPrincipal){
                System.out.println("ERROR: no hay suficientes direcciones");
                return;
            }
            dirsNecesarias += Math.pow(2, bitshost);
            redes[j] = new subred (IpVa,32-hostbits[j]);    // se comienza a generar las subredes con la Ip en la que va el conteo
                                                            // y con el numero de bits de la mascara
            IpVa = redes[j].getBroadcast() + 1;             // suma 1 a la de boradcast que es la siguiente de red.
            
            // Mostrar la red:
            subred elem = redes[j];
            System.out.println("Subred "+(j+1)+"\n\tHost requeridos: "+nhosts[j]+"\n\tSe pueden ubicar: "+
                    (int) (Math.pow(2, hostbits[j])-2));
            System.out.println("\tDireccion de subred: ");
            mostrarIp(elem.getIpRed());
            System.out.println("\tMascara de "+elem.getNmasc()+": ");
            mostrarIp(elem.getMascara());
            System.out.println("\tRango asignable: ");
            mostrarIp(elem.getIpRed()+1);
            mostrarIp(elem.getBroadcast()-1);
            System.out.println("\tBroadcast: ");
            mostrarIp(elem.getBroadcast());
            
            
            //Exportacion de datos a libro VLSM.xls
            subredes.addCell(new Label(0, j+1, j+1+" "));
            subredes.addCell(new Label(1,j+1,mostrarIp2(elem.getIpRed())));
            int numeH=(int) (Math.pow(2, hostbits[j])-2);
            subredes.addCell(new Label(2, j+1, numeH+""));
            subredes.addCell(new Label(3, j+1, ""+nhosts[j]));
            subredes.addCell(new Label (4, j+1,""+(elem.getNmasc())));
            subredes.addCell(new Label(5, j+1, mostrarIp2(elem.getMascara())));
            subredes.addCell(new Label(6, j+1, mostrarIp2(elem.getIpRed()+1)));
            subredes.addCell(new Label(7, j+1, mostrarIp2(elem.getBroadcast()-1)));
            subredes.addCell(new Label(8, j+1, mostrarIp2(elem.getBroadcast())));
            
            
            
        }
        resultados.write();
        resultados.close();
        // Se muestran todas las redes generadas.
        /*for (int j = 0; j < redes.length; j++) {
            subred elem = redes[j];
            System.out.println("Subred "+(j+1)+"\n\tHost requeridos: "+nhosts[j]+"\n\tSe pueden ubicar: "+
                    (int) (Math.pow(2, hostbits[j])-2));
            System.out.println("\tDireccion de subred: ");
            mostrarIp(elem.getIpRed());
            System.out.println("\tMascara de "+elem.getNmasc()+": ");
            mostrarIp(elem.getMascara());
            System.out.println("\tRango asignable: ");
            mostrarIp(elem.getIpRed()+1);
            mostrarIp(elem.getBroadcast()-1);
            System.out.println("\tBroadcast: ");
            mostrarIp(elem.getBroadcast());
        }*/
        
        // Balance:
        System.out.println("Numero de direcciones IP en la red principal: "+nDirsPrincipal);
        System.out.println("Se requierian: "+dirsRequeridas);
        System.out.println("Hay disponibles para hosts: "+(dirsNecesarias-(nhosts.length*2)));
        System.out.println("De todas las direcciones de la principal se esta usando el: "+(((float)dirsNecesarias/ (float) nDirsPrincipal)*100)+"%");
        System.out.println("De las direcciones disponibles (en todas las subredes), se usaran (con los requeridos): "+(((float) dirsRequeridas/(float) dirsNecesarias)*100)+"%");
        
        //Runtime rt = Runtime.getRuntime();
        //Process p= rt.exec("VLSM.xls");
    }
    
    private static int armarInt (int[] arr){
        int x;
        // la posicion 0 es la mas significativa:
        x = (int) (arr[0] << 24);
        x += (int) (arr[1] << 16);
        x += (int) (arr[2] << 8);
        x += (int) (arr[3]);
        
        return x;
    }
    
    private static int [] desArmarInt (int x){
        int [] arr = new int [4];
        
        for (int i = 3; i >=0; i--) {
            if ((byte) x < 0){
                int aux = x;
                aux = aux & 0x7F;
                arr[i] = (byte) aux;
                arr[i] = arr[i] | 0x80;
            }else
                arr[i] = (byte) x;
            
            x = x>>>8;   
        }
        return arr;
    }
    
    private static int[] reverse (int[] arr){
        int [] rev = new int [arr.length];
        for (int i = 0; i < arr.length; i++) {
            rev[i] = arr[arr.length-1-i];
        }
        return rev;
    }
    
    private static void mostrarIp (int x){
        int [] arr = desArmarInt(x);
        System.out.println("\t\t"+arr[0]+"."+arr[1]+"."+arr[2]+"."+arr[3]);
        
    }
    
    private static String mostrarIp2(int x)
    {
        int [] arr = desArmarInt(x);
        return "\t\t"+arr[0]+"."+arr[1]+"."+arr[2]+"."+arr[3];
    }
}
 