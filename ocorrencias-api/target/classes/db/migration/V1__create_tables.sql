CREATE TABLE cliente (
    cod_cliente     BIGSERIAL PRIMARY KEY,
    nme_cliente     VARCHAR(100)        NOT NULL,
    dta_nascimento  DATE                NOT NULL,
    nro_cpf         VARCHAR(11)         NOT NULL UNIQUE,
    dta_criacao     TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE endereco (
    cod_endereco    BIGSERIAL PRIMARY KEY,
    nme_logradouro  VARCHAR(150)        NOT NULL,
    nme_bairro      VARCHAR(100)        NOT NULL,
    nro_cep         VARCHAR(8)          NOT NULL,
    nme_cidade      VARCHAR(100)        NOT NULL,
    nme_estado      VARCHAR(2)             NOT NULL
);

CREATE TABLE ocorrencia (
    cod_ocorrencia  BIGSERIAL PRIMARY KEY,
    cod_cliente     BIGINT              NOT NULL REFERENCES cliente(cod_cliente),
    cod_endereco    BIGINT              NOT NULL REFERENCES endereco(cod_endereco),
    dta_ocorrencia  DATE                NOT NULL DEFAULT CURRENT_DATE,
    sta_ocorrencia  VARCHAR(20)         NOT NULL DEFAULT 'ATIVA'
                        CHECK (sta_ocorrencia IN ('ATIVA', 'FINALIZADA'))
);

CREATE TABLE foto_ocorrencia (
    cod_foto_ocorrencia BIGSERIAL PRIMARY KEY,
    cod_ocorrencia      BIGINT          NOT NULL REFERENCES ocorrencia(cod_ocorrencia),
    dta_criacao         TIMESTAMP       NOT NULL DEFAULT NOW(),
    dsc_path_bucket     VARCHAR(500)    NOT NULL,
    dsc_hash            VARCHAR(64)     NOT NULL
);

CREATE INDEX idx_ocorrencia_cliente     ON ocorrencia(cod_cliente);
CREATE INDEX idx_ocorrencia_endereco    ON ocorrencia(cod_endereco);
CREATE INDEX idx_ocorrencia_status      ON ocorrencia(sta_ocorrencia);
CREATE INDEX idx_ocorrencia_data        ON ocorrencia(dta_ocorrencia);
CREATE INDEX idx_foto_ocorrencia        ON foto_ocorrencia(cod_ocorrencia);
