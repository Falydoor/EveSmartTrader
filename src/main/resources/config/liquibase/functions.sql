DROP FUNCTION IF EXISTS get_main_parent_market_group( BIGINT );
CREATE FUNCTION get_main_parent_market_group(BIGINT)
    RETURNS VARCHAR AS $$
DECLARE
    main_id ALIAS FOR $1;
    parent_id   BIGINT := 0;
    market_name VARCHAR;
BEGIN
    WHILE main_id IS NOT NULL AND parent_id IS NOT NULL LOOP
        SELECT
            parent_group_id,
            market_group_name
        INTO parent_id,
            market_name
        FROM inv_market_group
        WHERE id = main_id;
        IF parent_id IS NOT NULL
        THEN
            main_id := parent_id;
        END IF;
    END LOOP;
    RETURN main_id || '_' || market_name;
END;
$$ LANGUAGE plpgsql;
