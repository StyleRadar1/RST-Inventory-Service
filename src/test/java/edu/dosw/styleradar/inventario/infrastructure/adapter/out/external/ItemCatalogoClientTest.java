package edu.dosw.styleradar.inventario.infrastructure.adapter.out.external;

import edu.dosw.styleradar.inventario.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.inventario.domain.port.out.PrendaCatalogoInfo;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCatalogoClientTest {

    @Mock
    private ItemCatalogoFeignClient feignClient;

    @InjectMocks
    private ItemCatalogoClient client;

    @Test
    @DisplayName("Retorna PrendaCatalogoInfo cuando ms-item-catalogo responde 200")
    void retornaPrendaExistente() {
        UUID prendaId = UUID.randomUUID();
        UUID almacenId = UUID.randomUUID();
        when(feignClient.obtenerPrenda(prendaId)).thenReturn(new PrendaCatalogoDto(prendaId, almacenId));

        Optional<PrendaCatalogoInfo> resultado = client.obtenerPrendaPorId(prendaId);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().id()).isEqualTo(prendaId);
        assertThat(resultado.get().almacenId()).isEqualTo(almacenId);
    }

    @Test
    @DisplayName("Retorna Optional.empty cuando prendaId o respuesta son nulos")
    void manejaNulos() {
        assertThat(client.obtenerPrendaPorId(null)).isEmpty();
        verifyNoInteractions(feignClient);

        UUID prendaId = UUID.randomUUID();
        when(feignClient.obtenerPrenda(prendaId)).thenReturn(null);
        assertThat(client.obtenerPrendaPorId(prendaId)).isEmpty();
    }

    @Test
    @DisplayName("Retorna Optional.empty cuando ms-item-catalogo responde 404")
    void retornaVacioEn404() {
        UUID prendaId = UUID.randomUUID();
        Request req = Request.create(Request.HttpMethod.GET, "/api/v1/garments/" + prendaId,
                Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException notFound = FeignException.errorStatus(
                "ItemCatalogoFeignClient#obtenerPrenda(UUID)",
                Response.builder().status(404).reason("Not Found").request(req).headers(Map.of()).build());

        when(feignClient.obtenerPrenda(prendaId)).thenThrow(notFound);

        assertThat(client.obtenerPrendaPorId(prendaId)).isEmpty();
    }

    @Test
    @DisplayName("Lanza CatalogoNoDisponibleException cuando ms-item-catalogo falla o no responde")
    void lanza503EnFalloDeCatalogo() {
        UUID prendaId = UUID.randomUUID();
        Request req = Request.create(Request.HttpMethod.GET, "/api/v1/garments/" + prendaId,
                Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException unavailable = FeignException.errorStatus(
                "ItemCatalogoFeignClient#obtenerPrenda(UUID)",
                Response.builder().status(503).reason("Service Unavailable").request(req).headers(Map.of()).build());

        when(feignClient.obtenerPrenda(prendaId)).thenThrow(unavailable);

        assertThatThrownBy(() -> client.obtenerPrendaPorId(prendaId))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }
}
