package br.edu.ifsc.lages.rca.chat.network;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MulticastListenerTest {
    @Test void mensagemInvalidaNaoEncerraEscuta() throws Exception {
        CountDownLatch recebida=new CountDownLatch(1);
        try(MulticastSocket socket=new MulticastSocket(0); DatagramSocket sender=new DatagramSocket()) {
            Thread worker=new Thread(new MulticastListener(socket,m -> { if("ok".equals(m.getMessage())) recebida.countDown(); }));
            worker.setDaemon(true); worker.start();
            for(String json:new String[]{"{invalido", "null", "{}", "{\"date\":\"19/10/2026\",\"time\":\"10:00:00\",\"username\":\"teste\",\"message\":\"ok\"}"}) {
                byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
                sender.send(new DatagramPacket(bytes,bytes.length,InetAddress.getLoopbackAddress(),socket.getLocalPort()));
            }
            assertTrue(recebida.await(3,TimeUnit.SECONDS));
            socket.close(); worker.join(1000); assertFalse(worker.isAlive());
        }
    }
}
