package in.kahl.promptwhispers.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudinaryServiceTest {

    private final Cloudinary mockCloudinary = mock(Cloudinary.class);

    private CloudinaryService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new CloudinaryService(mockCloudinary);
    }

    @Test
    void uploadImageTest_whenProvideImageUrl_thenReturnCloudinaryImageUrl() throws IOException {
        // ARRANGE
        String inputUrl = "https://example.com/inputimage.png";
        Map<String, Object> mockResponse = Map.of("secure_url", "https://example.com/image.png");

        when(mockCloudinary.uploader()).thenReturn(mock(Uploader.class));
        when(mockCloudinary.uploader().upload(anyString(), anyMap())).thenReturn(mockResponse);

        // ACT
        String imageUrlActual = serviceUnderTest.uploadImage(inputUrl);

        // ASSERT
        assertEquals(mockResponse.get("secure_url"), imageUrlActual);
    }
}