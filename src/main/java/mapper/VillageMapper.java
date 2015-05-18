package mapper;

import model.Village;

/**
 * Created by jiankuan on 15/5/15.
 *
 * See VillageMapper.xml for configuration
 */
public interface VillageMapper {

    void insertVillage(Village village);

    Village getVillage(int vid);
}
