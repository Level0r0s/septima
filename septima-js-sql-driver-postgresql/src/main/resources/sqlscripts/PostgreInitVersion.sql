CREATE TABLE mtd_version
(
    VERSION_VALUE numeric NOT NULL,
    CONSTRAINT mtd_version_pk PRIMARY KEY(VERSION_VALUE)
)
#GO
INSERT INTO mtd_version (VERSION_VALUE) VALUES (0)
#GO
