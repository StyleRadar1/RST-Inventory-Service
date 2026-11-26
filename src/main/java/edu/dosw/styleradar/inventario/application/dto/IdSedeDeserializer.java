package edu.dosw.styleradar.inventario.application.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import edu.dosw.styleradar.inventario.domain.model.IdSede;

import java.io.IOException;
import java.util.UUID;

public class IdSedeDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return IdSede.desdeTexto(parser.getValueAsString());
    }
}
