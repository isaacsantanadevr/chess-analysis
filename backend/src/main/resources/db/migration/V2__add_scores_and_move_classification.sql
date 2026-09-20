-- Registros antigos ficam sem score_type: o score mate original nao foi armazenado.
ALTER TABLE move_analyses
    ALTER COLUMN evaluation DROP NOT NULL,
    ADD COLUMN score_type VARCHAR(8),
    ADD COLUMN centipawns INTEGER,
    ADD COLUMN mate INTEGER,
    ADD COLUMN mate_winner VARCHAR(5),
    ADD COLUMN before_score_type VARCHAR(8),
    ADD COLUMN before_centipawns INTEGER,
    ADD COLUMN before_mate INTEGER,
    ADD COLUMN before_mate_winner VARCHAR(5),
    ADD COLUMN best_move_before TEXT,
    ADD COLUMN centipawn_loss INTEGER CHECK (centipawn_loss >= 0),
    ADD COLUMN classification VARCHAR(16) CHECK (classification IN ('BEST', 'GOOD', 'INACCURACY', 'MISTAKE', 'BLUNDER'));

ALTER TABLE analysis_jobs
    ADD COLUMN initial_score_type VARCHAR(8),
    ADD COLUMN initial_centipawns INTEGER,
    ADD COLUMN initial_mate INTEGER,
    ADD COLUMN initial_mate_winner VARCHAR(5);

ALTER TABLE move_analyses ADD CONSTRAINT ck_move_analysis_score CHECK (
    (score_type IS NULL AND centipawns IS NULL AND mate IS NULL AND mate_winner IS NULL)
    OR (score_type IS NOT NULL AND (
        (score_type = 'CP' AND centipawns IS NOT NULL AND mate IS NULL AND mate_winner IS NULL AND evaluation IS NOT NULL)
        OR (score_type = 'MATE' AND centipawns IS NULL AND evaluation IS NULL AND mate IS NOT NULL
            AND mate_winner IS NOT NULL AND ((mate >= 0 AND mate_winner = 'WHITE') OR (mate <= 0 AND mate_winner = 'BLACK')))
    ))
);

ALTER TABLE move_analyses ADD CONSTRAINT ck_move_analysis_before_score CHECK (
    (before_score_type IS NULL AND before_centipawns IS NULL AND before_mate IS NULL AND before_mate_winner IS NULL)
    OR (before_score_type IS NOT NULL AND (
        (before_score_type = 'CP' AND before_centipawns IS NOT NULL AND before_mate IS NULL AND before_mate_winner IS NULL)
        OR (before_score_type = 'MATE' AND before_centipawns IS NULL AND before_mate IS NOT NULL
            AND before_mate_winner IS NOT NULL AND ((before_mate >= 0 AND before_mate_winner = 'WHITE') OR (before_mate <= 0 AND before_mate_winner = 'BLACK')))
    ))
);

ALTER TABLE analysis_jobs ADD CONSTRAINT ck_analysis_job_initial_score CHECK (
    (initial_score_type IS NULL AND initial_centipawns IS NULL AND initial_mate IS NULL AND initial_mate_winner IS NULL)
    OR (initial_score_type IS NOT NULL AND (
        (initial_score_type = 'CP' AND initial_centipawns IS NOT NULL AND initial_mate IS NULL AND initial_mate_winner IS NULL AND initial_evaluation IS NOT NULL)
        OR (initial_score_type = 'MATE' AND initial_centipawns IS NULL AND initial_evaluation IS NULL AND initial_mate IS NOT NULL
            AND initial_mate_winner IS NOT NULL AND ((initial_mate >= 0 AND initial_mate_winner = 'WHITE') OR (initial_mate <= 0 AND initial_mate_winner = 'BLACK')))
    ))
);
