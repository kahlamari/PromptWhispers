package in.kahl.promptwhispers.service;

import com.cloudinary.Cloudinary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(@NonNull String url) {
        try {
            var cloudinaryResponse = cloudinary.uploader().upload(url, Map.of(
                    "resource_type", "auto",
                    "public_id", UUID.randomUUID().toString(),
                    "folder", "promptwhispers_test"
            ));

            return cloudinaryResponse.get("secure_url").toString();
        } catch (IOException exception) {
            return url;
        }
    }
}
