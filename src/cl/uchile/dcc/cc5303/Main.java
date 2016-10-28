package cl.uchile.dcc.cc5303;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando Juego de SSDD...");
        String url_server;
        if(args.length>0) {
            url_server = args[0];
        } else{
            System.out.println("ingresa la url del servidor como argumento");
            System.out.println("java Main <url_server>");
            return;
        }
        System.out.println("urlServer: "+url_server);
        Client c1 = new Client(url_server);
        c1.start();
    }
}
