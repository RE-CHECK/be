package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.receipt.repository.ReceiptRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReceiptServiceImpl implements AdminReceiptService {

  private final ReceiptRepository receiptRepository;

  @Override
  @Transactional(readOnly = true)
  public void downloadCollegePaymentCsv(HttpServletResponse response) throws IOException {
    String filename = "college_payment_" + LocalDate.now() + ".csv";
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

    PrintWriter writer = response.getWriter();

    // Excel 한글 깨짐 방지 BOM
    writer.write('\uFEFF');

    // 헤더
    writer.println("\"일자\",\"단과대\",\"총 소비금액\"");

    List<Object[]> rows = receiptRepository.findDailyPaymentAmountByCollege();
    for (Object[] row : rows) {
      String date = escape(String.valueOf(row[0]));
      String collegeName = escape(String.valueOf(row[1]));
      String totalAmount = String.valueOf(row[2]);

      writer.printf("\"%s\",\"%s\",\"%s\"%n", date, collegeName, totalAmount);
    }

    writer.flush();
  }

  private String escape(String value) {
    if (value == null) return "";
    return value.replace("\"", "\"\"");
  }
}
