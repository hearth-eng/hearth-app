package com.hearth.app.dao;

import java.sql.Date;

/**
 *
 * @author schan280
 */
public class AvailabilityQueryGen {
    
    AvailabilityQueryGen() {}
    
    String availabilityQuery(Integer serviceId, Short durationMin, Date date) {
        StringBuilder buff = new StringBuilder(1024);
        short slots = (short) Math.ceil(durationMin / 60.0);

        buff.append("WITH avail_status AS (").append("\n")
                .append("    SELECT d.professional_id").append("\n")
                .append("           , d.date").append("\n")
                .append("           , d.start_time AS slot_1").append("\n");

        for (short i = 0; i < slots; i++) {
            buff.append("           , CASE LEAD(d.start_time, ").append(i).append(") OVER w").append("\n")
                    .append("                WHEN '17:00:00'::time THEN '18:00:00'::time").append("\n")
                    .append("                ELSE LEAD(d.start_time, ").append(i + 1).append(") OVER w").append("\n")
                    .append("             END AS slot_").append(i + 2).append("\n");
        }
        buff.append("      FROM fks_services a").append("\n")
                .append("     INNER JOIN fks_professional_services b ON (a.service_id = b.service_id)").append("\n")
                .append("     INNER JOIN fks_professionals c ON (b.professional_id = c.professional_id AND c.is_verified = ?)").append("\n")
                .append("     INNER JOIN fks_availabilities d ON (c.professional_id = d.professional_id AND d.date = ? AND d.is_booked = ?)").append("\n")
                .append("     WHERE a.service_id = ?").append("\n");

        buff.append("    WINDOW w AS (").append("\n")
                .append("        PARTITION BY d.professional_id, d.date").append("\n")
                .append("        ORDER BY d.start_time").append("\n")
                .append("    )").append("\n")
                .append(")").append("\n");

        buff.append(", prof_avail_status AS (").append("\n")
                .append("    SELECT professional_id, date");

        for (short i = 0; i <= slots; i++) {
            buff.append(", slot_").append(i + 1);
        }
        buff.append("\n");
        buff.append("      FROM avail_status").append("\n")
                .append("     WHERE EXTRACT(HOUR FROM (slot_2 - slot_1)) = 1").append("\n");

        for (short i = 1; i < slots; i++) {
            buff.append("       AND EXTRACT(HOUR FROM (slot_").append(i + 2).append("").append(" - ").append("slot_").append(i + 1).append(")) = 1").append("\n");
        }
        buff.append(")").append("\n");
        buff.append("SELECT slot_1 AS from_time").append(", ").append("slot_").append(slots + 1).append(" AS to_time, COUNT(*) AS slots").append("\n")
                .append("  FROM prof_avail_status").append("\n")
                .append(" GROUP BY slot_1").append(", ").append("slot_").append(slots + 1).append("\n")
                .append(" ORDER BY slot_1").append(", ").append("slot_").append(slots + 1).append("\n");

        return buff.toString();
    }
    
    String matchingProfessionalQuery(Boolean takeLock, Boolean fair) {
        String fairQuery = """
            WITH params AS (
                SELECT
                    CAST (? AS DATE) AS booking_date,
                    CAST (? AS TIME) AS work_start,
                    CAST (? AS TIME) AS work_end
            ),
            prof_eligibility AS (
                SELECT d.professional_id, d.date, p.work_start, p.work_end, MIN(d.start_time) AS start_time, MAX(d.end_time) AS end_time, COUNT(*) AS slot_count
                  FROM fks_professional_services a
                 INNER JOIN fks_professionals b ON (a.professional_id = b.professional_id AND b.is_verified = ?)
                 INNER JOIN fks_professional_neighbourhoods c ON (b.professional_id = c.professional_id AND c.neighbourhood_id = ?)
                 INNER JOIN fks_availabilities d ON (b.professional_id = d.professional_id)
                 CROSS JOIN params p
                 WHERE a.service_id = ?
                   AND d.date = p.booking_date
                   AND d.start_time >= p.work_start
                   AND d.end_time <= p.work_end
                   AND d.is_booked = ?
                   AND NOT EXISTS (
                       SELECT 1
                         FROM fks_availabilities e
                        WHERE d.professional_id = e.professional_id
                          AND d.date = e.date
                          AND e.is_booked = ?)
                 GROUP BY d.professional_id, d.date, p.work_start, p.work_end
                HAVING COUNT(*) = EXTRACT (HOUR FROM (p.work_end - p.work_start))
                   AND MIN(d.start_time) = p.work_start
                   AND MAX(d.end_time) = p.work_end
            )
            SELECT a.*
              FROM fks_availabilities a
             INNER JOIN prof_eligibility f ON (f.professional_id = a.professional_id AND f.date = a.date)
             CROSS JOIN params p
             WHERE a.start_time >= p.work_start
               AND a.end_time <= p.work_end
             ORDER BY a.professional_id, a.start_time
             LIMIT (SELECT EXTRACT(HOUR FROM (work_end - work_start))
                      FROM params)
            """;
        
        String query = """
            WITH params AS (
                SELECT
                    CAST (? AS DATE) AS booking_date,
                    CAST (? AS TIME) AS work_start,
                    CAST (? AS TIME) AS work_end
            ),
            prof_eligibility AS (
                SELECT d.professional_id, d.date, p.work_start, p.work_end, MIN(d.start_time) AS start_time, MAX(d.end_time) AS end_time, COUNT(*) AS slot_count
                  FROM fks_professional_services a
                 INNER JOIN fks_professionals b ON (a.professional_id = b.professional_id AND b.is_verified = ?)
                 INNER JOIN fks_professional_neighbourhoods c ON (b.professional_id = c.professional_id AND c.neighbourhood_id = ?)
                 INNER JOIN fks_availabilities d ON (b.professional_id = d.professional_id)
                 CROSS JOIN params p
                 WHERE a.service_id = ?
                   AND d.date = p.booking_date
                   AND d.start_time >= p.work_start
                   AND d.end_time <= p.work_end
                   AND d.is_booked = ?
                 GROUP BY d.professional_id, d.date, p.work_start, p.work_end
                HAVING COUNT(*) = EXTRACT (HOUR FROM (p.work_end - p.work_start))
                   AND MIN(d.start_time) = p.work_start
                   AND MAX(d.end_time) = p.work_end
            )
            SELECT a.*
              FROM fks_availabilities a
             INNER JOIN prof_eligibility f ON (f.professional_id = a.professional_id AND f.date = a.date)
             CROSS JOIN params p
             WHERE a.start_time >= p.work_start
               AND a.end_time <= p.work_end
             ORDER BY a.professional_id, a.start_time
             LIMIT (SELECT EXTRACT(HOUR FROM (work_end - work_start))
                      FROM params)
            """;
        
        String q = fair ? fairQuery : query;
        if (takeLock) {
            q += "\n FOR UPDATE";
        }
        return q;
    }
}
