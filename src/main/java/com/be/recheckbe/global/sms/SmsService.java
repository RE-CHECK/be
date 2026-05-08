package com.be.recheckbe.global.sms;

import com.be.recheckbe.domain.auth.exception.AuthErrorCode;
import com.be.recheckbe.global.exception.CustomException;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

  private static final Logger log = LoggerFactory.getLogger(SmsService.class);

  private final DefaultMessageService messageService;
  private final String fromNumber;

  public SmsService(
      @Value("${solapi.api-key}") String apiKey,
      @Value("${solapi.api-secret}") String apiSecret,
      @Value("${solapi.from-number}") String fromNumber) {
    this.messageService =
        NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    this.fromNumber = fromNumber;
  }

  public void sendVerificationCode(String to, String code) {
    Message message = new Message();
    message.setFrom(fromNumber);
    message.setTo(to);
    message.setText(
        "[RE:AJOU CHECK]\n" + "인증번호: " + code + "\n" + "타인에게 절대로 노출하지 마세요.");
    try {
      messageService.sendOne(new SingleMessageSendingRequest(message));
    } catch (Exception e) {
      log.error("[Solapi] SMS 발송 실패 - to: {}, cause: {}", to, e.getMessage(), e);
      throw new CustomException(AuthErrorCode.SMS_SEND_FAIL);
    }
  }
}
