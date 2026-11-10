CREATE TABLE IF NOT EXISTS inventario_items (
    id UUID PRIMARY KEY,
    prenda_id UUID NOT NULL,
    sede_id UUID NOT NULL,
    tipo_prenda VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    umbral_ultimas_unidades INT NOT NULL DEFAULT 3,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_publicacion TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_inventario_prenda_sede UNIQUE (prenda_id, sede_id)
);

CREATE TABLE IF NOT EXISTS inventario_tallas (
    id UUID PRIMARY KEY,
    inventario_item_id UUID NOT NULL,
    talla VARCHAR(10) NOT NULL,
    unidades_disponibles INT NOT NULL CHECK (unidades_disponibles >= 0),
    unidades_reservadas INT NOT NULL CHECK (unidades_reservadas >= 0),
    CONSTRAINT fk_inventario_tallas_item FOREIGN KEY (inventario_item_id)
        REFERENCES inventario_items (id) ON DELETE CASCADE,
    CONSTRAINT uk_inventario_item_talla UNIQUE (inventario_item_id, talla)
);

CREATE INDEX IF NOT EXISTS idx_inventario_items_prenda ON inventario_items (prenda_id);
CREATE INDEX IF NOT EXISTS idx_inventario_items_sede ON inventario_items (sede_id);
CREATE INDEX IF NOT EXISTS idx_inventario_tallas_lookup ON inventario_tallas (inventario_item_id, talla);
