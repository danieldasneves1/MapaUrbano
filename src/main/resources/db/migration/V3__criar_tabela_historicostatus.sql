CREATE TABLE HistoricoStatus(
    id BIGINT PRIMARY KEY,
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20),
    data_alteracao TIMESTAMP,
    ocorrencia_id BIGINT,
    CONSTRAINT fk_ocorrencia
             FOREIGN KEY (ocorrencia_id)
             REFERENCES ocorrencia(id)
             ON DELETE CASCADE
);