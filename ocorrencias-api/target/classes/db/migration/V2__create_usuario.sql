CREATE TABLE usuario (
    cod_usuario     BIGSERIAL PRIMARY KEY,
    nme_usuario     VARCHAR(100)    NOT NULL,
    dsc_email       VARCHAR(150)    NOT NULL UNIQUE,
    dsc_senha       VARCHAR(255)    NOT NULL,
    dta_criacao     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Usuário padrão para testes: admin / admin123
INSERT INTO usuario (nme_usuario, dsc_email, dsc_senha)
VALUES (
    'Administrador',
    'admin@admin.com.br',
    '$2a$10$cz1beNbJ4nXabTA7/VSPBecJHLCuGH8/aTFrxHVRh2OJ7BZqk1EGa'
);
