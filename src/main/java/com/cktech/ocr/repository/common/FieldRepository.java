package com.cktech.ocr.repository.common;

import com.cktech.ocr.model.field.FieldDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends GenericRepository<FieldDTO, Long> {
}