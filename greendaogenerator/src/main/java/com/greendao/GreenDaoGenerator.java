package com.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    public static void main(String[] args) throws Exception{

        /**歌曲名字*/
        String title = "title";
        /**演唱者*/
        String artist = "artist";
        /**所属专辑*/
        String album = "album";
        /**路径*/
        String path = "path";
        /**时长*/
        String duration = "duration";
        /**文件大小*/
        String file_size = "file_size";
        /**文件名字*/
        String file_name = "file_name";
        /**meidaID*/
        String media_id = "mediaID";

        /**播放连接*/
        String play_url = "play_url";

        /**头图*/
        String pic_url = "pic_url";

        /**1为数据库的版本,第二个参数是在java-gen下生产的包名*/
        Schema schema = new Schema(1, "com.greendao");

        /**一个实体类就关联到数据库中的一张表,,表名叫LocalMusic*/
        Entity loacl = schema.addEntity("LocalMusic");
        loacl.addIdProperty().autoincrement();
        loacl.addStringProperty(title);
        loacl.addStringProperty(artist);
        loacl.addStringProperty(album);
        loacl.addStringProperty(path);
        loacl.addStringProperty(duration);
        loacl.addStringProperty(file_size);
        loacl.addStringProperty(file_name);
        loacl.addStringProperty(media_id);
        loacl.addStringProperty(play_url);
        loacl.addStringProperty(pic_url);
        loacl.implementsInterface("MusicBeanGen");

        Entity favorites = schema.addEntity("Favorites");
        favorites.addIdProperty().autoincrement();
        favorites.addStringProperty(title);
        favorites.addStringProperty(artist);
        favorites.addStringProperty(album);
        favorites.addStringProperty(path);
        favorites.addStringProperty(duration);
        favorites.addStringProperty(file_size);
        favorites.addStringProperty(file_name);
        favorites.addStringProperty(media_id);
        favorites.addStringProperty(play_url);
        favorites.addStringProperty(pic_url);
        favorites.implementsInterface("MusicBeanGen");

        Entity download = schema.addEntity("Download");
        download.addIdProperty().autoincrement();
        download.addStringProperty(title);
        download.addStringProperty(artist);
        download.addStringProperty(album);
        download.addStringProperty(path);
        download.addStringProperty(duration);
        download.addStringProperty(file_size);
        download.addStringProperty(file_name);
        download.addStringProperty(media_id);
        download.addStringProperty(play_url);
        download.addStringProperty(pic_url);
        download.implementsInterface("MusicBeanGen");

        Entity recent = schema.addEntity("Recent");
        recent.addStringProperty(title);
        recent.addStringProperty(artist);
        recent.addStringProperty(album);
        recent.addStringProperty(path);
        recent.addStringProperty(duration);
        recent.addStringProperty(file_size);
        recent.addStringProperty(file_name);
        recent.addStringProperty(media_id).primaryKey();
        recent.addStringProperty(play_url);
        recent.addStringProperty(pic_url);
        recent.implementsInterface("MusicBeanGen");

        String searchitem = "smitem";//SearchMusicItem
        Entity searchHis = schema.addEntity("SearchHis");
        searchHis.addStringProperty(searchitem).primaryKey();

        new DaoGenerator().generateAll(schema, "app/src/main/java-gen");
    }
}
