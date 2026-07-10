package com.cktech.ocr.controller;

import com.cktech.ocr.model.field.FieldDTO;
import com.cktech.ocr.service.FieldService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/field")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @PostMapping
    public FieldDTO save(@RequestBody FieldDTO field) {
        return fieldService.save(field);
    }

    @GetMapping("/{id}")
    public FieldDTO get(@PathVariable Long id) {
        return fieldService.get(id);
    }

    @GetMapping
    public List<FieldDTO> getList() {
        return fieldService.getList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fieldService.delete(id);
    }
}