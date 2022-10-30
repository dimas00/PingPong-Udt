package csi.ufsm.br.pingpoong;

import com.fasterxml.jackson.databind.ObjectMapper;
import csi.ufsm.br.pingpoong.model.Pacote;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

@SpringBootApplication
public class PingPoongApplication {


    public static void main(String[] args) {
        SpringApplication.run(PingPoongApplication.class, args);
    }

    public PingPoongApplication() throws IOException {
        new Thread(new Servidor()).start();
        new Thread(new Cliente()). start();
    }


    private class Servidor implements Runnable {

        ServerSocket server = new ServerSocket(5555);
        public Servidor() throws IOException {
        }

        @SneakyThrows
        @Override
        public void run() {

            DatagramSocket servidor = new DatagramSocket(5555);

            while (true){
                byte[] buf = new byte[1024];
                Socket resp = server.accept();
                int respostaint = resp.getInputStream().read(buf);

                String strResp = new String(buf, 0, respostaint, "UTF-8");

                Pacote pacoteResp = new ObjectMapper().readValue(strResp, Pacote.class);

                if (pacoteResp.getTipo() == Pacote.TipoPacote.ping){
                    long timeNano = System.nanoTime() - pacoteResp.getTimeNano();
                    double tempo = timeNano/1000000.0;
                    System.out.println(" " + tempo + "ns");
                }else {
                    System.out.println("Pacote recebido de " + resp.getInetAddress() + "deveria ser ping");
                }

            }


        }
    }

    private class Cliente implements  Runnable{

        @SneakyThrows
        @Override
        public void run() {


            Scanner scanner = new Scanner(System.in);

            Socket socket = new Socket();

            while (true){
                System.out.println("\nDigite o endereço: ");
                String endereco = scanner.nextLine();
                Pacote pacote = Pacote.builder()
                        .tipo(Pacote.TipoPacote.ping)
                        .nome(("Dimais "))
                        .timeNano(System.nanoTime()).build();
                String strpacote = new ObjectMapper().writeValueAsString(pacote);

                byte[] bArray = strpacote.getBytes("UTF-8");
                        Socket tcpPacote = new Socket(bArray, 0, bArray.length, InetAddress.getByName(endereco), 5555)


                       DatagramPacket udpPacket = new DatagramPacket(bArray,
                               0,
                               bArray.length,
                               InetAddress.getByName(endereco),
                               5555);
                socket.send(udpPacket);
                byte[] buf = new byte[1024];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.getOutputStream();
                String strResp = new String(buf, 0, resp.getLength(), "UTF-8");

                Pacote pacoteResp = new ObjectMapper().readValue(strResp, Pacote.class);
                if (pacoteResp.getTipo() == Pacote.TipoPacote.pong){
                    long timeNano = System.nanoTime() - pacote.getTimeNano();
                    System.out.println(" " + timeNano + "ns");
                }else {
                    System.out.println("Pacote recebido de " + resp.getAddress().getHostName() + "deveria ser PONG");
                }

            }
        }
    }
}