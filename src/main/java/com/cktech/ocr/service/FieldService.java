package com.cktech.ocr.service;

import com.cktech.ocr.model.field.FieldDTO;
import com.cktech.ocr.repository.FieldRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldService {

    private final FieldRepository fieldRepository;

    public FieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public FieldDTO save(FieldDTO field) {
        return fieldRepository.save(field);
    }

    public FieldDTO get(Long id) {
        return fieldRepository.findById(id).orElseThrow();
    }

    public List<FieldDTO> getList() {
        return fieldRepository.findByIsDeletedFalse();
    }

    public List<FieldDTO> getActiveFieldList() {
        return fieldRepository.findByIsDeletedFalseAndIsActiveTrue();
    }

    public List<FieldDTO> getFieldsForScreen(String screenCode) {
        return fieldRepository.findByScreenCodeAndIsDeletedFalse(screenCode);
    }

    public void delete(Long id) {
        var data = fieldRepository.findById(id).orElseThrow();
        data.setIsDeleted(true);
        fieldRepository.save(data);
    }
}