package app.controller;

import app.exception.NotFoundException;
import app.mapper.VillageMapper;
import app.model.Village;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jiankuan on 15/5/15.
 */
@RestController
public class VillageController {

    @Autowired
    private VillageMapper villageMapper;

    @RequestMapping(value = "/village/new", method = RequestMethod.POST)
    public HttpEntity<Village> insertVillage(@RequestBody Village village) {
        villageMapper.insertVillage(village);
        return new ResponseEntity<>(village, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/village/{vid}", method = RequestMethod.GET)
    public Village getVillage(@PathVariable int vid) {
        Village village = villageMapper.getVillage(vid);
        if (village == null) {
            throw new NotFoundException();
        }
        return village;
    }
}
