CREATE TABLE ocorrencia(
    id BIGINT PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    categoria VARCHAR(20),
    logradouro VARCHAR(100),
    numero VARCHAR(20),
    bairro VARCHAR(20),
    cidade VARCHAR(20),
    longitude DECIMAL(10, 8),
    latitude DECIMAL(10, 8),
    status VARCHAR(20),
    data_criacao TIMESTAMP,
    usuario_id BIGINT,
    CONSTRAINT fk_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES usuario(id)
            ON DELETE SET NULL
);