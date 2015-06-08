package app.controller;

import app.exception.NotFoundException;
import app.model.Phone;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiankuan on 8/6/15.
 */
@RestController
public class PhoneController {

    @Autowired
    private void setRedisTemplate(StringRedisTemplate redisTemplate) {
        kvOps = redisTemplate.opsForValue();
        hashOps = redisTemplate.opsForHash();
        listOps = redisTemplate.opsForList();
    }

    private ValueOperations<String, String> kvOps;

    private HashOperations<String, String, String> hashOps;

    private ListOperations<String, String> listOps;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(method = RequestMethod.GET, value="/phone/{id}")
    public Phone getPhone(@PathVariable String id) throws IOException {
        String value = kvOps.get("phone:" + id);
        if (value == null) {
            throw new NotFoundException();
        }

        Phone phone = mapper.readValue(value, Phone.class);
        return phone;
    }

    @RequestMapping(method = RequestMethod.POST, value="/phone/new", consumes = "application/json")
    public HttpEntity<Phone> createPhone(@RequestBody Phone phone) throws JsonProcessingException {
        String value = mapper.writeValueAsString(phone);
        kvOps.set("phone:" + phone.id, value);
        listOps.rightPush("phones", value);
        return new ResponseEntity<>(phone, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value="/phones")
    public List<Phone> listPhones() throws IOException {
        List<String> values = listOps.range("phones", 0, -1);
        List<Phone> phones = new ArrayList<>(values.size());
        for (String value: values) {
            phones.add(mapper.readValue(value, Phone.class));
        }
        return phones;
    }
}
