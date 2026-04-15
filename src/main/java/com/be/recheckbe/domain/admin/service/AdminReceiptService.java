package com.be.recheckbe.domain.admin.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AdminReceiptService {

  void downloadCollegePaymentCsv(HttpServletResponse response) throws IOException;
}
