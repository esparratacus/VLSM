/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlsm;

/**
 *
 * @author Niko
 */
public class subred {
    
    private int ipRed;
    private int hosts;
    private int broadcast;
    private int mascara; 
    private int nmasc;
    
    
    //
    // mascara: Numero de bits que corresponden a la porcion de red
    // ip: direccion ip de la red
    public subred (int ip, int mascara){
        this.ipRed = ip;
        this.nmasc = mascara;
        this.mascara = calcularMascara (mascara);
        hosts = (int) Math.pow(2, (double) (32-mascara)) -2;
        broadcast = ip + hosts + 1;
    }
    
    private int calcularMascara (int nbits){
        int mascara=0;
        for (int j = 0; j < nbits; j++) {
            mascara += 1 <<31-j;
        }
        return mascara;
    }

    public int getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(int broadcast) {
        this.broadcast = broadcast;
    }

    public int getHosts() {
        return hosts;
    }

    public void setHosts(int hosts) {
        this.hosts = hosts;
    }

    public int getIpRed() {
        return ipRed;
    }

    public void setIpRed(int ipRed) {
        this.ipRed = ipRed;
    }

    public int getMascara() {
        return mascara;
    }

    public void setMascara(int mascara) {
        this.mascara = mascara;
    }
    public int getNmasc (){
        return this.nmasc;
    }
    
}
