package vn.qnam.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.qnam.dto.reponse.ResponseData;
import vn.qnam.dto.reponse.ResponseError;
import vn.qnam.service.MailService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/common")
public class CommonController {
    private final MailService mailService;

    @PostMapping("/email-sending")
    public ResponseData<?> sendEmail(@RequestParam String recipients,
                                     @RequestParam String subject,
                                     @RequestParam String content,
                                     @RequestParam(required = false) MultipartFile[] files) {
        try {
            log.info("Email sending successfully.");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Email sending successfully", mailService.sendEmail(recipients, subject, content, files));
        } catch (Exception e) {
            log.info("Email sending fail, message={}", e.getMessage());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Email sending fail.");
        }
    }
}
