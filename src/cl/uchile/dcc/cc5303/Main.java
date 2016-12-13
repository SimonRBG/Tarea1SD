package cl.uchile.dcc.cc5303;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando Juego de SSDD...");

        String ip = Util.getIp();
        String port = "60000";
        int id = -1;
        if(args.length > 1) {
            ip = args[0];
            port = args[1];
        } else{
            System.out.println("ingresa ip y puerto  de Coordinador como argumentos");
            System.out.println("java Main <ip> <puerto>");
            //return;
        }
        if(args.length >2){
            id = Integer.parseInt(args[2]);
        }

        String url_coordinator = "rmi://ip:port/zatackaCoordinator";
        url_coordinator=url_coordinator.replace("ip",ip);
        url_coordinator=url_coordinator.replace("port",port);
        System.out.println("url_coordinator: " + url_coordinator);

        Client c1;
        if(id>=0){
            c1 = new Client(url_coordinator, id);
        }else {
            c1 = new Client(url_coordinator);
        }

        c1.start();
    }
}
