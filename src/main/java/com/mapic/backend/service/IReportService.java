package com.mapic.backend.service;

import com.mapic.backend.dto.request.ReportRequest;

public interface IReportService {
    void submitReport(ReportRequest request);
}
