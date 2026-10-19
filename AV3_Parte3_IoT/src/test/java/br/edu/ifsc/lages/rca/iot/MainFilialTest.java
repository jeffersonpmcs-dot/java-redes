package br.edu.ifsc.lages.rca.iot;
import br.edu.ifsc.lages.rca.iot.network.UdpServer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MainFilialTest {
    @Test void argumentosAusentesNaoDerrubamAplicacao() {
        assertDoesNotThrow(() -> MainFilial.main(new String[0]));
        assertDoesNotThrow(() -> MainFilial.main(null));
    }
    @Test void filialDesconhecidaRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> new UdpServer("config.json", "Inexistente"));
    }
    @Test void configuracaoDeDemonstracaoValida() {
        assertDoesNotThrow(() -> new UdpServer("config.json", "Lages"));
        assertDoesNotThrow(() -> new UdpServer("config.json", "Florianopolis"));
    }
}
