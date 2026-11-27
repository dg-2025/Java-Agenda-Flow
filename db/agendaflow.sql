-- =====================================================================
-- Agenda Flow — script do banco (SQLite)
-- O aplicativo executa estes comandos sozinho na primeira vez que abre
-- (classe persistencia/Esquema.java) e grava o arquivo em
-- %APPDATA%\AgendaFlow\agendaflow.db. Este arquivo é só para consulta.
-- =====================================================================

CREATE TABLE IF NOT EXISTS usuario (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT    NOT NULL,
    email       TEXT    NOT NULL UNIQUE COLLATE NOCASE,          -- RN-02
    senha_hash  TEXT    NOT NULL,                                -- RNF-02 (BCrypt)
    papel       TEXT    NOT NULL CHECK (papel IN ('ADMIN', 'CLIENTE'))
);

CREATE TABLE IF NOT EXISTS servico (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    nome             TEXT    NOT NULL,
    descricao        TEXT,
    duracao_minutos  INTEGER NOT NULL CHECK (duracao_minutos > 0),  -- RN-04
    valor            REAL    NOT NULL CHECK (valor >= 0),           -- RN-04
    ativo            INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS profissional (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nome   TEXT    NOT NULL,
    ativo  INTEGER NOT NULL DEFAULT 1
);

-- RF-05: quais serviços cada profissional realiza (N:N)
CREATE TABLE IF NOT EXISTS profissional_servico (
    profissional_id  INTEGER NOT NULL REFERENCES profissional(id) ON DELETE CASCADE,
    servico_id       INTEGER NOT NULL REFERENCES servico(id) ON DELETE CASCADE,
    PRIMARY KEY (profissional_id, servico_id)
);

-- RF-06: expediente semanal (dia_semana 1 = segunda ... 7 = domingo)
CREATE TABLE IF NOT EXISTS horario_trabalho (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    profissional_id  INTEGER NOT NULL REFERENCES profissional(id) ON DELETE CASCADE,
    dia_semana       INTEGER NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio      TEXT    NOT NULL,
    hora_fim         TEXT    NOT NULL,
    CHECK (hora_inicio < hora_fim)
);

CREATE TABLE IF NOT EXISTS agendamento (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id       INTEGER NOT NULL REFERENCES usuario(id),
    profissional_id  INTEGER NOT NULL REFERENCES profissional(id),
    servico_id       INTEGER NOT NULL REFERENCES servico(id),
    data             TEXT    NOT NULL,          -- AAAA-MM-DD
    hora_inicio      TEXT    NOT NULL,          -- HH:MM
    hora_fim         TEXT    NOT NULL,
    status           TEXT    NOT NULL CHECK (status IN ('AGENDADO', 'CONCLUIDO', 'CANCELADO', 'FALTA')),
    valor            REAL    NOT NULL,          -- RN-11: já com o desconto da Strategy
    data_criacao     TEXT    NOT NULL
);

-- RNF-03: nenhum profissional com dois agendamentos sobrepostos (barreira final, no banco)
CREATE TRIGGER IF NOT EXISTS trg_agendamento_sem_conflito
BEFORE INSERT ON agendamento
WHEN NEW.status IN ('AGENDADO', 'CONCLUIDO') AND EXISTS (
    SELECT 1 FROM agendamento a
     WHERE a.profissional_id = NEW.profissional_id
       AND a.data = NEW.data
       AND a.status IN ('AGENDADO', 'CONCLUIDO')
       AND a.hora_inicio < NEW.hora_fim
       AND NEW.hora_inicio < a.hora_fim)
BEGIN
    SELECT RAISE(ABORT, 'CONFLITO_HORARIO');
END;

CREATE INDEX IF NOT EXISTS idx_agendamento_prof_data ON agendamento (profissional_id, data);
CREATE INDEX IF NOT EXISTS idx_agendamento_usuario ON agendamento (usuario_id);
