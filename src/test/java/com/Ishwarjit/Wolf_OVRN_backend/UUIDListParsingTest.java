package com.Ishwarjit.Wolf_OVRN_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UUIDListParsingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test() throws Exception {
        String uuid1 = "c26b570f-95f5-4a96-a1ce-1ffb72adb655";
        String uuid2 = "27ce47fb-ac99-4444-b025-9c546691838b";
        mockMvc.perform(get("/api/products?colorIds=" + uuid1 + "%2C" + uuid2))
                .andExpect(status().isOk());
    }
}
