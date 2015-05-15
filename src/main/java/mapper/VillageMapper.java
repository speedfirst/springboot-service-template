package mapper;

import model.Village;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * Created by jiankuan on 15/5/15.
 */
public interface VillageMapper {
    @Insert("INSERT INTO village(name, district) VALUES(#{name}, #{district})")
    @Options(useGeneratedKeys=true, keyProperty="vid") // return generated key
    void insertVillage(Village village);

    @Select("SELECT vid, name, district FROM village WHERE vid = #{vid}")
    Village getVillage(int vid);
}
