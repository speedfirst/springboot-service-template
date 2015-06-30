package app.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author jiankuan
 *         29/6/15.
 */
@RestController
public class UploadController {

    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    @ResponseBody
    public String handleUpload(@RequestParam String name, @RequestParam MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String result = new String(file.getBytes());
            return "You have uploaded a file " + name + "\n" + result;

        }
        return "You didn't upload a file";
    }
}
