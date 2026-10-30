package com.example.spreadsheet.controller;

import com.example.spreadsheet.service.GcsReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportDownloadController.class)
class ReportDownloadControllerTest {

    private static final String BODY = "{\"objectName\":\"processed/report.csv\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GcsReportService reportService;

    @Test
    void returnsReportAsAttachment() throws Exception {
        when(reportService.download("processed/report.csv")).thenReturn(
                new GcsReportService.DownloadedReport("report.csv", "text/csv", new byte[] {1, 2}));

        mockMvc.perform(post("/api/reports/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().bytes(new byte[] {1, 2}))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 2))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("report.csv")));
    }

    @Test
    void fallsBackToBinaryWhenStoredContentTypeIsInvalid() throws Exception {
        when(reportService.download("processed/report.csv")).thenReturn(
                new GcsReportService.DownloadedReport("report.csv", "not-a-media-type", new byte[] {1}));

        mockMvc.perform(post("/api/reports/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void rejectsBlankObjectName() throws Exception {
        mockMvc.perform(post("/api/reports/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectName\":\" \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(reportService);
    }

    @Test
    void propagatesMissingReportStatus() throws Exception {
        when(reportService.download("processed/report.csv"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/reports/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound());
    }
}
