BEGIN;

-- Tabela PERSON
INSERT INTO PERSON (email, taxnumber, name) VALUES
('alice@example.com', 123456789, 'Alice Silva'),
('bob@example.com', 987654321, 'Bob Costa'),
('carol@example.com', 112233445, 'Carol Dias'),
('david@example.com', 556677889, 'David Moreira'),
('eve@example.com', 223344556, 'Eve Santos');

-- Tabela SERVICECOST
INSERT INTO SERVICECOST (unlock, usable) VALUES
(1.00, 0.15);

-- Tabela TYPEOF
INSERT INTO TYPEOF (reference, nodays, price) VALUES
('res_ann', 365, 90.00),
('res_mon', 30, 15.00),
('tour_wk', 7, 25.00),
('tour_dy', 1, 5.00),
('promo_wkd', 2, 8.00);

-- Tabela STATION
INSERT INTO STATION (latitude, longitude) VALUES
(40.1234, -8.4567),
(40.5678, -8.9876),
(40.2345, -8.6789),
(40.3456, -8.7890),
(40.4567, -8.8901);

-- Tabela SCOOTERMODEL
INSERT INTO SCOOTERMODEL (designation, autonomy) VALUES
('Model A', 50),
('Model B', 45),
('Model C', 60),
('Model D', 70),
('Model E', 55);

-- Tabela EMPLOYEE
INSERT INTO EMPLOYEE (person) VALUES
(1),
(2),
(3),
(4),
(5);

-- Tabela CLIENT
INSERT INTO CLIENT (person, dtregister) VALUES
(1, '2024-01-01 08:00:00'),
(2, '2024-01-02 09:00:00'),
(3, '2024-01-03 10:00:00'),
(4, '2024-01-04 11:00:00'),
(5, '2024-01-05 12:00:00');

-- Tabela SCOOTER
INSERT INTO SCOOTER (weight, maxvelocity, battery, model) VALUES
(15.00, 25.0, 30, 1),
(12.00, 20.0, 25, 2),
(14.00, 22.0, 40, 3),
(13.50, 21.0, 35, 4),
(13.00, 19.0, 50, 5);

-- Tabela DOCK
INSERT INTO DOCK (station, state, scooter) VALUES
(1, 'occupy', 1),
(2, 'free', NULL),
(3, 'under maintenance', NULL),
(4, 'occupy', 2),
(5, 'free', NULL);

-- Tabela CARD
INSERT INTO CARD (credit, typeof, client) VALUES
(50.00, 'res_ann', 1),
(30.00, 'res_mon', 2),
(20.00, 'tour_wk', 3),
(5.00, 'tour_dy', 4),
(15.00, 'promo_wkd', 5);

-- Tabela REPLACEMENTORDER
INSERT INTO REPLACEMENTORDER (dtorder, dtreplacement, roccupation, station) VALUES
('2024-01-10 10:00:00', NULL, 50, 1),
('2024-01-11 11:00:00', NULL, 60, 2),
('2024-01-12 12:00:00', NULL, 70, 3),
('2024-01-13 13:00:00', NULL, 80, 4),
('2024-01-14 14:00:00', NULL, 90, 5);

-- Tabela REPLACEMENT
INSERT INTO REPLACEMENT (dtreplacement, action, dtreporder, repstation, employee) VALUES
('2024-01-15 15:00:00', 'inplace', '2024-01-10 10:00:00', 1, 1),
('2024-01-16 16:00:00', 'remove', '2024-01-11 11:00:00', 2, 2),
('2024-01-17 17:00:00', 'inplace', '2024-01-12 12:00:00', 3, 3),
('2024-01-18 18:00:00', 'remove', '2024-01-13 13:00:00', 4, 4),
('2024-01-19 19:00:00', 'inplace', '2024-01-14 14:00:00', 5, 5);

-- Tabela TOPUP
INSERT INTO TOPUP (dttopup, card, value) VALUES
('2024-01-20 20:00:00', 1, 10.00),
('2024-01-21 21:00:00', 2, 5.00),
('2024-01-22 22:00:00', 3, 7.50),
('2024-01-23 23:00:00', 4, 2.00),
('2024-01-24 00:00:00', 5, 4.50);

-- Tabela TRAVEL
INSERT INTO TRAVEL (dtinitial, comment, evaluation, dtfinal, client, scooter, stinitial, stfinal) VALUES
('2024-01-25 08:00:00', 'Great ride', 5, '2024-01-25 08:30:00', 1, 1, 1, 2),
('2024-01-25 08:00:00', 'Ok', 3, '2024-01-25 09:00:00', 2, 2, 2, 1),
('2024-01-26 09:00:00', 'Nice', 4, '2024-01-26 09:30:00', 3, 4, 1, 5),
('2024-01-26 09:00:00', 'Smooth ride', 4, '2024-01-26 09:30:00', 2, 2, 2, 3),
('2024-01-27 10:00:00', NULL, NULL, NULL, 3, 3, 3, NULL),
('2024-01-28 11:00:00', 'Good but slow', 3, '2024-01-28 11:45:00', 4, 4, 4, 5),
('2024-01-28 11:00:00', 'Great', 5, '2024-01-28 11:45:00', 1, 2, 3, 2),
('2024-01-29 12:00:00', 'Battery issue', 2, '2024-01-29 12:20:00', 5, 5, 5, 1);

COMMIT;
