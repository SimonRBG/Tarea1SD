package cl.uchile.dcc.cc5303;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando Juego de SSDD...");

        Client c1 = new Client();
        Client c2 = new Client();
        c1.start();
        c2.start();
    }
}
