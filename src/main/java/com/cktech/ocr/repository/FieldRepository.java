package com.cktech.ocr.repository;

import com.cktech.ocr.model.field.FieldDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends GenericRepository<FieldDTO, Long> {
    List<FieldDTO> findByScreenCodeAndIsDeletedFalse(String screenCode);
}