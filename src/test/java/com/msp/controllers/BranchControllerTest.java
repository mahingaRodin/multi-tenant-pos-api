package com.msp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msp.payloads.dtos.BranchDto;
import com.msp.services.BranchService;
import com.msp.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BranchController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BranchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BranchService branchService;

    @Test
    void createBranch_Success() throws Exception {
        BranchDto requestDto = new BranchDto();
        requestDto.setName("Main Branch");
        requestDto.setAddress("Kigali City Center");
        BranchDto mockResponse = new BranchDto();
        mockResponse.setId(UUID.randomUUID());
        mockResponse.setName("Main Branch");
        Mockito.when(branchService.createBranch(any(BranchDto.class))).thenReturn(mockResponse);
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Branch"));
    }

    @Test
    void testGetAllBranchesByStoreId_Success() throws Exception {
        UUID storeId = UUID.randomUUID();

        BranchDto branch = new BranchDto();
        branch.setName("Downtown Branch");

        Page<BranchDto> pagedResponse = new PageImpl<>(Collections.singletonList(branch));
        Mockito.when(branchService.getAllBranchesByStoreId(eq(storeId), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(pagedResponse);
        mockMvc.perform(get("/api/branches/store/{storeId}", storeId)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Downtown Branch"));
    }

    @Test
    void testUpdateBranch_Success() throws Exception {
        UUID branchId = UUID.randomUUID();

        BranchDto requestDto = new BranchDto();
        requestDto.setName("Updated Branch Name");

        Mockito.when(branchService.updateBranch(eq(branchId), any(BranchDto.class))).thenReturn(requestDto);
        mockMvc.perform(put("/api/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Branch Name"));
    }


    @Test
    void testDeleteBranch_Success() throws Exception {
        UUID branchId = UUID.randomUUID();
        Mockito.doNothing().when(branchService).deleteBranch(branchId);
        mockMvc.perform(delete("/api/branches/{id}", branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Branch deleted Successfully!"));
    }
}
