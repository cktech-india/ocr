package com.cktech.ocr;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cktech.ocr.OcrService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OcrController {

    private final com.cktech.ocr.OcrService ocrService;

    public OcrController(com.cktech.ocr.OcrService ocrService) {
        this.ocrService = ocrService;
    }


    @PostMapping(value = "/ocr", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> extractOcr(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Uploaded document payload is empty."));
        }

        try {
            Map<String, Object> response = ocrService.processKycDocument(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "OCR engine internal execution error: " + e.getMessage()));
        }
    }

}
