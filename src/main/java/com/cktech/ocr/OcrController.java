package com.cktech.ocr;

import com.cktech.ocr.model.field.FieldDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @GetMapping("/field/{id}")
    public ResponseEntity<FieldDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ocrService.getById(id));
    }

    @PostMapping(value = "/ocr", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> extractOcr(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Uploaded document payload is empty."));
        }

        try {
            Map<String, Object> response = ocrService.processKycDocument(file);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "OCR engine internal execution error: " + e.getMessage()));
        }
    }
}