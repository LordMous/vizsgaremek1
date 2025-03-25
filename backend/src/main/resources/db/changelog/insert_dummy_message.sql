INSERT INTO Message (chat_id, sender_id, message, message_type, created_at) VALUES
                                                        (1, 101, 'Szia Attila!', 'text', '2023-10-01 10:00:00'),  -- ferid üzenete attilának
                                                        (1, 102, 'Szia Ferid!', 'text', '2023-10-01 10:05:00'),    -- attila válasza feridnek
                                                        (2, 101, 'Szia Kristof!', 'text', '2023-10-01 11:00:00'),  -- ferid üzenete kristofnak
                                                        (2, 103, 'Szia Ferid!', 'text', '2023-10-01 11:10:00'),    -- kristof válasza feridnek
                                                        (3, 102, 'Szia Istvan!', 'text', '2023-10-01 12:00:00'),   -- attila üzenete istvannak
                                                        (3, 104, 'Szia Attila!', 'text', '2023-10-01 12:15:00'),   -- istvan válasza attilának
                                                        (1, 101, 'Nézd ezt a képet!', 'image', '2023-10-01 10:10:00'),  -- ferid küld egy képet
                                                        (2, 103, 'Itt egy videó!', 'video', '2023-10-01 11:20:00'),     -- kristof küld egy videót
                                                        (3, 102, 'Küldök egy fájlt!', 'file', '2023-10-01 12:30:00');