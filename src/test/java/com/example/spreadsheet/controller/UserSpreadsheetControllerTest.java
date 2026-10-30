package com.example.spreadsheet.controller;

import com.example.spreadsheet.service.UserSpreadsheetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserSpreadsheetController.class,
        properties = "app.aws.s3.bucket=input-bucket")
class UserSpreadsheetControllerTest {

    private static final String VALID_BODY = """
            {"users":[{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSpreadsheetService spreadsheetService;

    @Test
    void returnsCreatedWithBucketAndObjectKey() throws Exception {
        when(spreadsheetService.createAndUpload(anyList())).thenReturn("users/id.xlsx");

        mockMvc.perform(post("/api/spreadsheets/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bucket").value("input-bucket"))
                .andExpect(jsonPath("$.objectKey").value("users/id.xlsx"));
    }

    @Test
    void rejectsEmptyUserList() throws Exception {
        mockMvc.perform(post("/api/spreadsheets/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"users\":[]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(spreadsheetService);
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/spreadsheets/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"users":[{"firstName":"Ada","lastName":"Lovelace","email":"nope"}]}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(spreadsheetService);
    }

    @Test
    void propagatesUploadFailureStatus() throws Exception {
        when(spreadsheetService.createAndUpload(anyList()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY));

        mockMvc.perform(post("/api/spreadsheets/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadGateway());
    }
}
