package com.runningRank.runningRank.certificateProcessingJob.respository;

import com.runningRank.runningRank.certificateProcessingJob.dto.RetryGptJob;
import com.runningRank.runningRank.certificateProcessingJob.dto.RetryOcrJob;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CertificateProcessingJobJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 1. Job들 중 생성 시간이 10분이 넘었는데 ALL_DONE이 아닌 잡들은 실패로 간주
     * 2. 실패한 JOB들중 PENDING || OCR_QUEUE_SEND || OCR_QUEUE_FAILED인 JOB들 조회해서 OCR QUEUE SEND 재시도
     * @return
     */
    public List<RetryOcrJob> findRetryOcrJob() {
        String sql = """
            SELECT 
                user_id,
                id AS job_id,
                original_s3_url AS s3_image_url
            FROM 
                certificate_processing_job
            WHERE 
                created_at <= NOW() - INTERVAL 10 MINUTE
              AND status <> 'ALL_DONE'
              AND status IN ('PENDING', 'OCR_QUEUE_SEND', 'OCR_QUEUE_FAILED')
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> RetryOcrJob.builder()
                .userId(rs.getLong("user_id"))
                .jobId(rs.getString("job_id"))   // UUID를 String으로 받음
                .s3ImageUrl(rs.getString("s3_image_url"))
                .build()
        );
    }


    /**
     * GPT 재시도 로직
     * 1. Job들 중 생성 시간이 10분이 넘었는데 ALL_DONE이 아닌 잡들은 실패로 간주
     * 2. 실패한 JOB들중 OCR_DONE || GPT_QUEUE_SEND || GPT_QUEUE_FAILED 인 JOB들 조회해서 GPT QUEUE SEND 재시도
     */
    public List<RetryGptJob> findRetryGptJob() {
        String sql = """
            SELECT 
                user_id,
                id AS job_id,
                ocr_result_url AS s3_text_url,
                original_s3_url AS s3_image_url
            FROM 
                certificate_processing_job
            WHERE 
                created_at <= NOW() - INTERVAL 10 MINUTE
              AND status <> 'ALL_DONE'
              AND status IN ('OCR_DONE', 'GPT_QUEUE_SEND', 'GPT_QUEUE_FAILED')
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> RetryGptJob.builder()
                .userId(rs.getLong("user_id"))
                .jobId(rs.getString("job_id"))
                .s3TextUrl(rs.getString("s3_text_url"))
                .s3ImageUrl(rs.getString("s3_image_url"))
                .build()
        );
    }
}
